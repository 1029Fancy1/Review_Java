package com.knowledgehub.redis;

/**
 * Redis Key 构建器 — 将 Key 模板 + 参数 → 最终 Redis Key
 *
 * 学习要点（Day 8 重点理解）：
 * 1. Key 模板中用 %s 占位，通过 String.format() 填入参数
 * 2. 集中管理的好处：改 key 规范只需改一处，不用全局搜索替换
 * 3. Pattern 常量复用，避免硬编码
 *
 * 使用示例：
 * String key = RedisKeyBuilder.loginToken("abc123");
 * // 返回 "login:token:abc123"
 *
 * 面试复盘：
 * - Redis Key 命名规范？
 *   → 业务:模块:标识（如 login:token:{token}），可读性强，方便运维排查
 */
public class RedisKeyBuilder {

    // ==================== 登录态 ====================
    public static String loginToken(String token) {
        return String.format(RedisKey.LOGIN_TOKEN,token);
    }

    // ==================== 验证码 ====================

    public static String captchaEmail(String email) {
        return String.format(RedisKey.CAPTCHA_EMAIL, email);
    }

    public static String captchaCooldown(String email) {
        return String.format(RedisKey.CAPTCHA_COOLDOWN, email);
    }

    // ==================== Cache Aside 缓存 ====================

    public static String userInfo(Long userId) {

        return String.format(RedisKey.USER_INFO,userId);
    }

    public static String kbList(Long userId) {
        return String.format(RedisKey.KB_LIST,userId);
    }

    public static String docDetail(Long docId) {
        return String.format(RedisKey.DOC_DETAIL,docId);
    }

    // ==================== 分布式锁 ====================

    public static String lockDocParse(Long docId) {
        return String.format(RedisKey.LOCK_DOC_PARSE,docId);
    }

    // ==================== 限流 ====================

    public static String rateChat(Long userId, String minute) {
        return String.format(RedisKey.RATE_CHAT,userId,minute);
    }

    // ==================== ZSet ====================

    public static String recentKb(Long userId) {
        return String.format(RedisKey.RECENT_KB,userId);
    }

    // ==================== Hash 解析进度 ====================

    public static String taskDocParse(Long docId) {
        return String.format(RedisKey.TASK_DOC_PARSE,docId);
    }

    // ==================== 问答缓存 ====================

    public static String chatAnswer(Long kbId, Integer kbVersion, String questionHash) {
        return String.format(RedisKey.CHAT_ANSWER,kbId,kbVersion,questionHash);
    }

    // ==================== 每日额度 ====================

    public static String quotaChatDaily(Long userId, String date) {
        return String.format(RedisKey.QUOTA_CHAT_DAILY,userId,date);
    }

    // ==================== 幂等 ====================

    public static String idemToken(String token) {
        return String.format(RedisKey.IDEM_TOKEN,token);
    }
}
