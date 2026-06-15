package com.knowledgehub.redis;

/**
 * 验证码 Service — 模拟邮箱验证码，Redis 存储
 */
public interface CaptchaService {

    /**
     * 生成验证码 → 存 Redis → 返回给前端（真实项目发邮件）
     *
     * @param email 邮箱地址
     */
    void generate(String email);

    /**
     * 校验验证码，校验成功后删除（一次性使用）
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     * @return true=正确  false=错误或已过期
     */
    boolean verify(String email, String code);
}
