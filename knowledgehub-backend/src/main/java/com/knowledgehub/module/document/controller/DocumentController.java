package com.knowledgehub.module.document.controller;

import com.knowledgehub.common.PageResult;
import com.knowledgehub.common.Result;
import com.knowledgehub.context.UserContext;
import com.knowledgehub.module.document.dto.DocumentPageDTO;
import com.knowledgehub.module.document.service.DocumentService;
import com.knowledgehub.module.document.vo.DocumentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档控制器
 *
 * 接口清单：
 * - POST   /api/document/upload  — 上传文档
 * - GET    /api/document/list    — 文档列表（按知识库过滤）
 * - GET    /api/document/{id}    — 文档详情
 * - DELETE /api/document/{id}    — 删除文档
 */
@Tag(name = "文档模块", description = "文档上传、列表、详情、删除")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    

    @Operation(summary = "上传文档",
            description = "上传 PDF 或 Markdown 文件到指定知识库，文件会自动重命名防冲突")
    @PostMapping("/upload")
    public Result<DocumentVO> upload(
            @Parameter(description = "知识库 ID") @RequestParam Long kbId,
            @Parameter(description = "文件") @RequestParam MultipartFile file) {
        Long userId = UserContext.getUserId();
        DocumentVO vo = documentService.upload(kbId, file, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "文档列表")
    @GetMapping("/list")
    public Result<PageResult<DocumentVO>> list(@Valid DocumentPageDTO dto) {
        Long userId = UserContext.getUserId();
        PageResult<DocumentVO> result = documentService.listByPage(dto, userId);
        return Result.ok(result);
    }

    @Operation(summary = "文档详情")
    @GetMapping("/{id}")
    public Result<DocumentVO> getById(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        DocumentVO vo = documentService.getDetail(id, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        documentService.delete(id, userId);
        return Result.ok();
    }
}
