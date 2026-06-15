package com.knowledgehub.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置类
 *
 * 学习要点（Day 8）：
 * 1. RedisTemplate vs StringRedisTemplate：
 *    - RedisTemplate<Object, Object>：默认 JDK 序列化，存进去是乱码，Redis CLI 不可读
 *    - StringRedisTemplate：key 和 value 都是 String，Redis CLI 可读，本项目用这个
 * 2. StringRedisTemplate 已经由 Spring Boot 自动配置创建，这里不需要手动定义
 * 3. 如果需要存对象，用 Jackson2JsonRedisSerializer 自定义序列化器
 *
 * 面试复盘：
 * - StringRedisTemplate 怎么存对象？→ 手写 JSON.toJSONString(obj)，取出来 JSON.parseObject(json, Class)
 * - 为什么不用默认的 JdkSerializationRedisSerializer？→ Redis CLI 不可读、不同 Java 版本可能不兼容
 */
@Configuration
public class RedisConfig {

    /**
     * StringRedisTemplate 已由 Spring Boot 自动配置提供，
     * 直接 @Autowired 注入使用即可。
     * 此处保留配置类用于未来扩展。
     */
}
