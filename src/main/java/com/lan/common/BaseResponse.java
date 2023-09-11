package com.lan.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 * @author lan
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = -6910288092749194181L;

    /**
     * 响应码
     */
    @ApiModelProperty("响应码")
    private int code;

    /**
     * 返回的数据
     */
    @ApiModelProperty("返回的数据")
    private T data;

    /**
     * 返回的信息
     */
    @ApiModelProperty("返回的信息")
    private String message;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
