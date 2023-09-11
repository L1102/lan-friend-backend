package com.lan.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lan.common.ErrorCode;
import com.lan.exception.BusinessException;
import com.lan.mapper.UserMapper;
import com.lan.model.domain.User;
import com.lan.model.vo.UserVO;
import com.lan.service.UserService;
import com.lan.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lan.contant.RedisConstant.LOGIN_USER_KEY;
import static com.lan.contant.RedisConstant.USER_RECOMMEND_KEY;
import static com.lan.contant.UserConstant.ADMIN_ROLE;
import static com.lan.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 * @author lan
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "abcd";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号太长");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码太短");
        }
        // if (planetCode.length() > 5) {
        //     throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号太长");
        // }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ ]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码含有特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不相同");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        // 大于0表示已有人注册了
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
        // 2. 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setAvatarUrl(user.getAvatarUrl());
        user.setUsername(user.getUsername());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入错误");
        }
        // 返回用户id
        return user.getId();
    }

    @Override
    public String userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户和密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度大于4");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ ]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户含有特殊字符");
        }

        // 2. 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        if (!user.getUserPassword().equals(encryptPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        // 设置会话的最大非活动时间为900秒(15分钟)
        request.getSession().setMaxInactiveInterval(900);
        String token = UUID.randomUUID().toString(true);
        // 将用户登录信息保存到redis
        redisTemplate.opsForValue().set(LOGIN_USER_KEY + token, safetyUser);
        // 设置Redis中保存的用户信息 userStr 的过期时间为10分钟
        redisTemplate.expire(LOGIN_USER_KEY + token, Duration.ofMinutes(30));
        // 返回token
        return token;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        String token = request.getHeader("loginUserToken");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // stringRedisTemplate.delete(RedisConstant.LOGIN_USER_KEY + token);
        redisTemplate.delete(LOGIN_USER_KEY + token);
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户(内存 查询版)
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 在内存中判断是否包含要求，比较灵活
        List<User> collect = userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据标签搜索用户(sql 查询版)
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    public List<User> searchUserByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 遍历tagNameList，对于每个标签名tagName，使用QueryWrapper的like方法构建一个模糊查询条件，查询条件为tags列包含tagName
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        // 使用Java 8的Stream API来对查询结果进行处理
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果是不是管理员，只允许更新当前(自己)用户
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        String token = request.getHeader("loginUserToken");
        if (StrUtil.isBlank(token)) {
            return null;
        }
        Gson gson = new Gson();
        // 从redis中获取用户登录信息
        String userStr = stringRedisTemplate.opsForValue().get(LOGIN_USER_KEY + token);
        // Object userObject = redisTemplate.opsForValue().get(LOGIN_USER_KEY + token);
        if (StringUtils.isBlank(userStr)) {
            return null;
        }
        // 进一步处理用户对象
        User user = gson.fromJson(userStr, User.class);
        // 设置过期时间为30分钟
        redisTemplate.expire(LOGIN_USER_KEY + token, 30L, TimeUnit.MINUTES);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        request.getSession().setMaxInactiveInterval(900);
        return user;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 鉴权，仅管理员可查询
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public Page<UserVO> getRecommendUsers(long currentPage, User loginUser) {
        if (loginUser == null) {
            return getRandomUser();
        }
        Gson gson = new Gson();
        String redisKey = String.format(USER_RECOMMEND_KEY + loginUser.getId() + ":" + currentPage);
        if (currentPage <= 5) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                String user = stringRedisTemplate.opsForValue().get(redisKey);
                return gson.fromJson(user, new TypeToken<Page<UserVO>>() {
                }.getType());
            } else {
                Page<UserVO> userVOPage = this.matchUsers(currentPage, loginUser);
                redisTemplate.opsForValue().set(redisKey, userVOPage);
                return userVOPage;
            }
        } else {
            Page<UserVO> userVOPage = this.matchUsers(currentPage, loginUser);
            redisTemplate.opsForValue().set(redisKey, userVOPage);
            return userVOPage;
        }
    }

    private Page<UserVO> getRandomUser() {
        List<User> randomUsers = userMapper.getRandomUser();
        List<UserVO> userVOList = randomUsers.stream().map(randomUser -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(randomUser, userVO);
            return userVO;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(randomUsers, userVOList);
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    @Override
    public Page<UserVO> userPage(long currentPage) {
        Page<User> page = this.page(new Page<>(currentPage, 8));
        Page<UserVO> userVOPage = new Page<>();
        BeanUtils.copyProperties(page, userVOPage);
        return userVOPage;
    }

    @Override
    public Page<UserVO> matchUsers(long currentPage, User loginUser) {
        String tags = loginUser.getTags();
        if (tags == null) {
            return this.userPage(currentPage);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 --》相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或为当前用户
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .collect(Collectors.toList());

        //截取currentPage所需的List
        ArrayList<Pair<User, Long>> finalUserPairList = new ArrayList<>();
        int begin = (int) ((currentPage - 1) * 8);
        int end = (int) (((currentPage - 1) * 8) + 8) - 1;
        if (topUserPairList.size() < end) {
            //剩余数量
            int temp = (int) (topUserPairList.size() - begin);
            if (temp <= 0) {
                return new Page<>();
            }
            for (int i = begin; i <= begin + temp - 1; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        } else {
            for (int i = begin; i < end; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        }

        List<Long> userIdList = finalUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        List<UserVO> userVOList = this.list(userQueryWrapper).stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        userVOPage.setCurrent(currentPage);
        userVOPage.setSize(userVOList.size());
        userVOPage.setTotal(userVOList.size());
        return userVOPage;
    }

    /*@Override
    public List<User> matchUsers(long currentPage, User loginUser) {
        // QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // queryWrapper.select("id", "tags");
        // queryWrapper.isNotNull("tags");
        // List<User> userList = this.list(queryWrapper);

        // // 判断缓存中有没有数据,有就直接读
        String redisKey = String.format(USER_RECOMMEND_KEY + "%s", loginUser.getId());
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if (userPage != null) {
            return null;
        } else {

            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "tags");
            queryWrapper.isNotNull("tags");
            List<User> userList = this.list(queryWrapper);
            String tags = loginUser.getTags();
            Gson gson = new Gson();
            List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            // 用户列表的下标 --》相似度
            List<Pair<User, Long>> list = new ArrayList<>();
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                String userTags = user.getTags();
                // 无标签或为当前用户
                if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                    continue;
                }
                List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
                }.getType());
                // 计算分数
                long distance = AlgorithmUtils.minDistance(tagList, userTagList);
                list.add(new Pair<>(user, distance));
            }
            List<Pair<User, Long>> toUserPairList = list.stream()
                    .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                    .limit(currentPage).collect(Collectors.toList());
            List<Long> userIdList = toUserPairList.stream()
                    .map(pair -> pair.getKey().getId())
                    .collect(Collectors.toList());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", userIdList);
            // 1, 3, 2
            // User1、User2、User3
            // 1 => User1, 2 => User2, 3 => User3
            Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                    .map(this::getSafetyUser)
                    .collect(Collectors.groupingBy(User::getId));
            List<User> finalUserList = new ArrayList<>();
            for (Long userId : userIdList) {
                finalUserList.add(userIdUserListMap.get(userId).get(0));
            }
            return finalUserList;
        }


    }*/
}