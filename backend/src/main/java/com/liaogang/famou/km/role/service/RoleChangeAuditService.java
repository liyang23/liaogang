package com.liaogang.famou.km.role.service;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.role.event.RoleChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 角色变更审计服务（T207）。
 *
 * <p>监听 RoleChangeEvent 写 USER_ROLE_CHANGE 审计日志
 * <p>OQ-12 决策：仅记录审计，不主动失效旧 JWT（跨设备撤销不可行 OQ-5）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleChangeAuditService {

    private final AuditLogService auditLogService;

    @EventListener
    public void onRoleChange(RoleChangeEvent event) {
        AuditLogEntity auditLog = AuditLogEntity.builder()
            .action("USER_ROLE_CHANGE")
            .userId(event.getUserSub())
            .detail(String.format(
                "role=%s, changeType=%s, effectiveAt=%s, operator=%s",
                event.getRoleId(),
                event.getChangeType(),
                event.getEffectiveAt(),
                event.getOperatorSub()
            ))
            .build();
        auditLogService.recordAsync(auditLog);
        log.info("✓ 角色变更审计: user={}, role={}, type={}, operator={}",
            event.getUserSub(), event.getRoleId(), event.getChangeType(), event.getOperatorSub());
    }
}
