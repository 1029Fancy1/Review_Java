package com.knowledgehub.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 *
 * 学习要点：
 * 1. 登录只需要 username + password，字段比注册少
 * 2. DTO 按接口拆分，而不是一个 DTO 复用所有接口
 *    —— 每个接口的必填字段不同，校验规则不同
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}
