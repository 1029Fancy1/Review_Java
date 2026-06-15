package com.knowledgehub.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — token 的生成和解析
 *
 * 学习要点（Day 9）：
 * 1. JWT 结构：Header.Payload.Signature（三段 Base64 拼接）
 *    - Header：算法类型（HS256）
 *    - Payload：存的数据（userId、过期时间）
 *    - Signature：签名，防止篡改（Header + Payload + 密钥 算出来的）
 * 2. 为什么不直接把 JWT 当登录态用？
 *    → JWT 是无状态的，发出去了就收不回来（没法主动踢人下线）
 *    → 配合 Redis：Redis 存 token，删掉 token 就强制下线
 */
@Component
public class JwtUtils {

    @Value("${knowledgehub.jwt.secret:knowledgehub-jwt-secret-key-change-in-production}")
    private String secret;

    @Value("${knowledgehub.jwt.expiration:604800000}")
    private long expiration;  // 默认 7 天

    private SecretKey getKey() {
        // HS256 要求密钥至少 256 bit，不够的用 SHA 扩展
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : secret.repeat(3).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token，把 userId 存进 Payload
     */
    public String generate(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))         // Payload: sub = userId
                .issuedAt(new Date())                     // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(getKey())                       // 签名
                .compact();
    }

    /**
     * 解析 token，取出 userId
     * token 过期或被篡改会直接抛异常
     */
    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
