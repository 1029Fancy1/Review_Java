# KnowledgeHub AI — 个人知识库智能问答系统

基于 Spring Boot + Redis + PostgreSQL/pgvector + RAG 的个人知识库智能问答系统。

用户可以注册登录、创建知识库、上传 PDF/Markdown 文档，系统异步解析文档并通过 bge-m3 生成向量，基于 pgvector 检索，调用 DeepSeek API 生成带引用来源的答案。

---

## 技术栈

**后端：** Java 17 + Spring Boot 3.3 + MyBatis-Plus 3.5 + PostgreSQL 16 + pgvector + Redis 7 + JWT + Knife4j

**AI：** bge-m3 Embedding + DeepSeek API

**前端：** Vue 3 + Vite + Element Plus + Axios

**部署：** Docker Compose

---

## 快速开始

### 1. 启动基础设施

```bash
cd knowledgehub-backend
docker compose up -d
```

启动 PostgreSQL 16（含 pgvector）和 Redis 7。

### 2. 初始化数据库

```bash
docker exec -i knowledgehub-postgres psql -U postgres -d knowledgehub < src/main/resources/sql/schema.sql
```

### 3. 配置环境变量

复制示例配置文件：

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

按需修改 `application.yml` 中的 JWT 密钥和 DeepSeek API Key。

### 4. 启动应用

```bash
mvn spring-boot:run
```

### 5. 访问接口文档

```
http://localhost:8080/doc.html
```

---

## 项目结构

```text
src/main/java/com/knowledgehub/
├── common/              # 统一返回体、错误码、分页
├── exception/           # 全局异常处理
├── config/              # Spring Boot 配置
├── interceptor/         # 登录拦截器
├── aspect/              # AOP 请求日志
├── context/             # ThreadLocal 用户上下文
├── module/
│   ├── user/            # 用户模块（注册/登录）
│   ├── kb/              # 知识库模块（CRUD + 分页）
│   └── document/        # 文档模块（上传/删除）
├── redis/               # Redis 工程能力（锁/限流/缓存/排行榜）
└── utils/               # 工具类（BCrypt 加密）
```

---

## 接口清单

### 用户模块
- `POST /api/user/register` — 注册
- `POST /api/user/login` — 登录

### 知识库模块
- `POST /api/kb/create` — 创建知识库
- `GET /api/kb/list` — 分页列表
- `GET /api/kb/{id}` — 详情
- `PUT /api/kb/{id}` — 更新
- `DELETE /api/kb/{id}` — 删除

### 文档模块
- `POST /api/document/upload` — 上传文档
- `GET /api/document/list` — 文档列表
- `GET /api/document/{id}` — 详情
- `DELETE /api/document/{id}` — 删除

---

## 开发进度

| 周 | 内容 | 状态 |
|-----|------|------|
| 第 1 周 | Spring Boot 基础骨架 + 用户/知识库/文档 CRUD + AOP + Interceptor | ✅ |
| 第 2 周 | Redis P0 场景（token/验证码/缓存/锁/限流） | 🚧 |
| 第 3 周 | Redis P1 场景（排行榜/进度/额度/幂等）+ 异步任务 | ⏳ |
| 第 4 周 | RAG 入库链路（解析/chunk/embedding/入库） | ⏳ |
| 第 5 周 | RAG 问答链路（检索/prompt/DeepSeek/拒答/来源） | ⏳ |
| 第 6 周 | Vue 前端 + 联调 + 复盘 | ⏳ |
