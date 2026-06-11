package com.knowledgehub.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 *
 * 学习要点（Day 3）：
 * 1. DTO (Data Transfer Object) — 用于接收前端入参，和数据库 Entity 隔离
 * 2. @Valid 注解触发校验，校验规则定义在 DTO 字段上
 * 3. 为什么不用 Entity 直接接收请求？
 *    —— Entity 包含数据库所有字段，直接暴露给前端不安全
 *    —— 不同接口需要不同的校验规则（注册需要 username，查询不需要）
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度 3-50 位")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32 位")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;
}
