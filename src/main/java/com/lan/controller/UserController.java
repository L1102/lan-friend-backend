package com.lan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lan.common.BaseResponse;
import com.lan.common.ErrorCode;
import com.lan.common.ResultUtils;
import com.lan.exception.BusinessException;
import com.lan.model.domain.User;
import com.lan.model.request.UserLoginRequest;
import com.lan.model.request.UserRegisterRequest;
import com.lan.model.vo.UserVO;
import com.lan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * @author lan
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户管理模块")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String token = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(token);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("用户注销")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @GetMapping("/current")
    @ApiOperation("获取当前用户")
    @ApiImplicitParam(name = "request", value = "request")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 当用户更新信息后，此处获取到的用户信息是旧的，不是最新的
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);

        /*Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);*/
    }

    @GetMapping("/search")
    @ApiOperation("搜索用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名")})
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        List<User> list = userList.stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    // @GetMapping("/recommend")
    // public BaseResponse<List<User>> recommendUsers(String username, HttpServletRequest request) {
    //     QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
    //
    //     List<User> userList = userService.list(userQueryWrapper);
    //     List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    //     return ResultUtils.success(list);
    // }

    @GetMapping("/recommend")
    @ApiOperation("获取首页用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "currentPage", value = "第一页"),
                    @ApiImplicitParam(name = "request", value = "request")})
    public BaseResponse<Page<UserVO>> recommendUsers(long currentPage, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<UserVO> recommendUsers = userService.getRecommendUsers(currentPage, loginUser);
        return ResultUtils.success(recommendUsers);
        // 判断缓存中有没有数据,有就直接读
        // String redisKey = String.format(USER_RECOMMEND_KEY + "%s", loginUser.getId());
        // Gson gson = new Gson();
        // // Page<User> userPage = (Page<User>) stringRedisTemplate.opsForValue().get(redisKey);
        // Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        // // String userPage = (String) redisTemplate.opsForValue().get(redisKey);
        // if (userPage != null) {
        //     return ResultUtils.success(userPage);
        // }
        // // 没有缓存,就查数据库
        // QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        // userPage = userService.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        // try {
        //     // valueOperations.set(redisKey, userPage, 20000, TimeUnit.MILLISECONDS);
        //     redisTemplate.opsForValue().set(redisKey, userPage);
        // } catch (Exception e) {
        //     log.error("redis set key error", e);
        // }
        // return ResultUtils.success(userPage);
    }

    /**
     * 按标签搜索
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    @ApiOperation("按标签搜索用户")
    @ApiImplicitParam(name = "tagNameList", value = "标签列表")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 用户更新
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    @ApiOperation("用户更新")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);

    }

    /**
     * 删除
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation("刪除用户")
    @ApiImplicitParam(name = "id", value = "用户id")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @GetMapping("/match")
    @ApiOperation("匹配用户")
    @ApiImplicitParam(name = "num", value = "匹配数量(不能超过20)")
    public BaseResponse<Page<UserVO>> matchUsers(long num, HttpServletRequest request) {
        // if (num <= 0 || num >= 21) {
        //     throw new BusinessException(ErrorCode.PARAMS_ERROR);
        // }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }
}
