package com.lan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author lan
 */
@Data
@ApiModel(description = "用户注册请求体")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 7788835117104232402L;

    /**
     * 用户注册账号
     */
    @ApiModelProperty("用户注册账号")
    private String userAccount;

    /**
     * 用户注册密码
     */
    @ApiModelProperty("用户注册密码")
    private String userPassword;

    /**
     * 再次确认密码
     */
    @ApiModelProperty("再次确认密码")
    private String checkPassword;
}
