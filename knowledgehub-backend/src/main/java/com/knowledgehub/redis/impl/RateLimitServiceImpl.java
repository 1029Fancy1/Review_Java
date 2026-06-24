package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.RateLimitService;

import io.lettuce.core.Limit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis 限流实现 — Day 14 核心（固定窗口 + Lua 原子操作）
 *
 * 学习路线（建议按顺序理解）：
 * 1. 为什么限流要用 Redis？→ 分布式环境需要集中计数
 * 2. 固定窗口怎么做？→ INCR + EXPIRE，每分钟重置
 * 3. 为什么必须用 Lua？→ INCR + EXPIRE 两步需要原子性
 * 4. 固定窗口有什么问题？→ 边界突刺（见下方详解）
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 面试连环问                                                │
 * ├─────────────────────────────────────────────────────────────┤
 * │ Q: 为什么限流用 Redis 而不是本地变量？                     │
 * │ A: 分布式部署时，多台机器各自计数会超过总限制。             │
 * │    Redis 集中计数，所有实例共享同一个计数器。               │
 * │                                                             │
 * │ Q: 为什么 INCR + EXPIRE 必须用 Lua 而不是两条命令？        │
 * │ A: 分两步执行：INCR 成功 → 程序崩溃/网络断开 →             │
 * │    EXPIRE 没执行 → key 永不过期 →                           │
 * │    用户下一分钟被永久限流（计数器永远不重置）               │
 * │    Lua 脚本在 Redis 中原子执行，要么全做要么全不做。        │
 * │                                                             │
 * │ Q: 固定窗口的"边界突刺"是什么？                           │
 * │ A: 假设限制 20次/分钟：                                    │
 * │    用户在 12:00:59 秒内发 20 次（窗口1的最后1秒）          │
 * │    用户在 12:01:00 秒内又发 20 次（窗口2的第1秒）          │
 * │    → 实际上 2 秒内发了 40 次，翻倍了限制！                 │
 * │    因为窗口边界是人为切分的，边界两侧的请求不互相感知       │
 * │                                                             │
 * │ Q: 滑动窗口怎么解决边界突刺？                              │
 * │ A: ZSet 存储每次请求的时间戳为 score，                    │
 * │    查询时删除窗口外的旧数据（ZREMRANGEBYSCORE）            │
 * │    统计剩余数量（ZCARD）→ 判断是否超限                     │
 * │    缺点：每次请求都要 ZADD + ZREMRANGEBYSCORE + ZCARD      │
 * │    占用内存比固定窗口多                                    │
 * │                                                             │
 * │ Q: 令牌桶和固定窗口有什么区别？                            │
 * │ A: 固定窗口每分钟重置，令牌桶匀速补充令牌。                 │
 * │    令牌桶允许短时突发（桶里积攒的令牌可以一次性用完）       │
 * │    固定窗口则是绝对均匀分布                                 │
 * └─────────────────────────────────────────────────────────────┘
 */
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_LUA = """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            if count > tonumber(ARGV[1]) then
                return 0
            end
            return 1
            """;

    @Override
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        // Lua限流逻辑
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(RATE_LIMIT_LUA);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
            script,
            List.of(key),
            String.valueOf(limit),
            String.valueOf(windowSeconds)
        );

        return Long.valueOf(1).equals(result);
    }
}