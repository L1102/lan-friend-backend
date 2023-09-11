package com.lan.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签
 * @author lan
 */
@Data
@TableName(value = "`tag`")
public class Tag {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    @TableField(value = "`tagName`")
    private String tagName;

    /**
     * 用户id
     */
    @TableField(value = "`userId`")
    private Long userId;

    /**
     * 父标签id
     */
    @TableField(value = "`parentId`")
    private Long parentId;

    /**
     * 0 - 不是, 1 - 是父标签
     */
    @TableField(value = "`isParent`")
    private Integer isParent;

    /**
     * 创建时间
     */
    @TableField(value = "`createTime`")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "`updateTime`")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(value = "`isDelete`")
    private Integer isDelete;

}