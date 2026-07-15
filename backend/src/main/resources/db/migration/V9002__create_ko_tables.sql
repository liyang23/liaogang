-- V9002__create_ko_tables.sql
-- 来源：TP-2 T201（U4 KO 库数据模型 + 状态机）
-- 目的：建立 KO 库 3 张核心表（ko / ko_version / ko_references）
--       + V9001 seed 依赖的 2 张基础表（project / role，因 V9001 已 INSERT 但无对应建表 migration）
-- 依据：PRD v0.32 §5.2.1 KO 生命周期 + §10.7.7 ID/版本号格式 + §4.1 角色管理
--      + TP-1 T010 V9001 seed 实际引用

-- F-22 + F-8 修复：START TRANSACTION + COMMIT 包装 + IF NOT EXISTS 幂等
START TRANSACTION;

-- 1. project 表（V9001 seed 依赖；不在 T201 原始范围，因 V9001 隐含需要补建）
CREATE TABLE IF NOT EXISTS project (
    id                VARCHAR(50)   NOT NULL COMMENT 'PK: PROJ-0001~PROJ-0004',
    code              VARCHAR(50)   NOT NULL COMMENT '项目编码（业务唯一）',
    name              VARCHAR(200)  NOT NULL COMMENT '项目名称',
    description       TEXT              NULL COMMENT '项目描述',
    organization_code VARCHAR(50)       NULL COMMENT '组织机构编码（如 10000510 辽港集团）',
    status            VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT '项目状态: active/archived',
    is_deleted        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表（V9001 seed 依赖）';

-- 2. role 表（V9001 seed 依赖；不在 T201 原始范围，同 project 理由补建）
CREATE TABLE IF NOT EXISTS role (
    id          VARCHAR(50)  NOT NULL COMMENT 'PK: ROLE-0001~ROLE-0005',
    code        VARCHAR(50)  NOT NULL COMMENT '角色编码（业务唯一）',
    name        VARCHAR(100) NOT NULL COMMENT '角色名称',
    description TEXT             NULL COMMENT '角色描述',
    is_builtin  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否预置角色（不可删除）',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表（V9001 seed 依赖）';

-- 3. ko 表（KO 库主表；6 类型 CON/RUL/PAR/SCH/PRM/DOC 共 278 条）
CREATE TABLE IF NOT EXISTS ko (
    id            VARCHAR(50)   NOT NULL COMMENT 'PK: KO-{TYPE}-{NNNN}（§10.7.7.1 按类型独立自增）',
    type          VARCHAR(10)   NOT NULL COMMENT 'KO 类型: CON/RUL/PAR/SCH/PRM/DOC',
    title         VARCHAR(200)  NOT NULL COMMENT 'KO 标题',
    code          VARCHAR(50)   NOT NULL COMMENT 'KO 编码（业务唯一）',
    project_id    VARCHAR(50)   NOT NULL COMMENT '所属项目 ID（FK → project.id）',
    definition    TEXT              NULL COMMENT '形式化定义（JSON 或数学表达式）',
    effect        VARCHAR(50)       NULL COMMENT '约束效力: Hard/Soft/Normative',
    level         VARCHAR(10)       NULL COMMENT 'KO 层级: L1/L2/L3',
    organization  VARCHAR(50)       NULL COMMENT '所属组织',
    status        VARCHAR(20)   NOT NULL DEFAULT 'Draft' COMMENT '状态: Draft/Review/Approved/Published/Active（§5.2.1.3）',
    version       VARCHAR(20)   NOT NULL DEFAULT 'v1.0.0' COMMENT '当前版本（§10.7.7.5 v{MAJOR}.{MINOR}.{PATCH}）',
    is_deleted    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ko_code (code),
    KEY idx_ko_project (project_id),
    KEY idx_ko_type (type),
    KEY idx_ko_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KO 库主表（6 类型 CON/RUL/PAR/SCH/PRM/DOC）';

-- 4. ko_version 表（KO 版本历史；编辑触发版本号递增 §10.7.7.5）
CREATE TABLE IF NOT EXISTS ko_version (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK: 自增主键',
    ko_id       VARCHAR(50)   NOT NULL COMMENT 'FK → ko.id',
    version     VARCHAR(20)   NOT NULL COMMENT '本次版本号（v{MAJOR}.{MINOR}.{PATCH}）',
    title       VARCHAR(200)  NOT NULL COMMENT '本次版本标题',
    definition  TEXT              NULL COMMENT '本次版本形式化定义',
    status      VARCHAR(20)   NOT NULL DEFAULT 'Draft' COMMENT '本次版本状态: Draft/Review/Approved/Published',
    created_by  VARCHAR(50)   NOT NULL COMMENT '创建人 sub（来自 JWT）',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ko_version (ko_id, version),
    KEY idx_ko_version_ko (ko_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KO 版本历史（编辑触发版本号递增）';

-- 5. ko_references 表（KO 引用关系：DEPENDENCY/REFERENCE/CONFLICT）
CREATE TABLE IF NOT EXISTS ko_references (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK: 自增主键',
    source_ko_id   VARCHAR(50)  NOT NULL COMMENT '源 KO ID（FK → ko.id）',
    target_ko_id   VARCHAR(50)  NOT NULL COMMENT '目标 KO ID（FK → ko.id）',
    relation_type  VARCHAR(20)  NOT NULL COMMENT '关系类型: DEPENDENCY/REFERENCE/CONFLICT',
    description    TEXT             NULL COMMENT '关系描述',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ko_reference (source_ko_id, target_ko_id, relation_type),
    KEY idx_ko_ref_source (source_ko_id),
    KEY idx_ko_ref_target (target_ko_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KO 引用关系（DEPENDENCY/REFERENCE/CONFLICT）';

COMMIT;
