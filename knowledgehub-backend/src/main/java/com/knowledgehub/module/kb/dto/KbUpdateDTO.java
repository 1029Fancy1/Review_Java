package com.knowledgehub.module.kb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库更新请求 DTO
 */
@Data
@Schema(description = "更新知识库请求")
public class KbUpdateDTO {

    @NotBlank(message = "知识库名称不能为空")
    @Size(min = 1, max = 100, message = "知识库名称长度 1-100")
    @Schema(description = "知识库名称", example = "Java 面试知识库 V2")
    private String name;

    @Schema(description = "知识库描述")
    private String description;
}
