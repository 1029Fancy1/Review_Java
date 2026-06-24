# Redis 八股 — KnowledgeHub AI 项目面试复习手册

> 持续更新中。每个知识点附：场景 + 原理 + 本项目实现 + 面试话术。

---

## 目录

- [一、Redis 基础认知](#一redis-基础认知)
- [二、缓存穿透 / 击穿 / 雪崩](#二缓存穿透--击穿--雪崩)
- [三、Cache Aside 模式](#三cache-aside-模式)
- [四、空值缓存（Null Cache）](#四空值缓存null-cache)
- [五、随机 TTL（防雪崩）](#五随机-ttl防雪崩)
- [六、Key 命名规范](#六key-命名规范)
- [七、StringRedisTemplate vs RedisTemplate](#七stringredistemplate-vs-redistemplate)
- [八、分布式锁](#八分布式锁)
- [九、限流](#九限流)

---

## 一、Redis 基础认知

### 1.1 为什么要用缓存？

**场景：** 1000 个用户同时请求知识库列表，每次返回的数据完全相同。

| | MySQL/PostgreSQL | Redis |
|---|-----------------|-------|
| 数据在哪 | 磁盘（SSD） | **内存** |
| 单次操作 | 1-5ms | **0.1ms**（亚毫秒级） |
| 并发模型 | 连接池 + 多线程 | 单线程事件驱动 |
| QPS | ~10,000 | **~100,000** |

**核心价值：** 把数据从磁盘搬到内存，1ms → 0.1ms，DB 压力从 N 次降到 1 次。

### 1.2 Redis 为什么快？

1. **纯内存操作** — 没有磁盘 I/O
2. **单线程模型** — 避免上下文切换和锁竞争（Redis 6.0+ 网络 I/O 多线程，命令执行仍是单线程）
3. **IO 多路复用** — epoll 一个线程处理数万个连接
4. **高效底层数据结构** — SDS（简单动态字符串）、ziplist（压缩列表）、skiplist（跳表）等

### 1.3 Redis 的五种基本数据结构

| 结构 | 核心命令 | 本项目场景 |
|------|---------|-----------|
| String | SET/GET/DEL/INCR/SETNX | 缓存、token、验证码、限流、分布式锁 |
| Hash | HSET/HGET/HINCRBY/HGETALL | 文档解析进度 |
| List | LPUSH/RPOP/LRANGE | — |
| Set | SADD/SISMEMBER/SINTER | — |
| ZSet | ZADD/ZINCRBY/ZREVRANGE | 热门文档排行、最近访问 |

---

## 二、缓存穿透 / 击穿 / 雪崩

Redis 面试**必问**的三兄弟问题。

### 2.1 缓存穿透（Penetration）

**现象：** 查询一个数据库里根本不存在的数据，每次请求都穿透 Redis 直接打到 DB。

**为什么会发生：**
```
请求 docId=99999（不存在）
  → Redis miss（没缓存过这个 key）
  → MySQL miss（DB 里也没有）
  → 没写缓存（查不到，觉得没什么可写的）
  → 下次请求又来 → 继续穿透
```

**本项目解决方案：空值缓存**
- 将「不存在」也当作一种有效结果缓存：`SET doc:detail:999 "__NULL__"`
- TTL 设 5 分钟（短 TTL，防攻击又不影响业务）
- 命中 `"__NULL__"` 直接抛异常，不再查 DB

**其他方案对比：**

| 方案 | 原理 | 优缺点 |
|------|------|--------|
| 空值缓存 | 缓存 null 标记 | 简单，本项目使用；浪费少量内存 |
| 布隆过滤器 | 先判断 key 是否可能存在 | 省内存，有误判率；实现复杂 |
| 参数校验 | ID < 0 或格式不对直接拒绝 | 兜底方案，不能防合法 ID 攻击 |

### 2.2 缓存击穿（Breakdown）

**现象：** 一个热点 key 在过期的那一瞬间，大量并发请求同时打到 DB。

**为什么会发生：**
```
热点文档 doc:detail:42，TTL 30 分钟
10:30:00  缓存过期
10:30:00.001  请求A → miss → 查 MySQL（耗时 500ms）
10:30:00.002  请求B → miss → 查 MySQL（又查一遍）
10:30:00.003  请求C → miss → 查 MySQL（又查一遍）
...几百个请求同时查同一条数据
```

**本项目解决方案（Day 13 待实现）：分布式锁**
- 第一个请求：`SET lock:doc:parse:42 uuid NX EX 10` 拿到锁 → 查 DB → 写缓存 → 释放锁
- 其他请求：拿不到锁 → 等待/返回旧值

**其他方案：**
- 逻辑过期（永不过期，后台线程异步刷新）
- 互斥锁（跟本项目方案一样）

### 2.3 缓存雪崩（Avalanche）

**现象：** 大量 key 同时过期，所有请求瞬间全打 DB，DB 压力飙升甚至宕机。

**为什么会发生：**
```
1000 个 key 都是在 10:00 写入的，TTL 都是 30 分钟
10:30 → 1000 个 key 同时过期 → 1000 个请求同时回源 DB
```

**本项目解决方案：随机 TTL**
- 文档详情缓存 TTL：30~60 分钟之间取随机值
- 知识库列表缓存 TTL：10~30 分钟之间取随机值
- 每个 key 的过期时间被随机打散

**其他方案：**
- 多级缓存（本地 Caffeine → Redis → DB，每一层兜底）
- 热点数据永不过期（但要考虑内存淘汰策略）
- 限流降级（超过阈值直接返回兜底数据或错误）

### 2.4 三兄弟对比速查表

| 问题 | 一句话 | Redis 状态 | 本项目解法 |
|------|--------|-----------|-----------|
| 穿透 | 查不存在的数据 | 没有这个 key | `__NULL__` 空值缓存 5min |
| 击穿 | 热点过期 + 高并发 | key 刚好过期 | 分布式锁排队（Day 13） |
| 雪崩 | 大量 key 同时过期 | 批量同时消失 | TTL 随机化 30~60min |

---

## 三、Cache Aside 模式

### 3.1 核心思想

> **应用程序自己管理缓存，DB 是唯一真相源（Single Source of Truth）。缓存只是 DB 的附属品。**

### 3.2 读路径（本项目 listByPage / getDetail 均遵循）

```
1. 先查 Redis  — GET key
2. 命中 → 直接返回  （不走 DB，99% 的情况）
3. 未命中 → 查 MySQL
4. 写 Redis  — SET key value EX ttl
5. 返回结果
```

### 3.3 写路径（create / update / delete 均遵循）

```
1. 先更新 MySQL  — UPDATE/INSERT/DELETE
2. 再删除 Redis  — DEL key（不是更新缓存！）
3. 等下次读请求时自动重建
```

### 3.4 为什么删缓存而不是更新缓存？

| 维度 | 更新缓存 | 删除缓存（本项目） |
|------|---------|-----------------|
| 操作成本 | 查 DB + 拼 JSON + SET | 一次 DEL |
| 并发写 | N 次写操作 | 1 次 DEL |
| 资源浪费 | 更新后没人读就白做了 | 懒加载，谁用谁建 |
| 复杂度 | 需要知道缓存的数据结构 | 不需要知道 |

### 3.5 为什么先更新 DB 再删缓存？（面试高频 🔥）

```
❌ 先删缓存，再更新 DB 的风险：

  时间线 ──────────────────────────▶
  请求A（更新）              请求B（读取）
  ① DEL cache               
                             ② GET cache → miss（A刚删了）
                             ③ SELECT → 读到旧数据
                             ④ SET cache(旧数据) ← 脏数据！
  ⑤ UPDATE DB（新数据）
  
  最终：DB 是新数据，Redis 是旧数据 → 不一致！


✅ 先更新 DB，再删缓存：

  时间线 ──────────────────────────▶
  请求A（更新）              请求B（读取）
  ① UPDATE DB（新数据）
                             ② GET cache → 可能命中旧数据
                             ③ 返回旧数据（此时刻的正确状态）
  ④ DEL cache
  
  最终：B 读到的是旧版本，但那是请求发出时刻的合法状态
        删缓存后，下一个请求就重建新数据了 → 最终一致
```

### 3.6 删缓存失败了怎么办？

| 方案 | 等级 | 说明 |
|------|------|------|
| TTL 兜底 | 基础（本项目采用） | 所有缓存都设 TTL，过期后自动一致 |
| Canal + binlog | 生产级 | 监听 MySQL binlog，数据变更异步补偿删除 |
| 消息队列重试 | 生产级 | DEL 失败 → 写入 MQ → 消费者重试 |

---

## 四、空值缓存（Null Cache）— Day 12

### 4.1 要解决的问题

缓存穿透：攻击者反复请求不存在的 docId=99999，每次穿透到 DB。

### 4.2 做法（DocumentServiceImpl.getDetail）

```java
// 1. 查缓存
String cached = cacheService.get("doc:detail:999");

// 2. 命中了空值标记 → 直接返回"不存在"，不查 DB
if (NULL_MARKER.equals(cached)) {
    throw new BusinessException(ErrorCode.DOC_NOT_FOUND);
}

// 3. 未命中 → 查 DB
Document doc = baseMapper.selectById(999);

// 4. DB 也没有 → 写入空值缓存（5 分钟短 TTL）
if (doc == null) {
    cacheService.set(key, "__NULL__", Duration.ofMinutes(5));
    throw new BusinessException(ErrorCode.DOC_NOT_FOUND);
}
```

### 4.3 效果对比

```
没有空值缓存：
  请求1: miss → DB → 不存在 → 抛异常 → 没写缓存
  请求2: miss → DB → 不存在 → 抛异常  ← DB 每次都被打

有空值缓存：
  请求1: miss → DB → 不存在 → SET "__NULL__" EX 300 → 抛异常
  请求2: 命中 "__NULL__" → 直接抛异常  ← DB 毫发无伤
  ...5 分钟内所有请求都被 Redis 拦截...
```

### 4.4 为什么空值缓存 TTL 设 5 分钟？

- **太长**（30 分钟）：如果管理员重建了同一 ID，用户要等 30 分钟才能访问
- **太短**（10 秒）：攻击者 10 秒后又能穿透，没意义
- **5 分钟**：MySQL 自增 ID 不会重用，不存在的 ID 不会突然存在；足够防攻击，不阻塞正常业务

---

## 五、随机 TTL（防雪崩）— Day 12

### 5.1 要解决的问题

缓存雪崩：批量 key 同时过期，瞬间流量全打 DB。

### 5.2 做法

```java
// 不要这样写：
cacheService.set(key, json, Duration.ofMinutes(30));  // ❌ 所有 key 同一 TTL

// 要这样写：
int TTL_MIN = 30 * 60;  // 30 分钟
int TTL_MAX = 60 * 60;  // 60 分钟
Duration randomTtl = Duration.ofSeconds(
    TTL_MIN + RANDOM.nextInt(TTL_MAX - TTL_MIN + 1)
);
cacheService.set(key, json, randomTtl);  // ✅ 每个 key 的 TTL 随机
```

### 5.3 效果示意

```
固定 TTL 30min：  ████████████████████████████████  ← 同时过期 → 雪崩 💥

随机 TTL 30~60min：
  key1: ████████████████████████████  (11:48 过期)
  key2: ████████████████████████████████████████████  (12:25 过期)
  key3: ████████████████████████  (11:42 过期)
  key4: ██████████████████████████████████████  (12:10 过期)
  ← 过期时间被随机打散，请求平滑分布 →
```

### 5.4 随机范围怎么选？

- **上限**：数据多久一定需要更新？（文档详情最长容忍 1 小时 stale）
- **下限**：缓存命中率和 DB 压力的平衡（至少 10 分钟才有缓存意义）
- **范围跨度**：越大越平滑，但不能大到你无法接受的数据延迟

---

## 六、Key 命名规范 — Day 8

### 6.1 设计原则

```
命名模板：业务模块:场景:标识参数

示例：
  login:token:{token}           — 登录态
  captcha:email:{email}         — 邮箱验证码
  captcha:cooldown:{email}      — 验证码冷却期
  doc:detail:{docId}            — 文档详情缓存
  kb:list:{userId}              — 知识库列表缓存
  lock:doc:parse:{docId}        — 文档解析锁
  rate:chat:{userId}:{minute}   — 问答限流
  rank:doc:hot                  — 热门文档排行
```

### 6.2 本项目两层设计

```java
// 第一层：常量模板（RedisKey.java）
public static final String DOC_DETAIL = "doc:detail:%s";

// 第二层：构建器（RedisKeyBuilder.java）
public static String docDetail(Long docId) {
    return String.format(RedisKey.DOC_DETAIL, docId);
    // → "doc:detail:42"
}
```

**好处：**
- `RedisKey.java` — 一张文件看全系统所有 Key（全局视图）
- `RedisKeyBuilder.java` — 所有 key 的构建逻辑统一入口
- 改命名规范只需改 `RedisKey` 常量，不用全局搜索替换
- 知道有哪些 key，方便运维排查和清理

### 6.3 面试话术

> "Redis Key 命名遵循 `业务:模块:标识` 的格式，比如 `doc:detail:42`。所有 Key 模板集中在一个常量类里管理，配合 KeyBuilder 构建最终 key。好处是可读性强、方便运维排查、改规范只改一处。"

---

## 七、StringRedisTemplate vs RedisTemplate — Day 8

### 7.1 核心差异

| | RedisTemplate<K,V> | StringRedisTemplate |
|---|-------------------|-------------------|
| 泛型 | `<Object, Object>` | `<String, String>` |
| Key 序列化 | JDK 序列化（二进制） | String |
| Value 序列化 | JDK 序列化（二进制） | String |
| Redis CLI 查看 | `\xac\xed\x00\x05t\x00\x06...` | `{"id":42,"title":"xxx"}` |
| 存对象 | `redisTemplate.opsForValue().set(k, obj)` 自动序列化 | `stringRedisTemplate.opsForValue().set(k, JSON.toJSONString(obj))` 手动序列化 |
| 跨语言 | ❌ 绑定 JDK 版本 | ✅ 纯文本 |

### 7.2 本项目选择及理由

**选 StringRedisTemplate。**

**理由：**
1. Redis CLI `GET key` 直接看 JSON → 出问题能快速排查
2. 不依赖 JDK 序列化 → 各环境兼容
3. JSON 序列化自己控制 → 想用什么库（fastjson / jackson）自己定

### 7.3 面试话术

> "我选 StringRedisTemplate，因为 RedisTemplate 默认 JDK 序列化，存进去是二进制乱码，运维在命令行看不到内容。我的项目存的是 JSON 字符串，redis-cli 里直接 `GET` 就能看到，方便排查。对象序列化用手动 `JSON.toJSONString` / `JSON.parseObject`，完全可控。"

---

## 八、分布式锁 — Day 13

### 8.1 要解决的问题

**单机锁在分布式环境下失效：**

```java
// 单机环境：synchronized 或 ReentrantLock 完全够用
synchronized(this) {
    // 只有一个线程能执行
    parseDocument(docId);
}

// 分布式环境：两台机器各有一个 JVM，各管各的锁
// 机器A 的 synchronized 管不到 机器B 的线程
// → 同一个文档可能被两台机器同时解析
```

**本项目场景：** 文档异步解析，多台服务器可能同时触发对同一文档的解析。

### 8.2 核心三要素

| 要素 | 含义 | 怎么做 |
|------|------|--------|
| **互斥** | 同一时刻只有一个客户端能拿到锁 | `SET key value NX`（Not eXists） |
| **防死锁** | 拿到锁的进程崩溃后，锁必须自动释放 | `SET key value NX EX ttl`（加过期时间） |
| **身份校验** | 释放锁时只能释放自己持有的 | value=UUID + Lua 比对后删除 |

### 8.3 加锁：SET NX EX

```bash
# Redis 命令（原子执行）：
SET lock:doc:parse:42 "uuid-abc-123" NX EX 600

# 解读：
# NX  → Not eXists：只有 key 不存在时才设置成功（= 拿到锁）
# EX  → Expire：10 分钟后自动删除（= 防死锁）
# value → UUID：记下"这锁是我加的"
```

**为什么 NX 和 EX 必须在同一条命令？**

```
❌ 分两步执行：
  SET lock:doc:parse:42 uuid NX  → 成功！拿到锁
  （进程崩溃 💥）
  EXPIRE lock:doc:parse:42 600   → 永远没机会执行
  结果：锁永远不释放 → 死锁！

✅ 原子执行：
  SET lock:doc:parse:42 uuid NX EX 600
  结果：要么全成功（加锁+设TTL），要么全失败
```

### 8.4 释放：Lua 脚本校验身份后删除

```lua
-- Lua 脚本在 Redis 服务端原子执行
if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('DEL', KEYS[1])  -- 是我加的锁 → 删除
else
    return 0                            -- 不是我的锁 → 不动
end
```

**为什么释放必须用 Lua？**

```
❌ Java 代码分两步：
  String value = redis.get(lockKey);
  if (value.equals(myUUID)) {
      // ← 这个间隙里，锁刚好过期！
      //    线程B 拿到锁（新的UUID）
      redis.delete(lockKey);  // 删掉的是 B 的锁！💥
  }

✅ Lua 脚本：
  Redis 执行 Lua 期间，其他所有命令排队等待
  GET 和 DEL 之间不会有任何命令插入
```

### 8.5 为什么 value 用 UUID？

```
场景推演：

  线程A: 拿到锁，UUID=aaa
  线程A: 执行任务...（超时了）
  锁自动过期（TTL 到了）
  线程B: 拿到锁，UUID=bbb
  线程A: 任务完成，准备释放锁

  不校验 UUID：A 直接 DEL → 删掉了 B 的锁 → B 被误伤 💥
  校验 UUID：A GET 发现 value=bbb ≠ aaa → 不删 → B 安全 ✅
```

### 8.6 完整面试话术

> "我实现的分布式锁有三个关键设计：第一，SET NX EX 原子加锁，保证互斥和防死锁；第二，value 用 UUID，释放时校验身份，防止误删别人的锁；第三，释放用 Lua 脚本，保证 GET 判断 + DEL 删除是原子的。TTL 设 10 分钟，是预估解析时间的 2-3 倍。如果要生产级，会用 Redisson 的看门狗机制自动续期。"

### 8.7 延伸：Redisson 看门狗（Watch Dog）

手写锁的痛点：TTL 是拍脑袋设的，设短了业务没执行完就过期，设长了死锁等太久。

Redisson 的方案：加锁时不设 TTL（或设默认 30s），后台定时任务每隔 10s 检查——如果锁还在、线程还活着 → 续期到 30s。业务完成后主动释放 + 取消续期。

**面试加分项：** 知道手写锁的局限，知道 Redisson 的存在和原理。

---

## 九、限流 — Day 14

### 9.1 要解决的问题

**场景：** 问答接口调用 DeepSeek API，每次调用都有成本。需要限制每个用户每分钟最多 20 次请求，防止滥用。

**为什么不用本地变量计数？**
```
机器A: count=15（本机计数）
机器B: count=10（本机计数）
→ 实际总量 = 25，超过 20 的限制
→ 分布式环境必须用 Redis 集中计数
```

### 9.2 固定窗口（本项目方案）

```
Key 设计：rate:chat:{userId}:{yyyyMMddHHmm}
例：      rate:chat:1:202606221430

时间窗口：每分钟一个 key
         rate:chat:1:202606221430 → 2026-06-22 14:30 这一分钟
         rate:chat:1:202606221431 → 2026-06-22 14:31 这一分钟
```

**流程：**
```
用户请求 →
  1. INCR rate:chat:1:202606221430  → count
  2. 如果 count == 1 → EXPIRE 60s（首次请求设过期）
  3. 如果 count > 20 → 返回"请求过于频繁"
  4. 否则 → 允许通过
```

### 9.3 Lua 脚本

```lua
local count = redis.call('INCR', KEYS[1])   -- 计数+1
if count == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[2])  -- 首次设过期
end
if count > tonumber(ARGV[1]) then
    return 0  -- 超限
end
return 1      -- 允许
```

### 9.4 为什么必须用 Lua？

```
❌ Java 代码分两步：
  Long count = redis.opsForValue().increment(key);
  // ← 如果这里程序崩溃，EXPIRE 永远不会执行
  //    key 永不过期 → 计数器永远不重置
  //    → 用户下一分钟还在被限流 → 事实上被永久封禁！
  redis.expire(key, Duration.ofSeconds(60));

✅ Lua 脚本：
  INCR → 判断 → EXPIRE 在 Redis 服务端原子完成
  要么全做，要么全不做
```

### 9.5 为什么 count==1 才设 EXPIRE？

```
❌ 每次 INCR 都重置 EXPIRE：
  key 过期时间是 1 分钟后
  攻击者在第 59 秒发请求 → EXPIRE 被重置到又 60 秒后
  → 第 59 秒再发一次 → 又重置
  → 窗口永远不关闭 → 限流失效

✅ 只在首次设 EXPIRE：
  key 在第 1 次请求时设 60 秒过期
  之后无论多少请求，过期时间不变
  → 1 分钟后窗口准时关闭 → 计数器归零
```

### 9.6 四种限流算法对比

| 算法 | 原理 | 优点 | 缺点 | Redis 实现 |
|------|------|------|------|-----------|
| **固定窗口**（本项目） | 每分钟一个计数器 | 简单 | 边界突刺 | INCR + EXPIRE |
| **滑动窗口** | ZSet 存每次请求时间戳 | 精确 | 内存大 | ZADD + ZREMRANGE + ZCARD |
| **令牌桶** | 匀速生成令牌，请求消耗令牌 | 允许突发 | 实现稍复杂 | Lua 维护令牌数 |
| **漏桶** | 请求进队列，恒定速率出队 | 绝对平滑 | 无法应对突发 | List + 定时任务 |

### 9.7 固定窗口的边界突刺（Critical Flaw）

```
限制：20次/分钟

12:00:59  用户发 20 次（落入窗口 12:00）
12:01:00  用户发 20 次（落入窗口 12:01）
→ 实际 2 秒内发了 40 次，是限制的 2 倍！
```

**滑动窗口解法：**
```bash
# 每次请求：
ZADD rate:chat:1 1760423400123 req_001  # score=当前时间戳
ZREMRANGEBYSCORE rate:chat:1 0 1760423340123  # 删除 60 秒前的
ZCARD rate:chat:1  # 统计剩余 → 判断超限
```

### 9.8 面试话术

> "我用固定窗口实现限流，核心是用 Lua 脚本把 INCR + 判断首次 + EXPIRE + 阈值判断打包原子执行。固定窗口的优势是实现简单，但存在边界突刺问题——窗口切换的瞬间可能出现 2 倍流量。面试时我会主动提这个缺陷，并说明滑动窗口通过 ZSet 时间戳来解决。生产环境中，令牌桶更常用，因为它允许短时突发，更符合实际流量模型。"


---

## 附录：面试问题速查表

### 基础必问题
- [x] 为什么用 Redis 做缓存？Redis 为什么快？
- [x] 缓存穿透/击穿/雪崩分别是什么？怎么解决？
- [x] Cache Aside 模式怎么做的？
- [x] 先删缓存还是先更新数据库？为什么？
- [x] StringRedisTemplate 和 RedisTemplate 有什么区别？

### 进阶题
- [x] 空值缓存做了什么？TTL 为什么设 5 分钟？
- [x] 随机 TTL 的原理？范围怎么选？
- [x] Redis Key 命名规范是什么？
- [x] 删缓存失败了怎么办？有哪些兜底方案？
- [x] 分布式锁怎么实现？SET NX EX + Lua 的原理？（Day 13）
- [x] 限流怎么实现？固定窗口 vs 滑动窗口？（Day 14）

### 数据结构题
- [ ] ZSet 底层结构？为什么用跳表？（Day 15）
- [ ] Hash 和 String 存对象有什么区别？（Day 18）
- [ ] Redis 过期策略？内存淘汰策略？

---

> 最后更新：2026-06-22 — Day 14 完成（分布式锁 + 限流）
