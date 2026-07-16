-- V9004__create_prm_tables.sql
-- 来源：TP-2 T208（U6 提示词系统模块）
-- 目的：建立 PRM 模板 2 张表（prm_template + prm_section）
-- 依据：PRD v0.32 §5.2.4 PRM 模板 + Section + §10.7.7 ID 格式

START TRANSACTION;

-- 1. prm_template 表（PRM 模板主表；3 预置 KO-PRM-0001/0002/0003）
CREATE TABLE IF NOT EXISTS prm_template (
    id          VARCHAR(50)   NOT NULL COMMENT 'PK: KO-PRM-0001/0002/0003',
    name        VARCHAR(200)  NOT NULL COMMENT '模板名称',
    description TEXT              NULL COMMENT '模板描述',
    version     VARCHAR(20)   NOT NULL DEFAULT 'v1.0.0' COMMENT '版本',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PRM 提示词模板主表（3 预置）';

-- 2. prm_section 表（PRM 模板的章节；17 段 = 9+3+5）
CREATE TABLE IF NOT EXISTS prm_section (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK: 自增主键',
    template_id   VARCHAR(50)   NOT NULL COMMENT 'FK → prm_template.id',
    section_index INT           NOT NULL COMMENT 'Section 序号（1-9 / 1-3 / 1-5）',
    title         VARCHAR(200)  NOT NULL COMMENT 'Section 标题',
    section_type  VARCHAR(20)   NOT NULL COMMENT '类型: FIXED（变量赋值）/ DYNAMIC（动态选择）',
    content       TEXT          NOT NULL COMMENT 'Section 内容模板（含 {{var}} / {{#each}} 等）',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_prm_section (template_id, section_index),
    KEY idx_prm_section_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PRM 模板 Section（17 段：9+3+5）';

COMMIT;
