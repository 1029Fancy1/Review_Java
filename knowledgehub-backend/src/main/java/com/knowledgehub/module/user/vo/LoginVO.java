package com.knowledgehub.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录返回 VO — 包含 token + 用户信息
 */
@Data
@Builder
@Schema(description = "登录返回")
public class LoginVO {

    @Schema(description = "JWT token")
    private String token;

    @Schema(description = "用户信息")
    private UserVO userInfo;
}
