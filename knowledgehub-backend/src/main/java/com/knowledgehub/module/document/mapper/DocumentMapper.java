package com.knowledgehub.module.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledgehub.module.document.entity.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档 Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
    // 基础 CRUD 由 BaseMapper 提供
}
