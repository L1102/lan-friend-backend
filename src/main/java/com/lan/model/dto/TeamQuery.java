package com.lan.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lan.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 队伍查询封装类
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "队伍查询请求体", parent = PageRequest.class)
public class TeamQuery extends PageRequest {

    private static final long serialVersionUID = 5462425737778099968L;
    /**
     * 队伍 id
     */
    @ApiModelProperty(value = "队伍id")
    private Long id;

    /**
     * id 列表
     */
    @ApiModelProperty(value = "id列表")
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和和描述搜索）
     */
    @ApiModelProperty(value = "搜索关键词")
    private String searchText;

    /**
     * 队伍名称
     */
    @ApiModelProperty(value = "队伍名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(value = "队伍描述")
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

}
