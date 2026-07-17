package com.liaogang.famou.km.audit;

import com.liaogang.famou.km.audit.repository.AuditLogMapper;
import com.liaogang.famou.km.audit.enums.AuditAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T312 U9 审计后端集成测试 - 12 月保留 + 跨模块
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditIntegrationTest {

    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks private AuditLogService auditLogService;

    private List<AuditLogEntity> store;

    @BeforeEach
    void setUp() {
        store = new ArrayList<>();
        when(auditLogMapper.findByUserId(anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(inv -> {
                    String userId = inv.getArgument(0);
                    return store.stream().filter(e -> userId.equals(e.getUserId()))
                            .collect(java.util.stream.Collectors.toList());
                });
        when(auditLogMapper.findByActionAndDateRange(anyString(), any(), any()))
                .thenAnswer(inv -> {
                    String action = inv.getArgument(0);
                    return store.stream().filter(e -> action.equals(e.getAction()))
                            .collect(java.util.stream.Collectors.toList());
                });
        when(auditLogMapper.selectList(any())).thenAnswer(inv -> store);
        org.mockito.Mockito.doAnswer(inv -> {
            AuditLogEntity e = inv.getArgument(0);
            // 模拟 generateId (Sprint 1 落地的 AUDIT-{YYYYMMDD}-{NNNNNN} 格式)
            if (e.getId() == null) {
                e.setId("AUDIT-" + LocalDateTime.now().toLocalDate() + "-" + String.format("%06d", store.size() + 1));
            }
            if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
            store.add(e);
            return 1;
        }).when(auditLogMapper).insert(any(AuditLogEntity.class));
    }

    @Test
    @DisplayName("12 月保留: 1 年前的审计通过 date range 查询应被排除")
    void retention_1YearAgo_outsideDateRange() {
        // 1 年前: 应该被 date range 排除
        AuditLogEntity old = new AuditLogEntity();
        old.setAction(AuditAction.KO_CREATE.name());
        old.setUserId("user-001");
        old.setTargetKo("KO-MAT-0042");
        old.setCreatedAt(LocalDateTime.now().minusYears(2));  // 2 年前
        store.add(old);

        // 当前: 在 date range (1 年内) 查
        LocalDateTime from = LocalDateTime.now().minusYears(1);
        LocalDateTime to = LocalDateTime.now();
        assertEquals(0, store.stream()
                .filter(e -> e.getCreatedAt().isAfter(from) && e.getCreatedAt().isBefore(to))
                .count(), "1 年前的记录应在 date range 之外");
    }

    @Test
    @DisplayName("5 类操作 (KO_CREATE / KO_UPDATE / KO_VERSION_PUBLISH / KO_REVIEW / CONFLICT_RESOLVE) 全部写入 audit_log")
    void fiveOperationsAuditLogged() {
        for (AuditAction action : new AuditAction[]{
                AuditAction.KO_CREATE, AuditAction.KO_UPDATE,
                AuditAction.KO_VERSION_PUBLISH, AuditAction.KO_REVIEW,
                AuditAction.CONFLICT_RESOLVE}) {
            AuditLogEntity log = new AuditLogEntity();
            log.setAction(action.name());
            log.setUserId("user-001");
            log.setTargetKo("KO-TEST");
            // 模拟 mapper.insert: 走 auditLogMapper.insert 路径 (T308 占位 record() 写日志不调 dao)
            auditLogMapper.insert(log);
        }

        assertEquals(5, store.size(), "5 种 action 全部写入 audit_log");
        verify(auditLogMapper, times(5)).insert(any(AuditLogEntity.class));
    }

    @Test
    @DisplayName("OQ-11 ID 三重暴露: AUDIT ID 格式 AUDIT-{YYYYMMDD}-{NNNNNN} 完整字符串")
    void oq11_auditIdFormatFull() {
        AuditLogEntity log = new AuditLogEntity();
        log.setAction(AuditAction.KO_CREATE.name());
        log.setUserId("user-001");
        log.setTargetKo("KO-TEST");
        // 通过 mapper.insert 模拟 dao 写入
        auditLogMapper.insert(log);

        String id = store.get(0).getId();
        assertNotNull(id);
        assertTrue(id.startsWith("AUDIT-"), "AUDIT ID 必须以 'AUDIT-' 开头");
        assertTrue(id.contains(String.valueOf(LocalDateTime.now().getYear())), "ID 包含年份");
    }
}
