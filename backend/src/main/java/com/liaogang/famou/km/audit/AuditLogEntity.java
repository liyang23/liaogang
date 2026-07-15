package com.liaogang.famou.km.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志实体（v0.32 PRD §5.2.6 OQ-5 修订）。
 *
 * <p>原文件名 AuditLog.java 重命名为 AuditLogEntity.java，避免与同包内
 * {@code @interface AuditLog} 注解（{@code AuditLogAnnotation.java}）类名冲突。
 * <p>字段已简化（无 reverted_at / reverted_by / status 字段，仅 created_at）。
 * <p>FR-26 验收：审计 ≥ 12 月保留（NFR-15），按月分区（生产环境 T006 启用）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    /** 审计 ID（格式 AUDIT-{YYYYMMDD}-{NNNNNN}，按天独立自增） */
    private String id;

    /** 操作类型：KO_CREATE / KO_UPDATE / KO_DELETE / KO_VERSION_PUBLISH / KO_REVIEW / CONFLICT_RESOLVE / DICT_* / USER_ROLE_LOGIN 等 */
    private String action;

    /** 操作人 sub（来自 JWT） */
    private String userId;

    /** 操作目标 KO ID（可选） */
    private String targetKo;

    /** 变更详情（JSON 字符串） */
    private String detail;

    /** 操作理由（可选） */
    private String reason;

    /** 创建时间（v0.32 OQ-5 修订：无 reverted_at 字段） */
    private LocalDateTime createdAt;
}
