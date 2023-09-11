package com.lan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 * @author lan
 */
@Data
@ApiModel(description = "加入队伍请求体")
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private Long teamId;

    /**
     * 队伍密码
     */
    @ApiModelProperty(value = "队伍密码")
    private String password;

}
