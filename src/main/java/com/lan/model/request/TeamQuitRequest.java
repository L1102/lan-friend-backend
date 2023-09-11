package com.lan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 * @author lan
 */
@Data
@ApiModel(description = "退出队伍请求体")
public class TeamQuitRequest implements Serializable {


    private static final long serialVersionUID = 7876675827099340588L;

    /**
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private Long teamId;

}
