package com.knowledgehub.redis;

import java.util.List;

/**
 * ZSet 最近访问知识库 — Day 16
 *
 * 场景：用户查看知识库详情时，记录访问记录；看板展示最近访问的 N 个知识库
 *
 * Redis 结构：ZSet → recent:kb:{userId}
 * - member = kbId（字符串）
 * - score = 访问时间戳（System.currentTimeMillis()）
 *
 * 核心命令：
 * - ZADD key score member：添加/更新访问记录（时间戳覆盖）
 * - ZREVRANGE：按时间戳从大到小取最近 N 个
 * - ZREMRANGEBYRANK：只保留最近 N 条，清理旧数据
 *
 * 面试复盘：
 * 1. 为什么 score 用时间戳？
 *    → ZSet 按 score 排序，时间戳天然递增，ZREVRANGE 就能取最新的
 * 2. 历史数据怎么清理？
 *    → ZREMRANGEBYRANK 保留最近 N 条 + TTL 30 天双重保障
 */
public interface RecentService {

    /**
     * 记录用户访问了某个知识库
     *
     * @param userId 用户 ID
     * @param kbId   知识库 ID
     */
    void recordVisit(Long userId, Long kbId);

    /**
     * 获取用户最近访问的 N 个知识库
     *
     * @param userId 用户 ID
     * @param n      取最近 N 个
     * @return kbId 列表，按访问时间从近到远
     */
    List<Long> getRecent(Long userId, int n);
}
