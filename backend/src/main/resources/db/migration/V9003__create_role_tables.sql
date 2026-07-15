-- V9003__create_role_tables.sql
-- 来源：TP-2 T205（U5 权限与角色模块）
-- 目的：建立 2 张 U5 新表（role_permission + user_role）
--       role 表 V9002 已建（T201 顺带建），此处不重复 CREATE
-- 依据：PRD v0.32 §4.1.2 5 预置角色默认权限矩阵 + §4.1.3 用户角色分配

START TRANSACTION;

-- 1. role_permission 表（5 角色 × 6 KO 类型 × 5 操作 = 150 cells）
CREATE TABLE IF NOT EXISTS role_permission (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK: 自增主键',
    role_id     VARCHAR(50)   NOT NULL COMMENT 'FK → role.id',
    menu_id     VARCHAR(50)   NOT NULL COMMENT '菜单 ID 或 KO 类型（CON/RUL/PAR/SCH/PRM/DOC）',
    operation   VARCHAR(20)   NOT NULL COMMENT '操作：查阅/新增/更新/删除/审核',
    allowed     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否允许（0/1）',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu_op (role_id, menu_id, operation),
    KEY idx_rp_role (role_id),
    KEY idx_rp_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限矩阵 cell（5 角色 × 6 KO 类型 × 5 操作 = 150 cells）';

-- 2. user_role 表（用户角色分配，多对多）
CREATE TABLE IF NOT EXISTS user_role (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK: 自增主键',
    user_sub      VARCHAR(50)   NOT NULL COMMENT '用户 sub（来自 JWT）',
    role_id       VARCHAR(50)   NOT NULL COMMENT 'FK → role.id',
    assigned_by   VARCHAR(50)       NULL COMMENT '分配人 sub',
    assigned_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_sub, role_id),
    KEY idx_ur_user (user_sub),
    KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色分配（OQ-12 角色变更下次登录生效）';

COMMIT;
