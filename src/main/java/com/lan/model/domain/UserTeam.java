package com.lan.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户队伍关系
 * @author lan
 */

@Data
@TableName(value = "user_team")
public class UserTeam implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 队伍id
     */
    @TableField(value = "teamId")
    private Long teamId;

    /**
     * 加入时间
     */
    @TableField(value = "joinTime")
    private Date joinTime;

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
    private Byte isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}