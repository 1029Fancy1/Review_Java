package com.knowledgehub.module.user.controller;

import com.knowledgehub.common.ErrorCode;
import com.knowledgehub.common.Result;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.redis.CaptchaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码控制器
 */
@Tag(name = "验证码模块", description = "验证码生成与校验")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "获取邮箱验证码")
    @PostMapping("/captcha")
    public Result<Void> getCaptcha(
            @Parameter(description = "邮箱") @RequestParam String email) {
        if (email == null || email.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱不能为空");
        }
        captchaService.generate(email);
        return Result.ok();
    }

    @Operation(summary = "校验验证码")
    @PostMapping("/captcha/verify")
    public Result<Boolean> verifyCaptcha(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "验证码") @RequestParam String code) {
        return Result.ok(captchaService.verify(email, code));
    }
}
