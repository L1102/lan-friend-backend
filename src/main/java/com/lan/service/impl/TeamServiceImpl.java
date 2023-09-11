package com.lan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lan.exception.BusinessException;
import com.lan.common.ErrorCode;
import com.lan.mapper.TeamMapper;
import com.lan.model.domain.Team;
import com.lan.model.domain.User;
import com.lan.model.domain.UserTeam;
import com.lan.model.dto.TeamQuery;
import com.lan.model.enums.TeamStatusEnum;
import com.lan.model.request.TeamJoinRequest;
import com.lan.model.request.TeamQuitRequest;
import com.lan.model.request.TeamUpdateRequest;
import com.lan.model.vo.TeamUserVO;
import com.lan.model.vo.UserVO;
import com.lan.service.TeamService;
import com.lan.service.UserService;
import com.lan.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lan
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public Page<TeamUserVO> listTeams(long currentPage, TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查新条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNoneBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText)
                        .or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if ((!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE))) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        Page<Team> teamListPage = this.page(new Page<>(currentPage, 8), queryWrapper);
        if (CollectionUtils.isEmpty(teamListPage.getRecords())) {
            return new Page<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        Page<TeamUserVO> teamUserVOPage = new Page<>();
        BeanUtils.copyProperties(teamListPage, teamUserVOPage);
        List<Team> records = teamListPage.getRecords();
        for (Team team : records) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            UserVO userVO = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
                teamUserVOList.add(teamUserVO);
            }
        }
        teamUserVOPage.setRecords(teamUserVOList);
        return teamUserVOPage;
    }

    @Override
    public Page<TeamUserVO> getTeamHahJoinNum(Page<TeamUserVO> teamUserVOPage) {
        List<TeamUserVO> teamUserVOList = teamUserVOPage.getRecords();
        teamUserVOList.forEach((team) -> {
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, team.getId());
            long hasJoinNum = userTeamService.count(userTeamLambdaQueryWrapper);
            team.setHasJoinNum(hasJoinNum);
        });
        teamUserVOPage.setRecords(teamUserVOList);
        return teamUserVOPage;
    }

    @Override
    public Page<TeamUserVO> getUserJoinNum(User loginUser, Page<TeamUserVO> teamHahJoinNum) {
        List<TeamUserVO> teamList = teamHahJoinNum.getRecords();
        try {
            List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
            // 判断当前用户已加入的队伍
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId())
                    .in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId)
                    .collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
            teamHahJoinNum.setRecords(teamList);
        } catch (Exception ignored) {
        }
        return teamHahJoinNum;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查新条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNoneBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText)
                        .or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if ((!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE))) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        // Page<Team> teamList = this.page(new Page<>(1, 10), queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            UserVO userVO = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
                teamUserVOList.add(teamUserVO);
            }
        }
        // 关联查询用户信息
        // Team team = new Team();
        return teamUserVOList;
    }

    @Override
    public List<TeamUserVO> listMyCreateTeams(Long userId) {
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId, userId);
        List<Team> teamList = this.list(teamLambdaQueryWrapper);
        List<TeamUserVO> teamUserVOList = teamList.stream().map(team -> this.getTeam(team.getId(), userId)).collect(Collectors.toList());
        return teamUserVOList;
    }

    @Override
    public List<TeamUserVO> listMyJoinTeams(TeamQuery teamQuery) {
        List<Long> idList = teamQuery.getIdList();
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.in(Team::getId, idList);
        List<Team> teamList = this.list(teamLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            UserVO userVO = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
                teamUserVOList.add(teamUserVO);
            }
        }

        return teamUserVOList;
    }

    @Override
    public TeamUserVO getTeam(Long teamId, Long userId) {
        Team team = this.getById(teamId);
        TeamUserVO teamVO = new TeamUserVO();
        BeanUtils.copyProperties(team, teamVO);
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long count = userTeamService.count(userTeamLambdaQueryWrapper);
        teamVO.setHasJoinNum(count);
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        long userJoin = userTeamService.count(userTeamLambdaQueryWrapper);
        teamVO.setHasJoin(userJoin > 0);
        // User leader = userService.getById(team.getUserId());
        // teamVO.setLeaderName(leader.getUsername());
        return teamVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if ((id == null || id <= 0)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队伍的创建者才可以修改
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        // todo 这里会更新全部字段，如果字段不传值就会被设置为null，
        return this.updateById(updateTeam);
    }

    // @Override
    public boolean joinTeam2(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍数量
        Long userId = loginUser.getId();
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍数量
        Long userId = loginUser.getId();
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            while (true) {
                if (lock.tryLock(100, 5000, TimeUnit.MILLISECONDS)) {
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    // userTeamQueryWrapper.eq("userId", userId);
                    // long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    long hasJoinNum = userTeamService.lambdaQuery().eq(UserTeam::getUserId, userId).count();
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    // userTeamQueryWrapper = new QueryWrapper<>();
                    // userTeamQueryWrapper.eq("userId", userId);
                    // userTeamQueryWrapper.eq("teamId", teamId);
                    // long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    long hasUserJoinTeam = userTeamService.lambdaQuery().eq(UserTeam::getUserId, userId)
                            .eq(UserTeam::getTeamId, teamId).count();
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                } else {
                    // 获取锁失败，退出循环
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常");
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩下一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 是否为队长
            if (team.getUserId().equals(userId)) {
                // 把队伍转移给最早加入的用户
                // 1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 根据 id 获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        // 检查是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 查询是否存在加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        // 如果存在关联信息，则先删除关联信息
        if (!CollectionUtils.isEmpty(userTeams)) {
            boolean result = userTeamService.remove(userTeamQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
            }
        }
        // 删除队伍
        return this.removeById(teamId);
    }


    /**
     * 获取队伍当前人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}
