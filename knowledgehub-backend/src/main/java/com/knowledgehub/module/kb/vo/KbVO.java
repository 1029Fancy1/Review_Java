package com.knowledgehub.module.kb.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库返回 VO
 *
 * 学习要点：
 * 1. 不含 userId——前端不需要知道用户ID（当前用户就是自己）
 * 2. kbVersion 只在缓存逻辑里用，是否暴露给前端视需求而定
 * 3. docCount 是衍生字段，从 document 表聚合统计（Day 5 实现）
 */
@Data
@Builder
@Schema(description = "知识库信息")
public class KbVO {

    @Schema(description = "知识库 ID")
    private Long id;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "文档数量")
    private Integer docCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
