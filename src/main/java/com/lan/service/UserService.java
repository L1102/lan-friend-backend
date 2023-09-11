package com.lan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lan.model.domain.User;
import com.lan.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 * @author lan
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 返回脱敏后的用户信息
     */
    String userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    // /**
    //  * 匹配用户
    //  * @param num
    //  * @param loginUser
    //  * @return
    //  */
    // List<User> matchUsers(long num, User loginUser);

    Page<UserVO> userPage(long currentPage);

    Page<UserVO> matchUsers(long currentPage, User loginUser);

    Page<UserVO> getRecommendUsers(long pageNum, User loginUser);
}


