package com.knowledgehub.redis;

import java.time.Duration;

/**
 * Redis 分布式锁 Service — Day 13
 *
 * 解决场景：文档异步解析时，防止多个线程重复解析同一文档（缓存击穿防护同理）
 *
 * 核心技术：
 * - SET NX EX：原子加锁（NX = 不存在才写，EX = 设置过期时间）
 * - UUID value：标记"这个锁是我加的"（防止释放别人的锁）
 * - Lua 脚本释放：GET + DEL 两步必须原子执行
 *
 * 面试复盘：
 * 1. 为什么 SET NX 和 EX 要一起执行？
 *    → 分两步的话，SET NX 成功后进程崩溃，锁永远不释放（死锁）
 * 2. 为什么 value 用 UUID？
 *    → 释放锁时需要验证"是不是我加的锁"，防止误删别人的锁
 * 3. 为什么释放用 Lua？
 *    → GET 判断 + DEL 删除是两步操作，不是原子的，必须用 Lua 打包
 */
public interface LockService {

    /**
     * 尝试加锁（非阻塞）
     *
     * @param lockKey   锁的 key（如 lock:doc:parse:42）
     * @param lockValue 锁的持有者标识（UUID，用于释放时校验）
     * @param ttl       锁的过期时间（防死锁，如 10 分钟）
     * @return true=加锁成功，false=锁已被别人持有
     */
    boolean tryLock(String lockKey, String lockValue, Duration ttl);

    /**
     * 释放锁（只有持有者才能释放）
     *
     * @param lockKey   锁的 key
     * @param lockValue 加锁时用的 UUID，必须匹配才能释放
     * @return true=释放成功，false=锁不存在或不属于当前持有者
     */
    boolean unlock(String lockKey, String lockValue);

    /**
     * 检查锁是否存在（调试用）
     */
    boolean isLocked(String lockKey);
}
