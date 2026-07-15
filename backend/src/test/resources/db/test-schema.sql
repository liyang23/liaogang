-- test-schema.sql（H2 兼容）
-- F-30 修复：KoControllerIT 不跑 Flyway migration（MySQL DDL 与 H2 不兼容），
--         直接用 H2 兼容语法建表用于集成测试
-- 注：与 V9002__create_ko_tables.sql 字段一致，但去掉 MySQL 专属语法

CREATE TABLE IF NOT EXISTS project (
    id                VARCHAR(50)   NOT NULL,
    code              VARCHAR(50)   NOT NULL,
    name              VARCHAR(200)  NOT NULL,
    description       TEXT              NULL,
    organization_code VARCHAR(50)       NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'active',
    is_deleted        INT           NOT NULL DEFAULT 0,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS role (
    id          VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT             NULL,
    is_builtin  INT          NOT NULL DEFAULT 0,
    is_deleted  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ko (
    id            VARCHAR(50)   NOT NULL,
    type          VARCHAR(10)   NOT NULL,
    title         VARCHAR(200)  NOT NULL,
    code          VARCHAR(50)   NOT NULL,
    project_id    VARCHAR(50)   NOT NULL,
    definition    TEXT              NULL,
    effect        VARCHAR(50)       NULL,
    level         VARCHAR(10)       NULL,
    organization  VARCHAR(50)       NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'Draft',
    version       VARCHAR(20)   NOT NULL DEFAULT 'v1.0.0',
    is_deleted    INT           NOT NULL DEFAULT 0,
    created_by    VARCHAR(50)       NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ko_version (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    ko_id       VARCHAR(50)   NOT NULL,
    version     VARCHAR(20)   NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    definition  TEXT              NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'Draft',
    created_by  VARCHAR(50)   NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ko_references (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    source_ko_id   VARCHAR(50)  NOT NULL,
    target_ko_id   VARCHAR(50)  NOT NULL,
    relation_type  VARCHAR(20)  NOT NULL,
    description    TEXT             NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);