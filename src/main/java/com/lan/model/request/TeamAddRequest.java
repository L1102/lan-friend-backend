package com.lan.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户登录请求体
 * @author lan
 */
@Data
@ApiModel(description = "创建队伍请求体")
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 队伍名称
     */
    @ApiModelProperty("队伍名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 最大人数
     */
    @ApiModelProperty("最大人数")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @ApiModelProperty("过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty("0 - 公开，1 - 私有，2 - 加密")
    private Integer status;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;

}
