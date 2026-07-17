-- U9 项目管理 V9008 增量 (T309)
-- 在 V9002 已有 project 表基础上增:
-- 1. archived_at 字段 (区别于 is_deleted 软删除; OQ-7 归档仅冻结 KO 不影响 PRP/SNP)
-- 2. archived_by 字段 (OQ-5 审计 + OQ-7 归档操作人)
-- 3. status 字段约束保留 (active/archived 已在 V9002)

ALTER TABLE project
    ADD COLUMN archived_at DATETIME NULL COMMENT 'OQ-7: 归档时间 (active -> archived 状态切换)' AFTER updated_at,
    ADD COLUMN archived_by VARCHAR(64) NULL COMMENT 'OQ-7: 归档操作人 (来自 JWT userId)' AFTER archived_at,
    ADD KEY idx_project_status (status),
    ADD KEY idx_project_archived (archived_at);
