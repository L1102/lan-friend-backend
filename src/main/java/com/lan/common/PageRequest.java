package com.lan.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 * @author lan
 */
@Data
@ApiModel(description = "分页请求参数")
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分页大小
     */
    @ApiModelProperty("分页大小")
    protected int pageSize;

    /**
     * 当前第几页
     */
    @ApiModelProperty("当前第几页")
    protected int pageNum;
}
