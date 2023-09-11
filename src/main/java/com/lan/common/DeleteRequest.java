package com.lan.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用的刪除请求
 * @author lan
 */
@Data
@ApiModel(description = "删除队伍")
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private long id;

}
