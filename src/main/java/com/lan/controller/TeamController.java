package com.lan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lan.common.BaseResponse;
import com.lan.common.DeleteRequest;
import com.lan.common.ErrorCode;
import com.lan.common.ResultUtils;
import com.lan.exception.BusinessException;
import com.lan.model.domain.Team;
import com.lan.model.domain.User;
import com.lan.model.domain.UserTeam;
import com.lan.model.dto.TeamQuery;
import com.lan.model.request.TeamAddRequest;
import com.lan.model.request.TeamJoinRequest;
import com.lan.model.request.TeamQuitRequest;
import com.lan.model.request.TeamUpdateRequest;
import com.lan.model.vo.TeamUserVO;
import com.lan.service.TeamService;
import com.lan.service.UserService;
import com.lan.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 队伍接口
 * @author lan
 */
@RestController
@RequestMapping("/team")
@Slf4j
@Api(tags = "队伍管理模块")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    @ApiOperation("创建队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }


    @PostMapping("/update")
    @ApiOperation("更新队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @ApiOperation("根据队伍id获取队伍")
    @ApiImplicitParam(name = "id", value = "队伍id")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    @ApiOperation("获取队伍列表")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Page<TeamUserVO>> listTeams2(long currentPage, TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        User loginUser = userService.getLoginUser(request);
        // 1、查询队伍列表
        Page<TeamUserVO> teamUserVOPage = teamService.listTeams(currentPage, teamQuery, isAdmin);
        Page<TeamUserVO> teamHahJoinNum = teamService.getTeamHahJoinNum(teamUserVOPage);
        Page<TeamUserVO> finalTeamUserVOPage = teamService.getUserJoinNum(loginUser, teamHahJoinNum);
        return ResultUtils.success(finalTeamUserVOPage);
    }


    /*@ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception ignored) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum((long) teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }*/

    // /**
    //  * 获取我创建的队伍
    //  * @param teamQuery
    //  * @param request
    //  * @return
    //  */
    // @GetMapping("/list/my/create")
    // @ApiOperation("获取用户创建的队伍的信息")
    // @ApiImplicitParam(name = "request", value = "request")
    // public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
    //     if (teamQuery == null) {
    //         throw new BusinessException(ErrorCode.PARAMS_ERROR);
    //     }
    //     User loginUser = userService.getLoginUser(request);
    //     teamQuery.setUserId(loginUser.getId());
    //     List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
    //     return ResultUtils.success(teamList);
    // }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    @ApiOperation("获取用户创建的队伍的信息")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listMyCreateTeams(loginUser.getId());
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    @ApiOperation("获取用户加入的队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = Optional.ofNullable(userService.getLoginUser(request))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN));
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
        // 查询用户加入的队伍
        List<UserTeam> userTeamList = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId()).list();
        // 获取队伍ID列表
        List<Long> idList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        if (idList.isEmpty()) {
            return null;
        }
        teamQuery.setIdList(idList);

        // List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        List<TeamUserVO> teamList = teamService.listMyJoinTeams(teamQuery);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        List<UserTeam> userTeamList2 = userTeamService.lambdaQuery().in(UserTeam::getTeamId, teamIdList).list();
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            userTeamLambdaQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList2.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> team.setHasJoin(hasJoinTeamIdSet.contains(team.getId())));
        } catch (Exception ignored) {
        }
        // 3、查询已加入队伍的人数
        List<UserTeam> userTeamList3 = userTeamService.lambdaQuery().in(UserTeam::getTeamId, teamIdList).list();
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList3.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum((long) teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join2")
    @ApiOperation("获取用户加入的队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams2(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 查询用户加入的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        // 获取队伍ID列表
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        if (idList.isEmpty()) {
            return null;
        }
        teamQuery.setIdList(idList);

        // List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        List<TeamUserVO> teamList = teamService.listMyJoinTeams(teamQuery);

        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList2 = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList2.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception ignored) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList3 = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList3.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum((long) teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    @ApiOperation("分页获取队伍信息")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        int pageNum = teamQuery.getPageNum();
        int pageSize = teamQuery.getPageSize();
        Page<Team> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    @ApiOperation(value = "加入队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    @ApiOperation("退出队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    @ApiOperation("删除队伍")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }


}
