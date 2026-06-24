package com.knowledgehub.redis.impl;

import com.knowledgehub.redis.RankingService;
import com.knowledgehub.redis.RedisKey;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//ZSet热门文档排行榜
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void incrHot(Long docId) {
        //ZINCRBY增加热度
        stringRedisTemplate.opsForZSet().
        incrementScore(RedisKey.RANK_DOC_HOT,String.valueOf(docId),1);
    }

    @Override
    public List<HotDoc> getTopN(int n) {
        //ZREVRANGE 获取 Top N
        Set<ZSetOperations.TypedTuple<String>> topSet = 
        stringRedisTemplate.opsForZSet()
        .reverseRangeWithScores(RedisKey.RANK_DOC_HOT, 0, n-1);
        List<HotDoc> result = new ArrayList<>();
        if (topSet != null) {
            for (ZSetOperations.TypedTuple<String> tuple :topSet){
                if (tuple.getValue() != null) {
                    result.add(new HotDoc(
                        Long.valueOf(tuple.getValue()),
                        tuple.getScore()
                    ));
                }
            }
        }
        return result;
    }
}