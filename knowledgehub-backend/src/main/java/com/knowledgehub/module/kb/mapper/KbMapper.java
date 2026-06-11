package com.knowledgehub.module.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledgehub.module.kb.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库 Mapper
 */
@Mapper
public interface KbMapper extends BaseMapper<KnowledgeBase> {
    // 基础 CRUD 由 BaseMapper 提供
}
