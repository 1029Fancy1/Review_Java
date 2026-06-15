package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.RedisKeyBuilder;
import com.knowledgehub.redis.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token Service 实现 — 基于 Redis String 管理登录态
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate stringRedisTemplate;

    //token 保存逻辑。
    @Override
    public void save(String token, Long userId) {
        String key = RedisKeyBuilder.loginToken(token);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(userId),Duration.ofDays(7));
    }

    //token 校验逻辑。
    @Override
    public Long getUserId(String token) {
        String key = RedisKeyBuilder.loginToken(token);
        String userIdStr = stringRedisTemplate.opsForValue().get(key);
        if (userIdStr == null) {
            return null; //token不存在或者已经过期
        }
        return Long.valueOf(userIdStr);
    }

    
     //token 删除逻辑。
    @Override
    public void delete(String token) {
        String key = RedisKeyBuilder.loginToken(token);
        stringRedisTemplate.delete(key);
    }
}
