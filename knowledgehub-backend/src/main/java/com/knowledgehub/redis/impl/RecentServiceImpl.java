package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.RecentService;
import com.knowledgehub.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentServiceImpl implements RecentService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void recordVisit(Long userId, Long kbId) {
        //ZADD记录访问
        String key = RedisKeyBuilder.recentKb(userId);
        stringRedisTemplate.opsForZSet().add(key, String.valueOf(kbId),System.currentTimeMillis());
        stringRedisTemplate.opsForZSet().remove(key, 0,-101);
    }

    @Override
    public List<Long> getRecent(Long userId, int n) {
        // TODO: ZREVRANGE 获取最近访问
        String key = RedisKeyBuilder.recentKb(userId);
        Set<String> kbIds = stringRedisTemplate.opsForZSet()
        .reverseRange(key, 0, n-1);

        if (kbIds == null || kbIds.isEmpty()) {
            return Collections.emptyList();
        }
        return kbIds.stream().map(Long::valueOf).collect(Collectors.toList());
    }
}
