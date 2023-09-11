package com.lan.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @author lan
 */
@Data
@TableName(value = "team")
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 最大人数
     */
    @TableField(value = "maxNum")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @TableField(value = "expireTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @TableField(value = "`status`")
    private Integer status;

    /**
     * 密码
     */
    @TableField(value = "`password`")
    private String password;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Byte isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}