package com.knowledgehub.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库创建请求 DTO
 */
@Data
@Schema(description = "创建知识库请求")
public class KbCreateDTO {

    @NotBlank(message = "知识库名称不能为空")
    @Size(min = 1, max = 100, message = "知识库名称长度 1-100")
    @Schema(description = "知识库名称", example = "Java 面试知识库")
    private String name;

    @Schema(description = "知识库描述", example = "Spring Boot 和 Redis 复习资料")
    private String description;
}
