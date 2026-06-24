package com.knowledgehub.redis;

import java.util.List;

/**
 * ZSet 热门文档排行榜 — Day 15
 *
 * 场景：用户访问文档详情时，热度 +1；看板页展示 Top N 热门文档
 *
 * Redis 结构：ZSet → rank:doc:hot
 * - member = docId（字符串）
 * - score = 访问次数（热度）
 *
 * 核心命令：
 * - ZINCRBY：原子增加分数（不存在则自动创建，score=0+increment）
 * - ZREVRANGE WITHSCORES：按分数从高到低取 Top N
 *
 * 面试复盘：
 * 1. ZSet 底层结构？
 *    → 元素少：ziplist（压缩列表），元素多：skiplist（跳表）+ dict
 * 2. ZINCRBY 是原子操作吗？
 *    → 是，单条命令，天然原子
 * 3. 亿级排行榜怎么搞？
 *    → 分段（日榜/周榜/总榜）、分片、定时归档
 */
public interface RankingService {

    /**
     * 文档热度 +1（每次查看文档详情时调用）
     *
     * @param docId 文档 ID
     */
    void incrHot(Long docId);

    /**
     * 获取热门文档 Top N
     *
     * @param n Top N
     * @return [{docId, score}, ...]，按热度从高到低
     */
    List<HotDoc> getTopN(int n);

    /**
     * 热门文档条目
     */
    record HotDoc(Long docId, Double score) {}
}
