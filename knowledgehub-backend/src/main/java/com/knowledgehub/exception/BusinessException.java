package com.knowledgehub.exception;

import com.knowledgehub.common.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * 学习要点：
 * 1. 为什么继承 RuntimeException 而不是 Exception？
 *    —— 业务异常通常是不可恢复的，不需要强制 try-catch
 *    同时 Spring 事务默认对 RuntimeException 回滚
 *
 * 2. 为什么要持有 ErrorCode？
 *    —— 方便 GlobalExceptionHandler 统一提取 code 和 message
 *
 * 使用方式：
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
