package com.liaogang.famou.km.auth;

import com.liaogang.famou.km.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 辽港统一认证 Controller（v0.32 OQ-23：取代原 OIDC）。
 *
 * <p>前端流程：
 * <ol>
 *   <li>用户在慧应用平台点击本平台图标</li>
 *   <li>前端 URL 接收 code + codeVerifier + platform 三个 query 参数</li>
 *   <li>前端 POST /auth/liaogong-token 提交这三个参数</li>
 *   <li>后端调用招商云 PAAS getUserInfoByCode（APIKEY 鉴权）</li>
 *   <li>后端基于 sub 创建/查找本地 user + user_role</li>
 *   <li>后端签发本地 JWT（包含 sub / preferred_username / role）</li>
 *   <li>前端缓存 JWT + 跳转 dashboard</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LiaogongAuthController {

    private final LiaogongAuthService authService;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.liaogong-auth.mock-mode:true}")
    private boolean mockMode;

    /**
     * 慧应用 SSO code + codeVerifier 换取本地 JWT
     */
    @PostMapping("/liaogong-token")
    public Result<TokenResponse> exchangeLiaogongToken(
            @Valid @RequestBody LiaogongTokenRequest request,
            HttpServletRequest httpRequest) {
        log.info("慧应用 SSO code exchange: code={}, codeVerifier={}, platform={}",
                request.getCode().substring(0, Math.min(8, request.getCode().length())),
                request.getCodeVerifier().substring(0, Math.min(8, request.getCodeVerifier().length())),
                request.getPlatform());

        TokenResponse response = authService.exchangeToken(request, mockMode);
        return Result.ok(response);
    }

    /**
     * Sprint 1 mock 模式：直接以指定角色登录
     */
    @PostMapping("/mock-login")
    public Result<TokenResponse> mockLogin(@RequestBody MockLoginRequest request) {
        log.info("Mock 登录: role={}", request.getRole());
        TokenResponse response = authService.mockIssueToken(request.getRole());
        return Result.ok(response);
    }

    /**
     * 登出（前端清空 localStorage + 跳转登录页）
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 角色变更下次登录生效（OQ-12 修订）：不主动失效 Redis 中的旧 JWT
        return Result.ok();
    }

    // ============== DTO ==============

    @Data
    public static class LiaogongTokenRequest {
        @NotBlank
        private String code; // 授权码（一次性）
        @NotBlank
        private String codeVerifier; // 校验码
        @NotBlank
        private String platform; // 平台标识（如 hyy）
    }

    @Data
    public static class MockLoginRequest {
        @NotBlank
        private String role; // 5 预置角色之一
    }

    @Data
    public static class TokenResponse {
        private String token;
        private UserInfo user;
        private List<String> roles;
        private long expiresIn;

        @Data
        public static class UserInfo {
            private String sub;
            private String preferredUsername;
            private String displayName;
            private String organizationCode;
        }
    }
}
