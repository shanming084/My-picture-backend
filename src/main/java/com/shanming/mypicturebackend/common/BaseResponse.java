package com.shanming.mypicturebackend.common;

import com.shanming.mypicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {


    private static final long serialVersionUID = 9161447921393877909L;

    private int code;

    private T data;

    private String msg;

    public BaseResponse(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage() );
    }
}
