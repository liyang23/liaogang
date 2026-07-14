package com.liaogang.famou.km.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 签发与解析服务（v0.32 OQ-23 + OQ-12）。
 *
 * <p>本地 JWT 包含：
 * <ul>
 *   <li>sub：用户唯一标识（来自辽港统一认证 API）</li>
 *   <li>preferred_username：工号（业务主键）</li>
 *   <li>role：5 预置角色之一（从本地 user_role 表获取，不在 sub claim 中）</li>
 * </ul>
 *
 * <p>角色变更下次登录生效（OQ-12 修订）：旧 JWT 缓存不主动失效。
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.liaogong-auth.jwt-secret:default-jwt-secret-please-change-in-production-32bytes-min}")
    private String jwtSecret;

    @Value("${app.liaogong-auth.jwt-ttl-seconds:3600}")
    private long jwtTtlSeconds;

    public String issueToken(String sub, String preferredUsername, String role) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", preferredUsername);
        claims.put("role", role);

        return Jwts.builder()
            .setSubject(sub)
            .setClaims(claims)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + jwtTtlSeconds * 1000))
            .signWith(secretKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private SecretKey secretKey() {
        byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
