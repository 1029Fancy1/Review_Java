package com.knowledgehub.module.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档实体
 *
 * 学习要点（Day 2）：
 * 1. parse_status 字段驱动异步解析状态机
 * 2. file_type 限制上传格式（PDF / Markdown）
 * 3. kb_id + user_id 双重隔离
 *
 * parse_status 状态说明：
 * 0 = 待解析（上传后初始状态）
 * 1 = 解析中（异步任务已开始）
 * 2 = 解析成功（chunk + embedding 入库完成）
 * 3 = 解析失败
 */
@Data
@TableName("document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long kbId;

    private Long userId;

    private String title;

    /** PDF / MARKDOWN */
    private String fileType;

    /** 本地存储路径 */
    private String filePath;

    /** 解析状态：0=待解析 1=解析中 2=解析成功 3=解析失败 */
    private Integer parseStatus;

    /** chunk 数量 */
    private Integer chunkCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
