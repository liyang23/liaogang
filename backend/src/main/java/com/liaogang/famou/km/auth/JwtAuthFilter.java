package com.liaogang.famou.km.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaogang.famou.km.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 鉴权过滤器（v0.32 OQ-12 + OQ-23）。
 *
 * <p>流程：
 * <ol>
 *   <li>从 Authorization Bearer header 提取 JWT</li>
 *   <li>验证签名 + 解析 claims（sub / preferred_username / role / roles）</li>
 *   <li>基于 role 构造 Spring Security authorities</li>
 *   <li>注入 SecurityContext 供 Spring Security 路由守卫使用</li>
 * </ol>
 *
 * <p>角色变更下次登录生效（OQ-12 修订）：旧 JWT 缓存不主动失效，用户重新登录时新 JWT 携带新角色。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${app.liaogong-auth.jwt-secret:default-jwt-secret-please-change-in-production-32bytes-min}")
    private String jwtSecret;

    @Value("${app.liaogong-auth.jwt-ttl-seconds:3600}")
    private long jwtTtlSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            String sub = claims.getSubject();
            String role = (String) claims.get("role");
            String preferredUsername = (String) claims.get("preferred_username");

            if (role != null) {
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    sub, null, authorities
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 暴露当前角色给业务代码
                request.setAttribute("km_user_sub", sub);
                request.setAttribute("km_user_role", role);
                request.setAttribute("km_preferred_username", preferredUsername);
            }
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            // 不写入 SecurityContext，留给 Spring Security 拒绝
        }

        chain.doFilter(request, response);
    }

    private SecretKey secretKey() {
        byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // JJWT 0.11+ 要求至少 256 位（32 字节）
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 签发 JWT（供 LiaogongAuthService 调用）
     */
    public String issueToken(String sub, String preferredUsername, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .setSubject(sub)
            .claim("preferred_username", preferredUsername)
            .claim("role", role)
            .setIssuedAt(new java.util.Date(now))
            .setExpiration(new java.util.Date(now + jwtTtlSeconds * 1000))
            .signWith(secretKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
