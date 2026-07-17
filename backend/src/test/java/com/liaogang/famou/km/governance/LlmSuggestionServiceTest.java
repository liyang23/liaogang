package com.liaogang.famou.km.governance;

import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.llm.DeepSeekClient;
import com.liaogang.famou.km.llm.DeepSeekClient.ConflictContext;
import com.liaogang.famou.km.llm.DeepSeekClient.LlmSuggestion;
import com.liaogang.famou.km.llm.LlmQuotaService;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import com.liaogang.famou.km.governance.service.LlmSuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T302 LLM 建议服务测试 - DeepSeek mock + 配额管理 + 异常降级 (OQ-9 + R17a)
 */
@ExtendWith(MockitoExtension.class)
class LlmSuggestionServiceTest {

    @Mock private DeepSeekClient deepSeekClient;
    @Mock private LlmQuotaService llmQuotaService;
    @Mock private ConflictMapper conflictMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private LlmSuggestionService service;

    private ConflictEntity conflict;
    private LlmSuggestion mockSuggestion;

    @BeforeEach
    void setUp() {
        conflict = new ConflictEntity();
        conflict.setId(1L);
        conflict.setConflictType("C1");
        conflict.setKoAId("KO-MAT-0042");
        conflict.setKoBId("KO-MAT-0043");
        conflict.setScopeKey("ko_type:MAT");
        conflict.setFieldKey("min");

        mockSuggestion = new LlmSuggestion();
        mockSuggestion.setSuggestion("合并");
        mockSuggestion.setConfidence(0.85);
        mockSuggestion.setRationale("两个 KO 仅在 min 字段值上有差异，建议合并");
    }

    @Test
    @DisplayName("正常路径: 配额通过 + DeepSeek 200 + 审计写入 + 实体更新")
    void normalPath_quotaPasses_deepseekSucceeds() {
        when(llmQuotaService.incrementAndCheck(anyString())).thenReturn(true);
        when(deepSeekClient.getConflictSuggestion(any(ConflictContext.class))).thenReturn(mockSuggestion);

        LlmSuggestion result = service.suggestForConflict(conflict, "user-001");

        assertNotNull(result);
        assertNotNull(result.getSuggestion());
        verify(conflictMapper).updateById(conflict);
        verify(auditLogService).record(any());
    }

    @Test
    @DisplayName("Edge case: 配额耗尽 — 直接返回 null 不调 DeepSeek")
    void quotaExhausted_returnsNull_noDeepseekCall() {
        when(llmQuotaService.incrementAndCheck(anyString())).thenReturn(false);

        LlmSuggestion result = service.suggestForConflict(conflict, "user-001");

        assertNull(result);
        verify(deepSeekClient, never()).getConflictSuggestion(any(ConflictContext.class));
    }

    @Test
    @DisplayName("Error path: DeepSeek 5s 超时 / 42901 — 降级返回 null 不写审计")
    void deepseekTimeout_returnsNull_noAudit() {
        when(llmQuotaService.incrementAndCheck(anyString())).thenReturn(true);
        when(deepSeekClient.getConflictSuggestion(any(ConflictContext.class)))
                .thenThrow(new RuntimeException("DeepSeek v4 timeout 5s exceeded"));

        LlmSuggestion result = service.suggestForConflict(conflict, "user-001");

        assertNull(result);
        verify(auditLogService, never()).record(any());
    }
}
