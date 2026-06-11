package com.knowledgehub.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 *
 * 学习要点：
 * 1. 为什么用枚举而不是常量类？—— 类型安全、可维护性好
 * 2. 错误码分段规范：
 *    1xxx = 客户端错误（参数、权限等）
 *    2xxx = 服务端错误（系统异常、数据库异常等）
 *    3xxx = 第三方服务错误（Redis、DeepSeek 等）
 *    4xxx = 业务逻辑错误（知识库不存在、文档解析失败等）
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 1xxx: 客户端错误 ====================
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_ERROR(1001, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或 token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(1002, "请求过于频繁，请稍后重试"),

    // ==================== 2xxx: 服务端错误 ====================
    INTERNAL_ERROR(500, "系统内部错误"),
    DB_ERROR(2001, "数据库操作异常"),
    FILE_UPLOAD_ERROR(2002, "文件上传失败"),
    FILE_PARSE_ERROR(2003, "文件解析失败"),

    // ==================== 3xxx: 第三方服务错误 ====================
    REDIS_ERROR(3001, "Redis 操作异常"),
    EMBEDDING_ERROR(3002, "Embedding 服务调用失败"),
    LLM_ERROR(3003, "大模型服务调用失败"),
    LLM_TIMEOUT(3004, "大模型服务调用超时"),

    // ==================== 4xxx: 业务错误 ====================
    USER_EXISTS(4001, "用户名已存在"),
    USER_NOT_FOUND(4002, "用户不存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    USER_DISABLED(4004, "账号已被禁用"),
    CAPTCHA_ERROR(4005, "验证码错误或已过期"),
    CAPTCHA_TOO_FREQUENT(4006, "验证码发送过于频繁"),

    KB_NOT_FOUND(4101, "知识库不存在"),
    KB_NO_PERMISSION(4102, "无权限操作该知识库"),
    KB_NAME_EXISTS(4103, "知识库名称已存在"),

    DOC_NOT_FOUND(4201, "文档不存在"),
    DOC_PARSE_ALREADY_RUNNING(4202, "文档正在解析中，请勿重复触发"),
    DOC_PARSE_FAILED(4203, "文档解析失败"),
    DOC_TYPE_NOT_SUPPORTED(4204, "不支持的文档类型，仅支持 PDF 和 Markdown"),

    CHAT_QUOTA_EXCEEDED(4301, "今日提问次数已用完"),
    CHAT_NO_RELEVANT_CONTENT(4302, "知识库中没有找到相关内容，请换个问题试试"),

    // ==================== 5xxx: 幂等/重复提交 ====================
    DUPLICATE_SUBMIT(5001, "请勿重复提交");

    private final Integer code;
    private final String message;
}
