package com.liaogang.famou.km.governance.service;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.llm.DeepSeekClient;
import com.liaogang.famou.km.llm.LlmQuotaService;
import com.liaogang.famou.km.llm.DeepSeekClient.ConflictContext;
import com.liaogang.famou.km.llm.DeepSeekClient.LlmSuggestion;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 建议服务（U7 / T302 / OQ-9 接入 DeepSeek v4 + 配额管理 + ≤5s 响应 NFR-28）
 *
 * <p>wrapper over Sprint 1 已落地的 DeepSeekClient + LlmQuotaService（避免双重 LLM 配额扣减）。
 * 业务语义：调用 DeepSeek v4 获取 LLM 建议 + 配额原子扣减 + 写 audit_log (LLM_SUGGEST_REQUESTED)。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmSuggestionService {

    private final DeepSeekClient deepSeekClient;
    private final LlmQuotaService llmQuotaService;
    private final ConflictMapper conflictMapper;
    private final AuditLogService auditLogService;

    /**
     * 获取冲突的 LLM 建议（OQ-9 接入 DeepSeek v4）
     * 返回 null 表示配额耗尽或调用失败（前端应展示降级提示）
     */
    public LlmSuggestion suggestForConflict(ConflictEntity conflict, String userSub) {
        // 配额原子扣减（Redis Lua 脚本，平台 + 用户级双重限制）
        if (!llmQuotaService.incrementAndCheck(userSub)) {
            log.warn("LLM quota exhausted for user={} conflict={}", userSub, conflict.getId());
            return null;
        }

        ConflictContext ctx = new ConflictContext();
        ctx.setConflictType(conflict.getConflictType());
        ctx.setBothPartiesContent(buildContentFromConflict(conflict));

        LlmSuggestion suggestion = null;
        try {
            suggestion = deepSeekClient.getConflictSuggestion(ctx);
        } catch (Exception e) {
            // 5s 超时 / 42901 / 网络错 统一降级 (R17a)
            log.warn("LLM suggestion call failed: conflict={} reason={}", conflict.getId(), e.getMessage());
            return null;
        }

        // 持久化 LLM 建议到 conflict 表
        conflict.setLlmSuggestion(suggestion.getSuggestion());
        conflict.setLlmRationale(suggestion.getRationale());
        conflict.setConfidence(suggestion.getConfidence());
        conflictMapper.updateById(conflict);

        // 写 audit_log (5 类操作之一)
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction("LLM_SUGGEST_REQUESTED");
        auditLog.setUserId(userSub);
        auditLog.setTargetKo(conflict.getKoAId());
        auditLog.setDetail("type=" + conflict.getConflictType()
                + " confidence=" + suggestion.getConfidence());
        auditLogService.record(auditLog);

        return suggestion;
    }

    private String buildContentFromConflict(ConflictEntity conflict) {
        return "KO_A: " + conflict.getKoAId() + " | KO_B: " + conflict.getKoBId()
                + " | type: " + conflict.getConflictType()
                + " | scope: " + conflict.getScopeKey()
                + " | field: " + conflict.getFieldKey();
    }
}
