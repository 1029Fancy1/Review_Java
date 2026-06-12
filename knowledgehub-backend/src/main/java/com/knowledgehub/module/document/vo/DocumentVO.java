package com.knowledgehub.module.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档返回 VO
 */
@Data
@Builder
@Schema(description = "文档信息")
public class DocumentVO {

    @Schema(description = "文档 ID")
    private Long id;

    @Schema(description = "所属知识库 ID")
    private Long kbId;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "文件类型：PDF / MARKDOWN")
    private String fileType;

    @Schema(description = "解析状态：0=待解析 1=解析中 2=解析成功 3=解析失败")
    private Integer parseStatus;

    @Schema(description = "解析状态描述")
    private String parseStatusDesc;

    @Schema(description = "chunk 数量")
    private Integer chunkCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
