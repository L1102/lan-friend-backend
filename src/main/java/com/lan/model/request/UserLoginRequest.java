package com.lan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author lan
 */
@Data
@ApiModel(description = "用户登录请求体")
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 7788835117104232402L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码")
    private String userPassword;

}
