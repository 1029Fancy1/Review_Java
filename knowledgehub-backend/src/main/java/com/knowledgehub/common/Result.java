package com.knowledgehub.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回体
 *
 * 前端接收格式：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 *
 * 学习要点：
 * 1. 为什么用泛型 <T>？—— 不同接口返回不同类型的数据
 * 2. @JsonInclude(JsonInclude.Include.NON_NULL) 的作用？—— null 字段不序列化，减少响应体积
 * 3. 静态工厂方法 vs 构造器的选择？—— 工厂方法语义更清晰（ok / fail）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 200 成功
     * 其他 失败（对应 ErrorCode 枚举）
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    // ==================== 成功响应 ====================

    public static <T> Result<T> ok() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败响应 ====================

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
