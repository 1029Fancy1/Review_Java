package com.knowledgehub.redis;

/**
 * Redis Key 常量 — 定义所有场景的 Key 模板
 *
 * 学习要点（Day 8）：
 * 1. 所有 Redis Key 集中管理，避免散落各处难以维护
 * 2. Key 命名规范：业务模块:场景:参数
 * 3. 每个 Key 附带 TTL 说明，一目了然
 */
public class RedisKey {

    // ==================== 登录态 ====================
    /** login:token:{token} — TTL: 7 天 */
    public static final String LOGIN_TOKEN = "login:token:%s";

    // ==================== 验证码 ====================
    /** captcha:email:{email} — TTL: 5 分钟 */
    public static final String CAPTCHA_EMAIL = "captcha:email:%s";
    /** captcha:cooldown:{email} — TTL: 60 秒（防重复发送） */
    public static final String CAPTCHA_COOLDOWN = "captcha:cooldown:%s";

    // ==================== Cache Aside 缓存 ====================
    /** user:info:{userId} — TTL: 30 分钟 */
    public static final String USER_INFO = "user:info:%s";
    /** kb:list:{userId} — TTL: 10-30 分钟随机 */
    public static final String KB_LIST = "kb:list:%s";
    /** doc:detail:{docId} — TTL: 30-60 分钟随机 */
    public static final String DOC_DETAIL = "doc:detail:%s";

    // ==================== 分布式锁 ====================
    /** lock:doc:parse:{docId} — TTL: 10 分钟 */
    public static final String LOCK_DOC_PARSE = "lock:doc:parse:%s";

    // ==================== 限流 ====================
    /** rate:chat:{userId}:{yyyyMMddHHmm} — TTL: 60 秒 */
    public static final String RATE_CHAT = "rate:chat:%s:%s";

    // ==================== ZSet 排行榜 ====================
    /** rank:doc:hot — TTL: 无（按周期清理） */
    public static final String RANK_DOC_HOT = "rank:doc:hot";
    /** recent:kb:{userId} — TTL: 30 天 */
    public static final String RECENT_KB = "recent:kb:%s";

    // ==================== Hash 解析进度 ====================
    /** task:doc:parse:{docId} — TTL: 24 小时 */
    public static final String TASK_DOC_PARSE = "task:doc:parse:%s";

    // ==================== 问答缓存 ====================
    /** chat:answer:{kbId}:{kbVersion}:{questionHash} — TTL: 30 分钟 */
    public static final String CHAT_ANSWER = "chat:answer:%s:%s:%s";

    // ==================== 每日额度 ====================
    /** quota:chat:daily:{userId}:{yyyyMMdd} — TTL: 到当天 23:59:59 */
    public static final String QUOTA_CHAT_DAILY = "quota:chat:daily:%s:%s";

    // ==================== 幂等 ====================
    /** idem:token:{token} — TTL: 5 分钟 */
    public static final String IDEM_TOKEN = "idem:token:%s";
}
