package com.lan.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lan
 */
@Data
@ApiModel(description = "用户封装类")
public class UserVO implements Serializable {

    private static final long serialVersionUID = -1338324045594902557L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String username;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String avatarUrl;

    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private Integer gender;

    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 标签 json 列表
     */
    @ApiModelProperty(value = "标签列表")
    private String tags;

    /**
     * 个人简介
     */
    @ApiModelProperty(value = "个人简介")
    private String profile;

    /**
     * 用户状态 0-正常
     */
    @ApiModelProperty(value = "用户状态 0-正常")
    private Integer userStatus;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @ApiModelProperty(value = "用户角色 0-普通用户 1-管理员")
    private Integer userRole;

    /**
     * 创建时间
     */
    // @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
