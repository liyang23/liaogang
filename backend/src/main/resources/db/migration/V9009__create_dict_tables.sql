-- U9 字典管理数据模型 (T310)
-- 6 字典 + 9 预制量纲
-- 与 V9001-V9008 schema 不冲突（顺序在 V9008 之后）

CREATE TABLE IF NOT EXISTS `km_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dict_type` VARCHAR(32) NOT NULL COMMENT '类型介绍 / 效力分级 / 权威分级 / 类型分组 / 知识对象概念 / 量纲配置',
    `code` VARCHAR(64) NOT NULL COMMENT '字典项编码 (如 EFFECT_LEVEL_HIGH)',
    `name` VARCHAR(200) NOT NULL COMMENT '字典项名称 (如 高)',
    `description` TEXT NULL COMMENT '字典项描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `disabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记 (0=启用, 1=停用)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_code` (`dict_type`, `code`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_disabled` (`disabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表 (6 字典)';


CREATE TABLE IF NOT EXISTS `km_unit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` VARCHAR(32) NOT NULL COMMENT '量纲编码 (如 PERCENT, YUAN_TEU)',
    `name` VARCHAR(64) NOT NULL COMMENT '量纲名称 (如 %, 元/TEU)',
    `symbol` VARCHAR(16) NULL COMMENT '量纲符号 (如 %, h)',
    `description` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unit_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='9 预制量纲 (部署门禁)';


-- 9 预制量纲 seed (应用启动门禁 - 缺失即抛异常退出)
INSERT IGNORE INTO `km_unit` (`code`, `name`, `symbol`) VALUES
    ('PERCENT', '百分比', '%'),
    ('YUAN_TEU', '元/TEU', '¥/TEU'),
    ('HOUR', '小时', 'h'),
    ('PIECE', '条', NULL),
    ('CRANE_PER_HOUR', '辆/岸桥', '辆/h'),
    ('ROW', '栏', NULL),
    ('PERSON_PER_SHIFT', '人/班', '人/班'),
    ('MACHINE_PER_SHIFT', '台/班', '台/班'),
    ('BOX_PER_HOUR', '箱/h', '箱/h');
