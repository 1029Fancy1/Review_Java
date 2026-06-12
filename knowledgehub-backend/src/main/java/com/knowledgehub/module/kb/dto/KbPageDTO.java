package com.knowledgehub.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 知识库分页查询 DTO
 *
 * 学习要点：
 * 1. 分页参数 page/size 设置默认值和范围限制
 * 2. 防止前端恶意传超大值拖垮数据库
 */
@Data
@Schema(description = "知识库分页查询")
public class KbPageDTO {

    @Min(value = 1, message = "页码最小为 1")
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}
