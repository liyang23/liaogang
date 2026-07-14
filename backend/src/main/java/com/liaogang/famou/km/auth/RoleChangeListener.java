package com.liaogang.famou.km.auth;

import com.liaogang.famou.km.audit.AuditLog;
import com.liaogang.famou.km.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 角色变更监听器（v0.32 OQ-12 修订）。
 *
 * <p>OQ-12 决议：角色变更下次登录生效，旧 JWT 缓存不主动失效。
 * 本监听器仅在用户成功登录时记录审计日志（USER_ROLE_LOGIN + 角色快照），
 * 不修改现有 JWT 缓存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleChangeListener {

    private final AuditLogService auditLogService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String sub = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .findFirst()
            .orElse("UNKNOWN");

        log.debug("用户登录成功: sub={}, role={}", sub, role);

        // 写审计日志（实际触发由 T006 实施时接入 USER_ROLE_LOGIN 事件类型）
        AuditLog auditLog = AuditLog.builder()
            .action("USER_ROLE_LOGIN")
            .userId(sub)
            .detail("role=" + role)
            .build();
        auditLogService.recordAsync(auditLog);
    }
}
