package com.knowledgehub.module.kb.controller;

import com.knowledgehub.common.PageResult;
import com.knowledgehub.common.Result;
import com.knowledgehub.context.UserContext;
import com.knowledgehub.module.kb.dto.KbCreateDTO;
import com.knowledgehub.module.kb.dto.KbPageDTO;
import com.knowledgehub.module.kb.dto.KbUpdateDTO;
import com.knowledgehub.module.kb.service.KbService;
import com.knowledgehub.module.kb.vo.KbVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库控制器
 *
 * 接口清单：
 * - POST   /api/kb/create  — 创建知识库
 * - GET    /api/kb/list    — 分页列表
 * - GET    /api/kb/{id}    — 知识库详情
 * - PUT    /api/kb/{id}    — 更新知识库
 * - DELETE /api/kb/{id}    — 删除知识库
 *
 * 学习要点（Day 4）：
 * 1. 所有接口携带 userId，Service 层做权限校验
 * 2. 分页查询用 PageResult 包装
 * 3. Day 6 后 userId 从 UserContext 获取，不再手动传
 */
@Tag(name = "知识库模块", description = "知识库创建、列表、更新、删除")
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    /**
     * 临时方案：从请求头获取 userId（Day 6 替换为 UserContext）
     */


    @Operation(summary = "创建知识库")
    @PostMapping("/create")
    public Result<KbVO> create(@Valid @RequestBody KbCreateDTO dto) {
        Long userId = UserContext.getUserId();
        KbVO vo = kbService.create(dto, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "知识库分页列表")
    @GetMapping("/list")
    public Result<PageResult<KbVO>> list(@Valid KbPageDTO dto) {
        Long userId = UserContext.getUserId();
        PageResult<KbVO> result = kbService.listByPage(dto, userId);
        return Result.ok(result);
    }

    @Operation(summary = "知识库详情")
    @GetMapping("/{id}")
    public Result<KbVO> getById(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        KbVO vo = kbService.getDetail(id, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public Result<KbVO> update(@PathVariable Long id, @Valid @RequestBody KbUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        KbVO vo = kbService.update(id, dto, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        kbService.delete(id, userId);
        return Result.ok();
    }
}
