package com.knowledgehub.module.user.controller;

import com.knowledgehub.common.Result;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.module.user.dto.LoginDTO;
import com.knowledgehub.module.user.dto.RegisterDTO;
import com.knowledgehub.module.user.entity.User;
import com.knowledgehub.module.user.service.UserService;
import com.knowledgehub.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * 接口清单：
 * - POST /api/user/register — 注册
 * - POST /api/user/login    — 登录
 * - GET  /api/user/list     — 查询所有用户（Day 2 测试用）
 * - GET  /api/user/{id}     — 根据 ID 查用户（Day 2 测试用）
 */
@Tag(name = "用户模块", description = "用户注册、登录、个人信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @Valid 触发 DTO 上的校验注解（@NotBlank, @Size, @Email）
     * 校验失败时抛出 MethodArgumentNotValidException，由 GlobalExceptionHandler 统一处理
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO vo = userService.register(dto);
        return Result.ok(vo);
    }

    /**
     * 用户登录
     *
     * Day 9 将改造此接口，返回 JWT token
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        UserVO vo = userService.login(dto);
        return Result.ok(vo);
    }

    // ==================== Day 2 测试接口，后续移除 ====================

    @Operation(summary = "查询所有用户（测试用）")
    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.ok(userService.list());
    }

    @Operation(summary = "根据 ID 查用户（测试用）")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }
}
