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

    @PostConstruct
    public void init() {
        checkDefaultSecret();
    }

    // F-7 修复：fail-fast 检测 demo 默认密钥，避免 production 误用
    private void checkDefaultSecret() {
        if (jwtSecret != null && jwtSecret.startsWith("default-jwt-secret")) {
            String profile = System.getProperty("spring.profiles.active", "");
            if (profile.contains("prod") || profile.contains("prd")) {
                throw new IllegalStateException(
                    "生产环境检测到默认 JWT 密钥，请设置 LIAOGONG_JWT_SECRET 环境变量后重启。" +
                    "当前密钥前缀=" + jwtSecret.substring(0, Math.min(20, jwtSecret.length())));
            }
            System.out.println("[WARN] 使用默认 JWT 密钥（仅限 dev/test profile）。生产环境必须设置 LIAOGONG_JWT_SECRET。");
        }
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
