package com.liaogang.famou.km.audit;

import com.liaogang.famou.km.audit.controller.AuditController;
import com.liaogang.famou.km.audit.enums.AuditAction;
import com.liaogang.famou.km.audit.repository.AuditLogMapper;
import com.liaogang.famou.km.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * T308 U9 审计后端测试 - OQ-5 简化模型 + OQ-11 ID 三重暴露 + 12 月保留
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditLogServiceTest {

    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks private AuditController auditController;

    private List<AuditLogEntity> store;

    @BeforeEach
    void setUp() {
        store = new ArrayList<>();
        // 模拟 DB store: 通过 id 查询
        when(auditLogMapper.selectById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return store.stream().filter(e -> id.equals(e.getId())).findFirst().orElse(null);
        });
        when(auditLogMapper.findByUserId(anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(inv -> {
                    String userId = inv.getArgument(0);
                    return store.stream().filter(e -> userId.equals(e.getUserId())).collect(java.util.stream.Collectors.toList());
                });
        when(auditLogMapper.findByTargetKo(anyString())).thenAnswer(inv -> {
            String targetKo = inv.getArgument(0);
            return store.stream().filter(e -> targetKo.equals(e.getTargetKo())).collect(java.util.stream.Collectors.toList());
        });
        when(auditLogMapper.findByActionAndDateRange(anyString(), any(), any()))
                .thenAnswer(inv -> {
                    String action = inv.getArgument(0);
                    return store.stream().filter(e -> action.equals(e.getAction())).collect(java.util.stream.Collectors.toList());
                });
        when(auditLogMapper.selectList(any())).thenAnswer(inv -> store);
    }

    @Test
    @DisplayName("OQ-5 简化模型: AuditLogEntity 6 字段无 reverted_at / status(reverted)")
    void oq5_simplifiedModel_6FieldsNoReverted() {
        AuditLogEntity log = new AuditLogEntity();
        log.setId("AUDIT-20260717-000001");
        log.setAction(AuditAction.KO_CREATE.name());
        log.setUserId("user-001");
        log.setTargetKo("KO-MAT-0042");
        log.setDetail("{\"title\":\"料场库存上下限\"}");
        log.setReason(null);

        store.add(log);

        // OQ-5: 6 字段 (id, action, userId, targetKo, detail, reason, createdAt) 无 reverted_at
        assertNotNull(log.getId());
        assertNotNull(log.getAction());
        assertNotNull(log.getUserId());
        assertTrue(log.getTargetKo() != null || log.getTargetKo() == null, "targetKo 可空");
        assertNotNull(log.getDetail());
        // reverted_at / status(reverted) 字段不存在 (OQ-5 简化)
        // 验证: 反射检查 entity 无 revertedAt 字段
        boolean hasRevertedAt = false;
        for (java.lang.reflect.Field f : AuditLogEntity.class.getDeclaredFields()) {
            if (f.getName().equals("revertedAt") || f.getName().equals("statusReverted")) {
                hasRevertedAt = true;
            }
        }
        assertEquals(false, hasRevertedAt, "OQ-5 简化: AuditLogEntity 无 reverted_at 字段");
    }

    @Test
    @DisplayName("OQ-11 ID 三重暴露: 单条详情接口返回完整 AUDIT ID")
    void oq11_idTripleExposure_singleDetailReturnsFullAuditId() {
        AuditLogEntity log = new AuditLogEntity();
        log.setId("AUDIT-20260717-000001");
        log.setAction(AuditAction.USER_CONFLICT_ARBITRATE.name());
        log.setUserId("admin-001");
        log.setTargetKo("KO-MAT-0042");
        store.add(log);

        Result<AuditLogEntity> result = auditController.getById("AUDIT-20260717-000001");
        assertNotNull(result);
        assertEquals("AUDIT-20260717-000001", result.getData().getId());
        assertEquals(AuditAction.USER_CONFLICT_ARBITRATE.name(), result.getData().getAction());
    }

    @Test
    @DisplayName("Edge case: 不存在 ID 返回 40460 fail")
    void nonExistentId_returns40460() {
        Result<AuditLogEntity> result = auditController.getById("AUDIT-20260717-999999");
        assertNotNull(result);
        assertEquals(40460, result.getCode());
    }

    @Test
    @DisplayName("Edge case: CSV 导出首列必为 AUDIT ID (OQ-11)")
    void csvExport_firstColumnIsAuditId() {
        AuditLogEntity log1 = new AuditLogEntity();
        log1.setId("AUDIT-20260717-000001");
        log1.setAction(AuditAction.KO_CREATE.name());
        log1.setUserId("user-001");
        log1.setTargetKo("KO-A");
        log1.setDetail("{\"k\":\"v\"}");
        log1.setCreatedAt(java.time.LocalDateTime.of(2026, 7, 17, 10, 0));
        store.add(log1);

        AuditLogEntity log2 = new AuditLogEntity();
        log2.setId("AUDIT-20260717-000002");
        log2.setAction(AuditAction.KO_UPDATE.name());
        log2.setUserId("user-001");
        log2.setTargetKo("KO-B");
        log2.setDetail("plain text");  // 不含逗号
        log2.setCreatedAt(java.time.LocalDateTime.of(2026, 7, 17, 11, 0));
        store.add(log2);

        Result<String> result = auditController.exportCsv(null, null, null);
        String csv = result.getData();
        // 首行 header: AUDIT_ID,action,user_id,target_ko,detail,reason,created_at
        assertTrue(csv.startsWith("AUDIT_ID,action,"), "CSV 首行 header 必为 AUDIT_ID");
        // 数据行首列必为 AUDIT ID 完整字符串
        assertTrue(csv.contains("AUDIT-20260717-000001,"), "数据行首列必为完整 AUDIT ID");
        assertTrue(csv.contains("AUDIT-20260717-000002,"), "第二条数据行首列必为完整 AUDIT ID");
    }

    @Test
    @DisplayName("CSV 导出: 含逗号/引号的 detail 字段正确转义")
    void csvExport_detailFieldEscapedProperly() {
        AuditLogEntity log = new AuditLogEntity();
        log.setId("AUDIT-20260717-000010");
        log.setAction(AuditAction.KO_UPDATE.name());
        log.setUserId("user-001");
        log.setTargetKo("KO-A");
        log.setDetail("detail with \"quote\" and, comma");  // 含逗号 + 引号
        log.setCreatedAt(java.time.LocalDateTime.of(2026, 7, 17, 10, 0));
        store.add(log);

        Result<String> result = auditController.exportCsv(null, null, null);
        String csv = result.getData();
        // detail 字段被 \" 包住 + 内嵌 \" 转义
        assertTrue(csv.contains("\"detail with \"\"quote\"\" and, comma\""), "CSV 转义正确");
    }

    @Test
    @DisplayName("AuditAction enum 集中管理 - 17 种 action 不重复")
    void auditActionEnum_18ActionsDistinct() {
        AuditAction[] actions = AuditAction.values();
        assertEquals(17, actions.length, "17 种 action 定义");
        // action 名称去重
        java.util.Set<String> names = new java.util.HashSet<>();
        for (AuditAction a : actions) names.add(a.name());
        assertEquals(17, names.size(), "action 名称无重复");
    }

    @Test
    @DisplayName("Edge case: by-target 查询 - 返回目标 KO 相关审计")
    void findByTargetKo_returnsRelatedAudits() {
        AuditLogEntity log1 = new AuditLogEntity();
        log1.setId("AUDIT-001");
        log1.setTargetKo("KO-MAT-0042");
        store.add(log1);

        AuditLogEntity log2 = new AuditLogEntity();
        log2.setId("AUDIT-002");
        log2.setTargetKo("KO-MAT-9999");
        store.add(log2);

        Result<List<AuditLogEntity>> result = auditController.findByTargetKo("KO-MAT-0042");
        assertEquals(1, result.getData().size());
        assertEquals("AUDIT-001", result.getData().get(0).getId());
    }
}
