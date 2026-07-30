package com.yuan.exam.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责产生、解析与验证 Token
 */
@Component
public class JwtUtils {

    /** JWT 签名密钥字符串（来自 application.yml） */
    @Value("${jwt.secret}")
    private String secret;

    /** Token 过期时间（毫秒） */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 取得签名用密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 产生 JWT Token
     *
     * @param username 用户名称
     * @param role     角色
     * @return JWT 字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token，取出 username
     *
     * @param token JWT 字符串
     * @return 用户名称
     */
    public String parseToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中解析角色
     *
     * @param token JWT 字符串
     * @return 角色字符串
     */
    public String parseRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT 字符串
     * @return true 表示有效，false 表示无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Token 取得 Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
