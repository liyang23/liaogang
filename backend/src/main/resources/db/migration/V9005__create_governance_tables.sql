-- U7 知识治理数据模型 (T301)
-- 6 C 类冲突 + 1 H 类健康 + LLM 配额
-- 与 V9001-V9004 schema 不冲突（顺序在 V9004 之后）

CREATE TABLE IF NOT EXISTS `km_conflict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `fingerprint` VARCHAR(12) NOT NULL COMMENT '冲突指纹 MD5(ko_a_id+ko_b_id+conflict_type+scope_key+field_key)[:12]',
    `conflict_type` VARCHAR(8) NOT NULL COMMENT 'C1/C2/C3/C4/C5/C6/H2',
    `ko_a_id` VARCHAR(32) NOT NULL COMMENT '冲突 KO 双方 A',
    `ko_b_id` VARCHAR(32) NOT NULL COMMENT '冲突 KO 双方 B',
    `scope_key` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '作用域 (ko_type:xxx / prm:xxx / title 等)',
    `field_key` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '冲突字段名',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending / reviewing / resolved / auto_resolved',
    `confidence` DECIMAL(4,3) NULL COMMENT 'C6 置信度 <0.8 弹 Modal 二次确认 (OQ-8)',
    `llm_suggestion` TEXT NULL COMMENT 'DeepSeek v4 返回的建议 (OQ-9)',
    `llm_rationale` TEXT NULL COMMENT 'DeepSeek v4 返回的理由',
    `resolution_state` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT 'OQ-8 仲裁快路径: Draft→Review→Approved→Published',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `resolved_at` DATETIME NULL COMMENT 'resolved / auto_resolved 时填充',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fingerprint` (`fingerprint`),
    KEY `idx_status` (`status`),
    KEY `idx_ko_a` (`ko_a_id`),
    KEY `idx_ko_b` (`ko_b_id`),
    KEY `idx_type_status` (`conflict_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='冲突记录 (6 C 类 + 1 H 类)';

-- C2 模板冲突场景的 (prm_id, section_count) 索引补充
CREATE INDEX `idx_prm_section` ON `km_conflict` (`scope_key`(32), `field_key`(32));


CREATE TABLE IF NOT EXISTS `km_llm_quota` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `scope` VARCHAR(16) NOT NULL COMMENT 'platform / user',
    `scope_key` VARCHAR(64) NOT NULL COMMENT 'platform="" / user={user_id}',
    `quota_date` VARCHAR(10) NOT NULL COMMENT 'yyyy-MM-dd (每日 0 点 Redis INCR 重置)',
    `used_count` INT NOT NULL DEFAULT 0 COMMENT '已用次数 (平台级 / 用户级)',
    `limit_count` INT NOT NULL COMMENT '上限 (平台级 100/天, 用户级 5/天)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scope_date` (`scope`, `scope_key`, `quota_date`),
    KEY `idx_quota_date` (`quota_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM 配额 (平台级 + 用户级 双重限制)';
