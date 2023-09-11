package com.lan.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍和用户信息封装类
 * @author lan
 */
@Data
@ApiModel(description = "队伍信息封装类")
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = 159026068577623464L;

    /**
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private Long id;

    /**
     * 队伍名称
     */
    @ApiModelProperty(value = "队伍名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 最大人数
     */
    @ApiModelProperty(value = "最大人数")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty(value = "0 - 公开，1 - 私有，2 - 加密")
    private Integer status;


    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    @ApiModelProperty(reference = "用户封装类")
    UserVO createUser;

    /**
     * 是否已加入队伍
     */
    @ApiModelProperty(value = "是否已加入队伍")
    private Boolean hasJoin = false;

    /**
     * 已加入的用户数
     */
    @ApiModelProperty(value = "已加入的用户数")
    private Long hasJoinNum;
}
