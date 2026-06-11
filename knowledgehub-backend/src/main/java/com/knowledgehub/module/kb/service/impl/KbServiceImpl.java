package com.knowledgehub.module.kb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledgehub.module.kb.entity.KnowledgeBase;
import com.knowledgehub.module.kb.mapper.KbMapper;
import com.knowledgehub.module.kb.service.KbService;
import org.springframework.stereotype.Service;

/**
 * 知识库 Service 实现
 */
@Service
public class KbServiceImpl extends ServiceImpl<KbMapper, KnowledgeBase> implements KbService {
    // Day 4 将在此实现知识库 CRUD 核心逻辑
}
