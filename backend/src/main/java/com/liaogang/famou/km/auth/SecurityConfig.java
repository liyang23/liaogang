package com.liaogang.famou.km.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置（v0.32 OQ-23：辽港统一认证取代原 OIDC）。
 *
 * <p>鉴权流程：慧应用 SSO 跳转 → code + codeVerifier 提交 → 调招商云 PAAS
 * getUserInfoByCode → 本地 user + user_role 创建/查找 → 本地 JWT 签发。
 *
 * <p>角色信息从本地 user_role + role_permission 表查询，**不依赖辽港 sub claim 中的 role 字段**。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离架构）
            .csrf(csrf -> csrf.disable())
            // 启用 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态会话（前后端分离 + JWT 鉴权）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 路由权限配置
            .authorizeHttpRequests(auth -> auth
                // 公共路由（无需鉴权）
                .requestMatchers("/auth/liaogong-token", "/auth/mock-login", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // 预检请求
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // KO 库查阅（5 预置角色均可）
                .requestMatchers(HttpMethod.GET, "/api/ko/**").hasAnyRole("ROLE-0001", "ROLE-0002", "ROLE-0003", "ROLE-0004", "ROLE-0005")
                // KO 库编辑（仅算法工程师 + 系统管理员 + 业务专家对 RUL/PAR）
                .requestMatchers(HttpMethod.POST, "/api/ko/**").hasAnyRole("ROLE-0001", "ROLE-0003", "ROLE-0004")
                .requestMatchers(HttpMethod.PUT, "/api/ko/**").hasAnyRole("ROLE-0001", "ROLE-0003", "ROLE-0004")
                .requestMatchers(HttpMethod.DELETE, "/api/ko/**").hasAnyRole("ROLE-0001")
                // 审计日志（仅系统管理员 + 合规审核员）
                .requestMatchers("/api/audit/**").hasAnyRole("ROLE-0001", "ROLE-0002")
                // 项目管理 / 字典管理 / 权限（仅系统管理员）
                .requestMatchers("/api/project/**", "/api/dict/**", "/api/role/**").hasRole("ROLE-0001")
                // 默认鉴权要求
                .anyRequest().authenticated())
            // JWT 鉴权过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // F-1 修复：禁止 setAllowedOriginPatterns(["*"]) + setAllowCredentials(true) 组合
        // （Spring Security 6 + CORS 规范：Allow-Credentials + Wildcard Origin 触发浏览器拒绝）
        // 改为允许具体 origin 列表 + credentials（通过 CORS_ALLOWED_ORIGINS 环境变量配置）
        // 默认仅开发环境（localhost:5173 Vite + localhost:3000 备用端口）
        String allowedOrigins = System.getenv().getOrDefault(
            "CORS_ALLOWED_ORIGINS",
            "http://localhost:5173,http://localhost:3000"
        );
        List<String> origins = java.util.Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
