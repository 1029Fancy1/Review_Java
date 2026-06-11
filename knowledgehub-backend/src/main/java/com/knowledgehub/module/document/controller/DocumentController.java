package com.knowledgehub.module.document.controller;

import com.knowledgehub.common.Result;
import com.knowledgehub.module.document.entity.Document;
import com.knowledgehub.module.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档控制器
 *
 * Day 2：基础骨架
 * Day 5：将添加文档上传、解析触发等接口
 */
@Tag(name = "文档模块", description = "文档上传、列表、解析、进度查询")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "查询所有文档（Day 2 测试用）")
    @GetMapping("/list")
    public Result<List<Document>> list() {
        return Result.ok(documentService.list());
    }

    @Operation(summary = "根据 ID 查文档（Day 2 测试用）")
    @GetMapping("/{id}")
    public Result<Document> getById(@PathVariable Long id) {
        return Result.ok(documentService.getById(id));
    }
}
