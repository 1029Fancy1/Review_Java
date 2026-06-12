package com.knowledgehub.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档分页查询 DTO
 */
@Data
@Schema(description = "文档分页查询")
public class DocumentPageDTO {

    @NotNull(message = "知识库 ID 不能为空")
    @Schema(description = "知识库 ID", example = "1")
    private Long kbId;

    @Min(value = 1, message = "页码最小为 1")
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}
