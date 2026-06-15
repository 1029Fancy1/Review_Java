package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.CaptchaService;
import com.knowledgehub.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 验证码 Service 实现 — Redis SETEX 存验证码
 *
 * 学习要点（Day 10）：
 * 1. Redis SETEX 原子操作：设置值 + 设置过期时间，一条命令完成
 * 2. 为什么验证码用 Redis？
 *    → 临时数据，天然 TTL，过期自动清理，不用扫表
 * 3. 防重复发送：cooldown key 60s TTL，发送前检查，存在则拒绝
 * 4. 校验后删除：验证码一次性使用，防止重复利用
 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void generate(String email) {
        //验证码生成
        String cooldownKey = RedisKeyBuilder.captchaCooldown(email);
        Boolean locked = stringRedisTemplate.opsForValue()
        .setIfAbsent(cooldownKey,"1", Duration.ofSeconds(60));
        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("验证码发送频繁，60秒后重试");
        }

        //生成 6 位随机验证码
        String code = String.format("%06d", (int)(Math.random()*1000000));

        String captchaKey = RedisKeyBuilder.captchaEmail(email);
        stringRedisTemplate.opsForValue().set(captchaKey, code,Duration.ofMinutes(5));
        System.out.println("验证码"+email+code);
    }
 

    @Override
    public boolean verify(String email, String code) {
        //验证码校验
        String captchaKey = RedisKeyBuilder.captchaEmail(email);
        String storedCode = stringRedisTemplate.opsForValue().get(captchaKey);

        if(storedCode == null){
            return false;
        }

        if (storedCode.equals(code)) {
            stringRedisTemplate.delete(captchaKey);
            return true;
        }
    
        return false;
    }
}
