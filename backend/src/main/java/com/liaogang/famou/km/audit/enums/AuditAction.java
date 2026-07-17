package com.liaogang.famou.km.audit.enums;

/**
 * 审计操作类型枚举 (U9 / T308)
 *
 * <p>集中管理所有 AuditLogService.record() 使用的 action 字符串，避免分散在调用方硬编码
 * (F-FEAS-R2-016 spec-doc-review P0 fix: 字符串 action 容易拼写错导致审计漏数据)。
 */
public enum AuditAction {
    KO_CREATE,
    KO_UPDATE,
    KO_DELETE,
    KO_VERSION_PUBLISH,
    KO_REVIEW,
    CONFLICT_RESOLVE,
    USER_CONFLICT_ARBITRATE,
    USER_ROLE_LOGIN,
    USER_ROLE_CHANGE,
    LLM_SUGGEST_REQUESTED,
    SNP_STALE_DETECTED,
    DICT_CREATE,
    DICT_UPDATE,
    DICT_DELETE,
    PROJECT_CREATE,
    PROJECT_ARCHIVE,
    DOC_PREVIEW_ACCESSED,
}
