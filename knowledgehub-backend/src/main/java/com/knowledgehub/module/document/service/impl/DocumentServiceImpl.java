package com.knowledgehub.module.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledgehub.module.document.entity.Document;
import com.knowledgehub.module.document.mapper.DocumentMapper;
import com.knowledgehub.module.document.service.DocumentService;
import org.springframework.stereotype.Service;

/**
 * 文档 Service 实现
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
    // Day 5 将在此实现文件上传、解析触发等核心逻辑
}
