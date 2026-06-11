package com.knowledgehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KnowledgeHub AI - 个人知识库智能问答系统
 *
 * 技术栈：
 * - Spring Boot 3.3.x + Java 17
 * - MyBatis-Plus + PostgreSQL 16 + pgvector
 * - Redis 7.x（登录态、缓存、分布式锁、限流、排行榜等）
 * - bge-m3 Embedding + DeepSeek API
 * - Vue 3 + Element Plus 前端
 *
 * @author yourname
 */
@SpringBootApplication
@MapperScan("com.knowledgehub.module.*.mapper")
public class KnowledgeHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeHubApplication.class, args);
        System.out.println("========================================");
        System.out.println("  KnowledgeHub AI 启动成功！");
        System.out.println("  Knife4j 文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
