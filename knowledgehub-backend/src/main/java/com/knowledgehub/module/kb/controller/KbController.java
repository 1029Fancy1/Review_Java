package com.knowledgehub.module.kb.controller;

import com.knowledgehub.common.Result;
import com.knowledgehub.module.kb.entity.KnowledgeBase;
import com.knowledgehub.module.kb.service.KbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库控制器
 *
 * Day 2：基础骨架
 * Day 4：将添加完整 CRUD 及权限校验
 */
@Tag(name = "知识库模块", description = "知识库创建、列表、更新、删除")
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    @Operation(summary = "查询所有知识库（Day 2 测试用）")
    @GetMapping("/list")
    public Result<List<KnowledgeBase>> list() {
        return Result.ok(kbService.list());
    }

    @Operation(summary = "根据 ID 查知识库（Day 2 测试用）")
    @GetMapping("/{id}")
    public Result<KnowledgeBase> getById(@PathVariable Long id) {
        return Result.ok(kbService.getById(id));
    }
}
