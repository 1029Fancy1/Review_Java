-- ==================== KnowledgeHub AI 建表脚本 ====================
-- 使用方式：在 PostgreSQL 中执行此脚本
-- psql -U postgres -d knowledgehub -f schema.sql
--
-- 学习要点（Day 2）：
-- 1. BIGSERIAL = BIGINT + 自增序列，PostgreSQL 特有
-- 2. 为什么用 BIGINT 而不是 INT？数据量大时避免溢出
-- 3. deleted 字段实现逻辑删除，MyBatis-Plus @TableLogic 自动处理
-- 4. kb_version 字段用于缓存失效，Day 17 会用到

-- ==================== 用户表 ====================
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100),
    status      SMALLINT     DEFAULT 1,   -- 1=正常 0=禁用
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT     DEFAULT 0    -- 逻辑删除 0=未删除 1=已删除
);

-- ==================== 知识库表 ====================
CREATE TABLE IF NOT EXISTS knowledge_base (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    kb_version  INT          DEFAULT 1,   -- 知识库版本号，用于缓存失效
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT     DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_kb_user_id ON knowledge_base(user_id);

-- ==================== 文档表 ====================
CREATE TABLE IF NOT EXISTS document (
    id           BIGSERIAL PRIMARY KEY,
    kb_id        BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL,
    file_type    VARCHAR(20),              -- PDF / MARKDOWN
    file_path    VARCHAR(500),             -- 本地存储路径
    parse_status SMALLINT     DEFAULT 0,   -- 0=待解析 1=解析中 2=解析成功 3=解析失败
    chunk_count  INT          DEFAULT 0,   -- chunk 数量
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted      SMALLINT     DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_document_kb_id ON document(kb_id);
CREATE INDEX IF NOT EXISTS idx_document_user_id ON document(user_id);
