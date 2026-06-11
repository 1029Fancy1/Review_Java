package com.knowledgehub.module.kb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库实体
 *
 * 学习要点（Day 2）：
 * 1. user_id 实现多租户数据隔离
 * 2. kb_version 字段用于缓存失效（Day 17 问答缓存时使用）
 * 3. 每次更新知识库时，kb_version + 1，缓存 key 自动变化
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID（数据隔离） */
    private Long userId;

    private String name;

    private String description;

    /** 知识库版本号，每次更新 +1 */
    private Integer kbVersion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
