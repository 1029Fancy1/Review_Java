package com.knowledgehub.module.user.controller;

import com.knowledgehub.common.Result;
import com.knowledgehub.module.user.dto.LoginDTO;
import com.knowledgehub.module.user.dto.RegisterDTO;
import com.knowledgehub.module.user.entity.User;
import com.knowledgehub.module.user.service.UserService;
import com.knowledgehub.module.user.vo.LoginVO;
import com.knowledgehub.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "用户模块", description = "用户注册、登录、个人信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    @Operation(summary = "用户登录，返回 JWT token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "查询所有用户（测试用）")
    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        List<UserVO> voList = userService.list().stream()
                .map(user -> UserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .status(user.getStatus())
                        .createTime(user.getCreateTime())
                        .build())
                .collect(Collectors.toList());
        return Result.ok(voList);
    }

    @Operation(summary = "根据 ID 查用户（测试用）")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        UserVO vo = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
        return Result.ok(vo);
    }
}
