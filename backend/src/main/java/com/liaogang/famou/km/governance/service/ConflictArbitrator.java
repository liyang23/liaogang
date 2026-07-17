package com.liaogang.famou.km.governance.service;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 冲突仲裁服务（U7 / T302 / OQ-8 仲裁快路径：Draft→Review→Approved→Published 一次操作完成）
 *
 * <p>管理员点击"仲裁并发布" → 创建 KO Version → 系统自动 Review → 自动 Approved → 自动 Published；
 * 审计日志写 USER_CONFLICT_ARBITRATE；C6 置信度 &lt;0.8 弹 Modal 二次确认（前端在 U7 任务 T303 实现）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictArbitrator {

    private final ConflictMapper conflictMapper;
    private final AuditLogService auditLogService;

    /** OQ-8 4 状态一次性转换 */
    public static final String RESOLUTION_DRAFT = "draft";
    public static final String RESOLUTION_REVIEW = "review";
    public static final String RESOLUTION_APPROVED = "approved";
    public static final String RESOLUTION_PUBLISHED = "published";

    /** C6 置信度阈值（&lt;0.8 弹 Modal 二次确认） */
    public static final double CONFIDENCE_CONFIRM_THRESHOLD = 0.8;

    /**
     * 仲裁并发布（OQ-8 一次操作完成 4 状态转换）
     *
     * @param conflict 冲突实体
     * @param userSub  管理员 sub
     * @return 仲裁后的 conflict 实体（status=resolved, resolution_state=published）
     */
    public ConflictEntity arbitrateAndPublish(ConflictEntity conflict, String userSub) {
        if (conflict == null) {
            throw new IllegalArgumentException("conflict is null");
        }
        // C6 置信度 <0.8 时, 前端应弹 Modal 二次确认 (T303 实现)
        // 后端不阻断 (前端 UI 责任)
        if (conflict.getConfidence() != null
                && conflict.getConfidence() < CONFIDENCE_CONFIRM_THRESHOLD) {
            log.warn("C6 conflict {} confidence {} < {}, expected frontend Modal confirm",
                    conflict.getId(), conflict.getConfidence(), CONFIDENCE_CONFIRM_THRESHOLD);
        }

        // OQ-8 快路径: 4 状态一次性自动转换
        conflict.setResolutionState(RESOLUTION_PUBLISHED);
        conflict.setStatus("resolved");
        conflict.setResolvedAt(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflictMapper.updateById(conflict);

        // 写 audit_log (USER_CONFLICT_ARBITRATE)
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction("USER_CONFLICT_ARBITRATE");
        auditLog.setUserId(userSub);
        auditLog.setTargetKo(conflict.getKoAId());
        auditLog.setDetail("conflict=" + conflict.getId()
                + " resolution=" + conflict.getResolutionState()
                + " confidence=" + conflict.getConfidence());
        auditLogService.record(auditLog);

        return conflict;
    }

    /**
     * 仅查询当前 4 状态 (OQ-8 状态机)
     */
    public static String[] getAllowedStates() {
        return new String[]{RESOLUTION_DRAFT, RESOLUTION_REVIEW, RESOLUTION_APPROVED, RESOLUTION_PUBLISHED};
    }
}
