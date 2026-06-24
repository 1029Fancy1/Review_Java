package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.LockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LockServiceImpl implements LockService {

    private final StringRedisTemplate stringRedisTemplate;

    //Lua 脚本原子释放锁
    private static final String UNLOCK_LUA = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            else
                return 0
            end
            """;

    //SET NX EX 加锁逻辑。
    @Override
    public boolean tryLock(String lockKey, String lockValue, Duration ttl) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl);
        return Boolean.TRUE.equals(success);
    }

    //Lua 脚本释放锁逻辑。
    @Override
    public boolean unlock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(UNLOCK_LUA);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
            script,
            List.of(lockKey),
            lockValue
        );
        return Long.valueOf(1).equals(result);
    }

    // 检查锁是否还存在
    @Override
    public boolean isLocked(String lockKey) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }
}
