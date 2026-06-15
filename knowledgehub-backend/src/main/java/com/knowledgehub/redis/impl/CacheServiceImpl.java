package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 通用缓存 Service 实现
 *
 * 学习要点（Day 11）：
 * 1. Cache Aside 模式：读 → 查缓存 → 命中返回 → 未命中查 DB → 写缓存 → 返回
 * 2. 更新策略：先更新 DB，再删除缓存（不是更新缓存！）
 * 3. 为什么删缓存而不是更新缓存？
 *    → 更新成本高（要查 DB 再写缓存），而且可能更新了但没人读
 *    → 删除后等下次读再重建，更轻量
 *
 * 面试复盘：
 * - 先删缓存还是先更新 DB？
 *   → 先更新 DB 再删缓存。先删缓存的话，在删和更新 DB 之间可能有其他请求读到旧数据写回缓存
 * - 删缓存失败了怎么办？
 *   → 设置 TTL 兜底，缓存自己过期。高要求场景用 Canal + binlog 异步补偿
 */
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * TODO: 请你手敲完成缓存写入。
     *
     * 学习目标：理解 opsForValue().set() 设置 TTL
     *
     * 参考实现：
     * stringRedisTemplate.opsForValue().set(key, value, ttl);
     */
    @Override
    public void set(String key, String value, Duration ttl) {
        //缓存写入
        stringRedisTemplate.opsForValue().set(key, value,ttl);
    }

    /**
     * TODO: 请你手敲完成缓存读取。
     *
     * 学习目标：理解 opsForValue().get()，key 不存在返回 null
     *
     * 参考实现：
     * return stringRedisTemplate.opsForValue().get(key);
     */
    @Override
    public String get(String key) {
        //缓存读取
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * TODO: 请你手敲完成缓存删除。
     *
     * 学习目标：理解更新数据库后删除缓存
     *
     * 参考实现：
     * stringRedisTemplate.delete(key);
     */
    @Override
    public void delete(String key) {
        //缓存删除
        stringRedisTemplate.delete(key);
    }
}
