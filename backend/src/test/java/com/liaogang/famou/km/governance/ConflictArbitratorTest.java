package com.liaogang.famou.km.governance;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import com.liaogang.famou.km.governance.service.ConflictArbitrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T302 ConflictArbitrator 测试 - OQ-8 仲裁快路径 4 状态一次性完成
 */
@ExtendWith(MockitoExtension.class)
class ConflictArbitratorTest {

    @Mock private ConflictMapper conflictMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private ConflictArbitrator arbitrator;

    private ConflictEntity conflict;

    @BeforeEach
    void setUp() {
        conflict = new ConflictEntity();
        conflict.setId(42L);
        conflict.setConflictType("C1");
        conflict.setKoAId("KO-MAT-0042");
        conflict.setKoBId("KO-MAT-0043");
        conflict.setConfidence(0.85);
    }

    @Test
    @DisplayName("Happy path: 管理员点击仲裁 → 4 状态一次性转 Published + 写 USER_CONFLICT_ARBITRATE 审计")
    void arbitrate_4StatesCompleted_atomically() {
        ConflictEntity result = arbitrator.arbitrateAndPublish(conflict, "admin-001");

        assertNotNull(result);
        assertEquals("published", result.getResolutionState(), "OQ-8 4 状态最终为 published");
        assertEquals("resolved", result.getStatus());
        assertNotNull(result.getResolvedAt());
        verify(conflictMapper).updateById(conflict);
        verify(auditLogService).record(any(AuditLogEntity.class));
    }

    @Test
    @DisplayName("Edge case: C6 置信度 <0.8 — 不阻断仲裁 (前端 Modal 二次确认在 T303 实现)")
    void c6LowConfidence_doesNotBlockArbitration() {
        conflict.setConfidence(0.7);  // C6 阈值以下

        ConflictEntity result = arbitrator.arbitrateAndPublish(conflict, "admin-001");

        // 后端不阻断: 仍然完成 4 状态转换
        assertEquals("published", result.getResolutionState());
    }

    @Test
    @DisplayName("Error path: conflict=null 抛 IllegalArgumentException")
    void nullConflict_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> arbitrator.arbitrateAndPublish(null, "admin-001"));
    }
}
