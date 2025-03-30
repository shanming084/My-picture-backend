package com.shanming.mypicturebackend.exception;

import com.shanming.mypicturebackend.common.BaseResponse;
import com.shanming.mypicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//写切面和切点
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //使用aop将所有BusinessException异常拦截，使用返回工具类返回信息
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException",e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }

    //使用aop将所有运行时异常拦截，使用返回工具类并定义为系统异常来返回信息
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("BusinessException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }
}
