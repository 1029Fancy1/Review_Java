package com.knowledgehub.redis;

import java.time.Duration;

/**
 * 通用缓存 Service — Cache Aside 模式
 */
public interface CacheService {

    /**
     * 存缓存
     *
     * @param key   Redis Key
     * @param value 序列化后的 JSON 字符串
     * @param ttl   过期时间
     */
    void set(String key, String value, Duration ttl);

    /**
     * 取缓存，不存在返回 null
     */
    String get(String key);

    /**
     * 删缓存
     */
    void delete(String key);
}
