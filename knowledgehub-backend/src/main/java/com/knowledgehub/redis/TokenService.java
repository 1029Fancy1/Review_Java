package com.knowledgehub.redis;

/**
 * Token Service — 登录态管理
 */
public interface TokenService {

    /**
     * 保存 token → Redis（登录成功后调用）
     *
     * @param token  JWT token 字符串
     * @param userId 用户 ID
     */
    void save(String token, Long userId);

    /**
     * 从 Redis 查 token 是否有效，返回 userId
     *
     * @param token JWT token 字符串
     * @return userId，token 不存在或过期返回 null
     */
    Long getUserId(String token);

    /**
     * 删除 token（退出登录时调用）
     *
     * @param token JWT token 字符串
     */
    void delete(String token);
}
