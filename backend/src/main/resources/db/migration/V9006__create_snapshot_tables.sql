-- U8 提示词快照数据模型 (T305)
-- SNP = 装配方案版本 (hash 命中复用)
-- PRP = 每次渲染一条
-- 与 V9005__create_governance_tables.sql 同 wave (V9005 之后)

CREATE TABLE IF NOT EXISTS `km_prompt_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `hash` VARCHAR(16) NOT NULL COMMENT 'ko_assembly_hash SHA256[:16]',
    `prm_id` VARCHAR(32) NOT NULL COMMENT 'KO-PRM-0001 / KO-PRM-0002 / KO-PRM-0003',
    `prm_version` VARCHAR(16) NOT NULL COMMENT 'v1.0 / v2.0 / v3.0',
    `rendered_text_canonical` TEXT NOT NULL COMMENT '渲染后内容 (含 {{var}} 替换或字面量)',
    `ko_ids` JSON NOT NULL COMMENT 'sorted(ko_ids[])',
    `ko_versions` JSON NOT NULL COMMENT 'sorted(ko_versions[])',
    `ko_field_values` JSON NOT NULL COMMENT 'sorted(ko_field_values[])',
    `manual_subitems_hash` VARCHAR(12) NOT NULL DEFAULT '' COMMENT 'Q-I4 §3 ManualSubItems array schema 派生的 hash',
    `var_bindings` JSON NOT NULL COMMENT 'sorted(var_bindings[])',
    `stale` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'false=正常, true=PAR 变更后陈旧',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hash` (`hash`),
    KEY `idx_prm` (`prm_id`),
    KEY `idx_prm_version` (`prm_id`, `prm_version`),
    KEY `idx_stale` (`stale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词快照 (SNP - 装配方案版本)';


CREATE TABLE IF NOT EXISTS `km_prompt_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `snapshot_hash` VARCHAR(16) NOT NULL COMMENT '关联 km_prompt_snapshot.hash',
    `rendered_text` TEXT NOT NULL COMMENT '每次渲染的具体输出',
    `render_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `char_count` INT NOT NULL DEFAULT 0,
    `token_count` INT NOT NULL DEFAULT 0,
    `user_id` VARCHAR(64) NOT NULL COMMENT '操作人 sub (来自 JWT)',
    `reason` VARCHAR(255) NULL COMMENT 'OQ-5 强制渲染理由 (≥10 字符)',
    `force_rendered` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_snapshot` (`snapshot_hash`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词渲染记录 (PRP)';
