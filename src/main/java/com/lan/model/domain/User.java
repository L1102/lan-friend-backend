package com.lan.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @author lan
 */
@Data
@TableName(value = "user")
@ApiModel(value = "用户实体")
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 用户昵称
     */
    @TableField(value = "`username`")
    @ApiModelProperty(value = "用户昵称")
    private String username;

    /**
     * 账号
     */
    @TableField(value = "`userAccount`")
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @TableField(value = "`avatarUrl`")
    @ApiModelProperty(value = "用户头像")
    private String avatarUrl;

    /**
     * 性别
     */
    @TableField(value = "`gender`")
    @ApiModelProperty(value = "性别")
    private Integer gender;

    /**
     * 密码
     */
    @TableField(value = "`userPassword`")
    @ApiModelProperty(value = "用户密码")
    private String userPassword;

    /**
     * 电话
     */
    @TableField(value = "`phone`")
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "`email`")
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 用户状态 0-正常
     */
    @TableField(value = "`userStatus`")
    @ApiModelProperty(value = "用户状态，0为正常")
    private Integer userStatus;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @TableField(value = "`userRole`")
    @ApiModelProperty(value = "用户角色 0-普通用户,1-管理员")
    private Integer userRole;

    /**
     * 创建时间
     */
    @TableField(value = "`createTime`")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "`updateTime`")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "`isDelete`")
    @TableLogic  //表示逻辑删除
    @ApiModelProperty(value = "逻辑删除")
    private Integer isDelete;


    /**
     * 标签 json 列表
     */
    @TableField(value = "`tags`")
    @ApiModelProperty(value = "标签列表")
    private String tags;

    /**
     * 个人简介
     */
    @TableField(value = "`profile`")
    @ApiModelProperty(value = "个人简介")
    private String profile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public String getUsername() {
        if (username == null) {
            this.username = "匿名";
        }
        return username;
    }


    public void setUsername(String username) {
        this.username = username;

    }

    public String getAvatarUrl() {
        if (avatarUrl == null) {
            this.avatarUrl = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201611%2F13%2F20161113235403_mChxJ.thumb.400_0.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1674204199&t=c7e8acabbf622115a358112235e27b39";
        }
        return avatarUrl;

    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}