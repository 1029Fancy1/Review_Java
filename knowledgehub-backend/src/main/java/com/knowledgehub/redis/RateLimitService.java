package com.knowledgehub.redis;

/**
 * Redis 限流 Service — Day 14
 *
 * 解决场景：问答接口限制每个用户每分钟的请求次数，防止滥用 LLM API
 *
 * 方案：固定窗口（Fixed Window）限流
 * - key = rate:chat:{userId}:{yyyyMMddHHmm}（精确到分钟）
 * - 每次请求 INCR +1
 * - 首次请求设 EXPIRE 60s（1 分钟后窗口自动重置）
 * - 超过阈值（如 20 次/分钟）拒绝请求
 *
 * 为什么用 Lua？
 * INCR 和 EXPIRE 是两步操作，分开执行可能：
 * 1. INCR 后崩溃 → EXPIRE 没执行 → key 永不过期 → 用户永远被限流
 * 2. Lua 保证 INCR → check first → EXPIRE 三步骤原子执行
 *
 * 面试复盘：
 * 1. 固定窗口的缺点？
 *    → 边界问题：用户在 12:00:59 和 12:01:00 各发 20 次，1 秒内发出 40 次
 *    → 滑动窗口可以解决（但实现更复杂）
 * 2. 四种限流算法对比？
 *    固定窗口：简单，有边界问题
 *    滑动窗口：精确，内存占用高
 *    令牌桶：允许突发，平滑限流
 *    漏桶：绝对平滑，强制恒定速率
 */
public interface RateLimitService {

    /**
     * 检查是否允许本次请求
     *
     * @param key           限流 key（如 rate:chat:1:202606221200）
     * @param limit         时间窗口内最大允许次数（如 20）
     * @param windowSeconds 时间窗口秒数（如 60 秒）
     * @return true=允许，false=超过限制
     */
    boolean isAllowed(String key, int limit, int windowSeconds);
}
