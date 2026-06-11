package com.knowledgehub.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具
 *
 * 学习要点：
 * 1. 为什么用 BCrypt 而不是 MD5/SHA256？
 *    —— BCrypt 自带盐值（salt），每次加密结果不同
 *    —— BCrypt 计算速度慢，抗暴力破解
 *    —— BCrypt 的 cost factor 可调节计算复杂度
 *
 * 2. BCrypt 的哈希格式：
 *    $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 *    ├── 2a = 算法版本
 *    ├── 10 = cost factor（2^10 次迭代）
 *    └── 后面 = 盐值 + 哈希值
 *
 * 3. matches(rawPassword, encodedPassword) 怎么能验证？
 *    —— 从 encodedPassword 中提取盐值和 cost factor
 *    —— 用相同的盐值和 cost factor 对 rawPassword 加密
 *    —— 比较结果
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验密码
     *
     * @param rawPassword     明文密码（用户输入）
     * @param encodedPassword 加密后的密码（数据库存储）
     * @return true 匹配 / false 不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
