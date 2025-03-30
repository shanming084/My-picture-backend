package com.shanming.mypicturebackend.exception;

/**
 * 异常处理工具类
 */
public class ThrowUtils {

    /**
     * 如果传入的判断为true，则抛传入的异常
     * @param condition 情况
     * @param e 异常
     */
    public static void throwIf(boolean condition, RuntimeException e) {
        if (condition){
            throw e;
        }
    }

    /**
     * 条件成立时抛内部异常
     * @param condition 情况
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode){
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立时抛内部异常和错误信息
     * @param condition 情况
     * @param errorCode 错误码
     * @param description 详细信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String description){
        throwIf(condition, new BusinessException(errorCode,description));
    }
}
