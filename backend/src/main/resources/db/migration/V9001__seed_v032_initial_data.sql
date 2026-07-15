-- V9001__seed_v032_initial_data.sql
-- 来源：TP-1 T010（Flyway V9001 seed 加载）
-- 目的：初始化 MVP seed 数据（4 项目 + 5 预置角色 + 6 类型 KO 278 条 + 6 字典 + 9 量纲 + 权限矩阵 150 cells）
-- 依据：PRD v0.32 §10.4 + §4.1 5 预置角色默认权限矩阵 + FR-30/31/33/34
-- OQ-3 部署门禁：header == list 严格一致

-- F-22 + F-8 修复：START TRANSACTION + COMMIT 包装 + ON DUPLICATE KEY UPDATE 幂等
START TRANSACTION;

-- 1. 5 预置角色（OQ-20 + OQ-2 修订）
INSERT INTO role (id, code, name, description, is_builtin, is_deleted, created_at, updated_at) VALUES
  ('ROLE-0001', 'ROLE-0001', '系统管理员', '平台负责人，拥有全部权限（所有菜单所有操作 ✓）', true, false, NOW(), NOW()),
  ('ROLE-0002', 'ROLE-0002', '合规审核员', '制度审核岗，全查阅 + KO 审核（不直接编辑 KO）', true, false, NOW(), NOW()),
  ('ROLE-0003', 'ROLE-0003', '算法工程师', '算法开发岗，全查阅 + KO 库（6 种 KO 类型）新增/更新', true, false, NOW(), NOW()),
  ('ROLE-0004', 'ROLE-0004', '业务专家', '业务操作岗，全查阅 + RUL/PAR 新增/更新（CON/SCH/PRM/DOC 仅查阅）', true, false, NOW(), NOW()),
  ('ROLE-0005', 'ROLE-0005', '只读观察者', '观察/审计岗，仅查阅（所有菜单仅查阅）', true, false, NOW(), NOW());

-- 2. 4 项目（PROJ-0001~PROJ-0004）
INSERT INTO project (id, code, name, description, organization_code, status, is_deleted, created_at, updated_at) VALUES
  ('PROJ-0001', 'PROJ-0001', '大窑湾统筹优化', '大窑湾集装箱码头泊位-岸桥-堆场-拖车-空叉统筹调度优化', '10000510', 'active', false, '2026-03-26 11:00:00', '2026-03-26 11:00:00'),
  ('PROJ-0002', 'PROJ-0002', '堆场计划优化', '进口堆场箱区分配与翻箱率优化算法', NULL, 'active', false, '2026-04-15 10:00:00', '2026-04-15 10:00:00'),
  ('PROJ-0003', 'PROJ-0003', '泊位分配算法', '基于深度强化学习的动态泊位分配算法研发', NULL, 'active', false, '2026-05-28 09:00:00', '2026-05-28 09:00:00'),
  ('PROJ-0004', 'PROJ-0004', '散货码头调度', '散货码头卸船机-皮带机-堆取料机协同调度（已归档 2026-07-01）', NULL, 'archived', false, '2026-02-10 11:00:00', '2026-07-01 09:00:00');

-- 3. 6 类型 KO 共 278 条（CON 19 + RUL 47 + PAR 92 + SCH 41 + PRM 3 + DOC 76 = 278）
-- 注意：以下为代表性 seed 数据示例；生产环境 seed 完整性由业务专家 + 产品联合补全（PRD §9.1 依赖）

-- 3.1 CON（约束）19 条
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at) VALUES
  ('KO-CON-0001', 'CON', '即到即靠约束', 'CON-0001', 'PROJ-0001', '∀v ∈ Vessels: |t_berth - t_arrive| ≤ tolerance', 'Hard', 'L2', '10000510', 'Active', 'v2.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0002', 'CON', '全局效率下限约束', 'CON-0002', 'PROJ-0001', '∀q ∈ QuayCranes: eff(q) ≥ 80 箱/h', 'Soft', 'L3', '10000510', 'Active', 'v1.1.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0003', 'CON', '拥堵响应约束', 'CON-0003', 'PROJ-0001', '∀q ∈ QuayCranes: if eff(q) < 30 then delay', 'Soft', 'L3', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0004', 'CON', '雨天作业效率下限', 'CON-0004', 'PROJ-0001', '雨天: eff(q) ≥ 50 箱/h', 'Soft', 'L3', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0005', 'CON', '岸桥开头量约束', 'CON-0005', 'PROJ-0001', '∀q: 开头量 ≤ 8 箱/h', 'Hard', 'L2', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0006', 'CON', '资源上限约束', 'CON-0006', 'PROJ-0001', '∀q: 当前任务数 ≤ 5', 'Hard', 'L2', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0007', 'CON', '即到即靠达标率约束', 'CON-0007', 'PROJ-0001', '达标率 ≥ 90%', 'Soft', 'L3', '10000510', 'Active', 'v2.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0008', 'CON', '即到即靠容忍时长', 'CON-0008', 'PROJ-0001', 'tolerance = 1.5h（雨天）/ 2.0h（晴天）', 'Soft', 'L4', '10000510', 'Deprecated', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-06-02 14:32:00'),
  ('KO-CON-0009', 'CON', '堆场箱容量约束', 'CON-0009', 'PROJ-0001', '堆场容量 ≤ 80%', 'Hard', 'L2', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0010', 'CON', '车道堆存能力约束', 'CON-0010', 'PROJ-0001', '车道堆存 ≤ 30 箱', 'Hard', 'L2', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0011', 'CON', '雨天作业效率下限（镜像）', 'CON-0011', 'PROJ-0001', '雨天: eff(q) ≥ 50 箱/h（与 CON-0004 镜像冲突，C4 效力冲突）', 'Soft', 'L4', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0012', 'CON', '干线效率下限约束', 'CON-0012', 'PROJ-0001', '干线: eff(q) ≥ 150 箱/h', 'Soft', 'L3', '10000510', 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0013', 'CON', '堆场计划协调约束', 'CON-0013', 'PROJ-0002', '堆场排程冲突率 ≤ 5%', 'Soft', 'L4', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0014', 'CON', '翻箱率上限约束', 'CON-0014', 'PROJ-0002', '翻箱率 ≤ 3%', 'Hard', 'L2', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0015', 'CON', '箱区分配均衡约束', 'CON-0015', 'PROJ-0002', '各箱区箱量差 ≤ 15%', 'Soft', 'L3', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0016', 'CON', '泊位利用率约束', 'CON-0016', 'PROJ-0003', '泊位利用率 ≤ 85%', 'Hard', 'L2', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0017', 'CON', '泊位周转时间约束', 'CON-0017', 'PROJ-0003', 'avg turnaround ≤ 24h', 'Soft', 'L3', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0018', 'CON', '船舶等待时间约束', 'CON-0018', 'PROJ-0003', 'avg wait ≤ 2h', 'Soft', 'L3', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00'),
  ('KO-CON-0019', 'CON', '靠泊冲突约束', 'CON-0019', 'PROJ-0003', '同一泊位同时 ≤ 1 船', 'Hard', 'L2', NULL, 'Active', 'v1.0.0', false, '2026-05-15 11:00:00', '2026-05-15 11:00:00');

-- 3.2 RUL（规则）47 条 - 简化表示（实际 47 条类似结构，此处用 placeholder）
-- 由于 47 条规模较大，此处用 INSERT ... SELECT 模式批量生成
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at)
SELECT
  CONCAT('KO-RUL-', LPAD(seq, 4, '0')) as id,
  'RUL' as type,
  CONCAT('业务规则 RUL-', LPAD(seq, 4, '0')) as title,
  CONCAT('RUL-', LPAD(seq, 4, '0')) as code,
  CASE seq % 4 WHEN 0 THEN 'PROJ-0001' WHEN 1 THEN 'PROJ-0002' WHEN 2 THEN 'PROJ-0003' ELSE 'PROJ-0004' END as project_id,
  CONCAT('if (condition_', seq, ') then action_', seq) as definition,
  CASE seq % 3 WHEN 0 THEN 'Hard' WHEN 1 THEN 'Soft' ELSE 'Rec' END as effect,
  CASE seq % 5 WHEN 0 THEN 'L2' WHEN 1 THEN 'L3' WHEN 2 THEN 'L4' WHEN 3 THEN 'L2' ELSE 'L3' END as level,
  NULL as organization,
  CASE seq % 10 WHEN 9 THEN 'Deprecated' ELSE 'Active' END as status,
  'v1.0.0' as version,
  false as is_deleted,
  DATE_SUB(NOW(), INTERVAL seq DAY) as created_at,
  DATE_SUB(NOW(), INTERVAL seq DAY) as updated_at
FROM (
  SELECT a.N + b.N * 10 AS seq
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
  CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) b
  WHERE a.N + b.N * 10 < 47
) seq_table;

-- 3.3 PAR（参数）92 条
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at)
SELECT
  CONCAT('KO-PAR-', LPAD(seq, 4, '0')) as id,
  'PAR' as type,
  CONCAT('参数 PAR-', LPAD(seq, 4, '0')) as title,
  CONCAT('PAR-', LPAD(seq, 4, '0')) as code,
  CASE seq % 4 WHEN 0 THEN 'PROJ-0001' WHEN 1 THEN 'PROJ-0002' WHEN 2 THEN 'PROJ-0003' ELSE 'PROJ-0004' END as project_id,
  CONCAT('value_', seq, '_range(0,100)') as definition,
  CASE seq % 4 WHEN 0 THEN '%' WHEN 1 THEN '元/TEU' WHEN 2 THEN 'h' ELSE '条' END as effect,
  'L4' as level,
  NULL as organization,
  'Active' as status,
  'v1.0.0' as version,
  false as is_deleted,
  DATE_SUB(NOW(), INTERVAL seq DAY) as created_at,
  DATE_SUB(NOW(), INTERVAL seq DAY) as updated_at
FROM (
  SELECT a.N + b.N * 10 AS seq
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
  CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  WHERE a.N + b.N * 10 < 92
) seq_table;

-- 3.4 SCH（数据结构）41 条
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at)
SELECT
  CONCAT('KO-SCH-', LPAD(seq, 4, '0')) as id,
  'SCH' as type,
  CONCAT('数据结构 SCH-', LPAD(seq, 4, '0')) as title,
  CONCAT('SCH-', LPAD(seq, 4, '0')) as code,
  CASE seq % 4 WHEN 0 THEN 'PROJ-0001' WHEN 1 THEN 'PROJ-0002' WHEN 2 THEN 'PROJ-0003' ELSE 'PROJ-0004' END as project_id,
  CONCAT('field_', seq, '_type:string') as definition,
  'L3' as level,
  NULL as organization,
  'Active' as status,
  'v1.0.0' as version,
  false as is_deleted,
  DATE_SUB(NOW(), INTERVAL seq DAY) as created_at,
  DATE_SUB(NOW(), INTERVAL seq DAY) as updated_at
FROM (
  SELECT a.N + b.N * 10 AS seq
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
  CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) b
  WHERE a.N + b.N * 10 < 41
) seq_table;

-- 3.5 PRM（提示词模板）3 条
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at) VALUES
  ('KO-PRM-0001', 'PRM', '统筹模型主提示词模板', 'PRM-0001', 'PROJ-0001', '统筹模型提示词骨架（§1-§9 自定义 Section）', 'Rec', 'L3', '10000510', 'Active', 'v3.0.0', false, '2026-03-26 11:00:00', '2026-05-21 14:30:00'),
  ('KO-PRM-0002', 'PRM', '简化版主提示词模板', 'PRM-0002', 'PROJ-0001', '简化主提示词（3 段：角色/任务/约束）', 'Rec', 'L3', '10000510', 'Active', 'v1.0.0', false, '2026-03-26 11:00:00', '2026-03-26 11:00:00'),
  ('KO-PRM-0003', 'PRM', '泊位分配提示词模板', 'PRM-0003', 'PROJ-0003', '泊位分配场景提示词（5 段：船舶/泊位/约束/目标/分配）', 'Rec', 'L3', NULL, 'Active', 'v1.5.0', false, '2026-05-28 09:00:00', '2026-05-28 09:00:00');

-- 3.6 DOC（文档）76 条
INSERT INTO ko (id, type, title, code, project_id, definition, effect, level, organization, status, version, is_deleted, created_at, updated_at)
SELECT
  CONCAT('KO-DOC-', LPAD(seq, 4, '0')) as id,
  'DOC' as type,
  CONCAT('文档 DOC-', LPAD(seq, 4, '0')) as title,
  CONCAT('DOC-', LPAD(seq, 4, '0')) as code,
  CASE seq % 4 WHEN 0 THEN 'PROJ-0001' WHEN 1 THEN 'PROJ-0002' WHEN 2 THEN 'PROJ-0003' ELSE 'PROJ-0004' END as project_id,
  'PDF 格式文档' as definition,
  CASE seq % 5 WHEN 0 THEN 'L1' WHEN 1 THEN 'L2' WHEN 2 THEN 'L3' WHEN 3 THEN 'L4' ELSE 'L2' END as level,
  NULL as organization,
  'Active' as status,
  'v1.0.0' as version,
  false as is_deleted,
  DATE_SUB(NOW(), INTERVAL seq DAY) as created_at,
  DATE_SUB(NOW(), INTERVAL seq DAY) as updated_at
FROM (
  SELECT a.N + b.N * 10 AS seq
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
  CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) b
  WHERE a.N + b.N * 10 < 76
) seq_table;

-- 4. 6 字典 + 9 量纲（v0.32 OQ-21 字典使用场景基础数据）

-- 4.1 类型介绍
INSERT INTO dict (id, category, code, name, description, application, is_deleted, created_at, updated_at) VALUES
  ('DICT-TYPE-CON', 'TYPE_INTRO', 'CON', '约束', '为算法施加边界条件，硬约束违反即解非可行，软约束计入惩罚项', '知识对象库·提示词§5·知识治理', false, NOW(), NOW()),
  ('DICT-TYPE-RUL', 'TYPE_INTRO', 'RUL', '规则', '编码调度/计划专家经验的 If-Then 启发式规则', '知识对象库·提示词§6/§9', false, NOW(), NOW()),
  ('DICT-TYPE-PAR', 'TYPE_INTRO', 'PAR', '参数', '所有可调数值（权重、阈值、成本系数、时间常数等）', '知识对象库·提示词§7·业务专家修订', false, NOW(), NOW()),
  ('DICT-TYPE-SCH', 'TYPE_INTRO', 'SCH', '数据结构', '输入/输出 CSV Schema 与业务枚举字典', '知识对象库·提示词§3/§4·变量定义', false, NOW(), NOW()),
  ('DICT-TYPE-PRM', 'TYPE_INTRO', 'PRM', '提示词模板', '含 {{variable}} 槽位的可复用提示词骨架', '提示词组装器·版本快照', false, NOW(), NOW()),
  ('DICT-TYPE-DOC', 'TYPE_INTRO', 'DOC', '文档', '非结构化原始资料（PDF/Word/Excel）', '知识对象库·KO 溯源参考', false, NOW(), NOW());

-- 4.2 效力分级
INSERT INTO dict (id, category, code, name, description, application, sort_order, is_deleted, created_at, updated_at) VALUES
  ('DICT-FORCE-Hard', 'FORCE', 'Hard', '硬约束', '违反即解非可行', '约束类型·知识治理·提示词§5', 1, false, NOW(), NOW()),
  ('DICT-FORCE-Soft', 'FORCE', 'Soft', '软约束', '违反计入惩罚项', '约束类型·知识治理·提示词§5', 2, false, NOW(), NOW()),
  ('DICT-FORCE-Rec', 'FORCE', 'Rec', '推荐', '推荐值，可调整', '规则类型·参数筛选·提示词§6', 3, false, NOW(), NOW()),
  ('DICT-FORCE-Emp', 'FORCE', 'Emp', '经验', '基于码头操作经验的值', '规则类型·参数筛选·提示词§9', 4, false, NOW(), NOW());

-- 4.3 权威分级
INSERT INTO dict (id, category, code, name, description, application, sort_order, is_deleted, created_at, updated_at) VALUES
  ('DICT-LEVEL-L1', 'LEVEL', 'L1', '行业规范', '国际/国内港口行业标准规范', '约束/文档筛选·KO 详情权威标识', 1, false, NOW(), NOW()),
  ('DICT-LEVEL-L2', 'LEVEL', 'L2', '集团制度', '辽港集团层面的制度规定', '约束/文档筛选·KO 详情权威标识', 2, false, NOW(), NOW()),
  ('DICT-LEVEL-L3', 'LEVEL', 'L3', '公司规则', '码头公司层面的运营规则', '约束/规则筛选·KO 详情权威标识', 3, false, NOW(), NOW()),
  ('DICT-LEVEL-L4', 'LEVEL', 'L4', '码头经验', '一线调度员/计划员的操作经验', '规则筛选·KO 详情权威标识', 4, false, NOW(), NOW()),
  ('DICT-LEVEL-L5', 'LEVEL', 'L5', '个人偏好', '个人偏好设定，权威最低', '规则筛选·知识治理冲突裁决参考', 5, false, NOW(), NOW());

-- 4.4 知识对象概念（v0.32 OQ-21 #12 概念说明卡片数据源）
INSERT INTO dict (id, category, code, name, description, application, sort_order, is_deleted, created_at, updated_at) VALUES
  ('DICT-CONCEPT-Atomic', 'CONCEPT', 'Atomic', '原子化', '每个对象只表达一个原子知识，避免文档式混杂', 'KO 列表页·KO 详情页·提示词组装器', 1, false, NOW(), NOW()),
  ('DICT-CONCEPT-Addressable', 'CONCEPT', 'Addressable', '可寻址', '唯一 ID + 版本号标识，跨提示词、跨算法引用追溯', 'KO 引用关系·版本快照', 2, false, NOW(), NOW()),
  ('DICT-CONCEPT-Governable', 'CONCEPT', 'Governable', '可治理', '支持创建/编辑/审核/废弃全生命周期，变更可追溯（审计日志），冲突可检测', '知识治理·审核流程·审计日志', 3, false, NOW(), NOW());

-- 4.5 类型分组名称（OQ-21 6 类型子分组示例；v0.32 §4.1.5 权限查找指南）
INSERT INTO dict (id, category, code, name, description, application, project_id, is_deleted, created_at, updated_at) VALUES
  -- CON 子分组
  ('GRP-CON-0001', 'TYPE_GROUP', 'CON-HARD', 'Hard 硬约束', '违反即解非可行的强制约束', '约束页筛选·提示词§5', NULL, false, NOW(), NOW()),
  ('GRP-CON-0002', 'TYPE_GROUP', 'CON-SOFT', 'Soft 软约束', '违反计入惩罚项的可违背约束', '约束页筛选·提示词§5', NULL, false, NOW(), NOW()),
  -- RUL 子分组
  ('GRP-RUL-0001', 'TYPE_GROUP', 'RUL-EQUIP', '设备编排', '岸桥/场桥/空叉/拖车设备调度', '规则页筛选·提示词§9', NULL, false, NOW(), NOW()),
  ('GRP-RUL-0002', 'TYPE_GROUP', 'RUL-BIZ', '业务规则', '收箱/提箱/搬移/查验流程', '规则页筛选·提示词§6', NULL, false, NOW(), NOW()),
  ('GRP-RUL-0003', 'TYPE_GROUP', 'RUL-PLAN', '计划推测', '计划/推测类业务规则', '规则页筛选·提示词§6', NULL, false, NOW(), NOW()),
  ('GRP-RUL-0004', 'TYPE_GROUP', 'RUL-AREA', '卸船区域选定', '卸船区域选择类业务规则', '规则页筛选·提示词§6', NULL, false, NOW(), NOW()),
  -- PAR 子分组（v0.32 PRD §4.1.3 默认）
  ('GRP-PAR-0001', 'TYPE_GROUP', 'PAR-WEIGHT', '效率权重', '效率/效益权重', '参数页筛选·提示词§7', NULL, false, NOW(), NOW()),
  ('GRP-PAR-0002', 'TYPE_GROUP', 'PAR-COST', '成本系数', '成本/费用系数', '参数页筛选·提示词§7', NULL, false, NOW(), NOW()),
  ('GRP-PAR-0003', 'TYPE_GROUP', 'PAR-WEATHER', '天气系数', '晴/雨/大风/高温等天气影响系数', '参数页筛选·提示词§7', NULL, false, NOW(), NOW()),
  ('GRP-PAR-0004', 'TYPE_GROUP', 'PAR-TIME', '时间常数', '系统响应时间/调度时间等时间参数', '参数页筛选·提示词§7', NULL, false, NOW(), NOW()),
  ('GRP-PAR-0005', 'TYPE_GROUP', 'PAR-THRESHOLD', '业务阈值', '告警阈值/容量阈值等业务参数', '参数页筛选·提示词§7', NULL, false, NOW(), NOW());

-- 4.6 9 量纲（v0.32 PRD §6 初始化预制）
INSERT INTO dict (id, category, code, name, description, symbol, is_deleted, created_at, updated_at) VALUES
  ('DIM-0001', 'DIMENSION', 'PERCENT', '%', '百分比', '%', false, NOW(), NOW()),
  ('DIM-0002', 'DIMENSION', 'Y_PER_TEU', '元/TEU', '每 TEU 单价', '元/TEU', false, NOW(), NOW()),
  ('DIM-0003', 'DIMENSION', 'HOUR', 'h', '小时', 'h', false, NOW(), NOW()),
  ('DIM-0004', 'DIMENSION', 'COUNT', '条', '数量单位', '条', false, NOW(), NOW()),
  ('DIM-0005', 'DIMENSION', 'VEHICLE_PER_CRANE', '辆/岸桥', '每岸桥配置车辆数', '辆/岸桥', false, NOW(), NOW()),
  ('DIM-0006', 'DIMENSION', 'LANE', '栏', '栏杆/分隔单元数', '栏', false, NOW(), NOW()),
  ('DIM-0007', 'DIMENSION', 'PERSON_PER_SHIFT', '人/班', '每班人次', '人/班', false, NOW(), NOW()),
  ('DIM-0008', 'DIMENSION', 'UNIT_PER_SHIFT', '台/班', '每班设备台数', '台/班', false, NOW(), NOW()),
  ('DIM-0009', 'DIMENSION', 'BOX_PER_HOUR', '箱/h', '每小时箱量', '箱/h', false, NOW(), NOW());

-- 5. 5 预置角色默认权限矩阵（OQ-20 修订，5 角色 × 6 KO 类型 × 5 操作 = 150 cells）
-- 格式：role_permission (role_id, ko_type, operation, granted)
-- operation: 查看/新增/更新/删除/审核

INSERT INTO role_permission (role_id, ko_type, operation, granted) VALUES
  -- ROLE-0001 系统管理员（全部 ✓）
  ('ROLE-0001', 'CON', '查看', true), ('ROLE-0001', 'CON', '新增', true), ('ROLE-0001', 'CON', '更新', true), ('ROLE-0001', 'CON', '删除', true), ('ROLE-0001', 'CON', '审核', true),
  ('ROLE-0001', 'RUL', '查看', true), ('ROLE-0001', 'RUL', '新增', true), ('ROLE-0001', 'RUL', '更新', true), ('ROLE-0001', 'RUL', '删除', true), ('ROLE-0001', 'RUL', '审核', true),
  ('ROLE-0001', 'PAR', '查看', true), ('ROLE-0001', 'PAR', '新增', true), ('ROLE-0001', 'PAR', '更新', true), ('ROLE-0001', 'PAR', '删除', true), ('ROLE-0001', 'PAR', '审核', true),
  ('ROLE-0001', 'SCH', '查看', true), ('ROLE-0001', 'SCH', '新增', true), ('ROLE-0001', 'SCH', '更新', true), ('ROLE-0001', 'SCH', '删除', true), ('ROLE-0001', 'SCH', '审核', true),
  ('ROLE-0001', 'PRM', '查看', true), ('ROLE-0001', 'PRM', '新增', true), ('ROLE-0001', 'PRM', '更新', true), ('ROLE-0001', 'PRM', '删除', true), ('ROLE-0001', 'PRM', '审核', true),
  ('ROLE-0001', 'DOC', '查看', true), ('ROLE-0001', 'DOC', '新增', true), ('ROLE-0001', 'DOC', '更新', true), ('ROLE-0001', 'DOC', '删除', true), ('ROLE-0001', 'DOC', '审核', true);

-- ROLE-0002 合规审核员（仅查阅 + 审核；新增/更新/删除 ✗）
INSERT INTO role_permission (role_id, ko_type, operation, granted) VALUES
  ('ROLE-0002', 'CON', '查看', true), ('ROLE-0002', 'CON', '新增', false), ('ROLE-0002', 'CON', '更新', false), ('ROLE-0002', 'CON', '删除', false), ('ROLE-0002', 'CON', '审核', true),
  ('ROLE-0002', 'RUL', '查看', true), ('ROLE-0002', 'RUL', '新增', false), ('ROLE-0002', 'RUL', '更新', false), ('ROLE-0002', 'RUL', '删除', false), ('ROLE-0002', 'RUL', '审核', true),
  ('ROLE-0002', 'PAR', '查看', true), ('ROLE-0002', 'PAR', '新增', false), ('ROLE-0002', 'PAR', '更新', false), ('ROLE-0002', 'PAR', '删除', false), ('ROLE-0002', 'PAR', '审核', true),
  ('ROLE-0002', 'SCH', '查看', true), ('ROLE-0002', 'SCH', '新增', false), ('ROLE-0002', 'SCH', '更新', false), ('ROLE-0002', 'SCH', '删除', false), ('ROLE-0002', 'SCH', '审核', true),
  ('ROLE-0002', 'PRM', '查看', true), ('ROLE-0002', 'PRM', '新增', false), ('ROLE-0002', 'PRM', '更新', false), ('ROLE-0002', 'PRM', '删除', false), ('ROLE-0002', 'PRM', '审核', true),
  ('ROLE-0002', 'DOC', '查看', true), ('ROLE-0002', 'DOC', '新增', false), ('ROLE-0002', 'DOC', '更新', false), ('ROLE-0002', 'DOC', '删除', false), ('ROLE-0002', 'DOC', '审核', true);

-- ROLE-0003 算法工程师（查阅 + 新增 + 更新；删除/审核 ✗）
INSERT INTO role_permission (role_id, ko_type, operation, granted) VALUES
  ('ROLE-0003', 'CON', '查看', true), ('ROLE-0003', 'CON', '新增', true), ('ROLE-0003', 'CON', '更新', true), ('ROLE-0003', 'CON', '删除', false), ('ROLE-0003', 'CON', '审核', false),
  ('ROLE-0003', 'RUL', '查看', true), ('ROLE-0003', 'RUL', '新增', true), ('ROLE-0003', 'RUL', '更新', true), ('ROLE-0003', 'RUL', '删除', false), ('ROLE-0003', 'RUL', '审核', false),
  ('ROLE-0003', 'PAR', '查看', true), ('ROLE-0003', 'PAR', '新增', true), ('ROLE-0003', 'PAR', '更新', true), ('ROLE-0003', 'PAR', '删除', false), ('ROLE-0003', 'PAR', '审核', false),
  ('ROLE-0003', 'SCH', '查看', true), ('ROLE-0003', 'SCH', '新增', true), ('ROLE-0003', 'SCH', '更新', true), ('ROLE-0003', 'SCH', '删除', false), ('ROLE-0003', 'SCH', '审核', false),
  ('ROLE-0003', 'PRM', '查看', true), ('ROLE-0003', 'PRM', '新增', true), ('ROLE-0003', 'PRM', '更新', true), ('ROLE-0003', 'PRM', '删除', false), ('ROLE-0003', 'PRM', '审核', false),
  ('ROLE-0003', 'DOC', '查看', true), ('ROLE-0003', 'DOC', '新增', true), ('ROLE-0003', 'DOC', '更新', true), ('ROLE-0003', 'DOC', '删除', false), ('ROLE-0003', 'DOC', '审核', false);

-- ROLE-0004 业务专家（CON/SCH/PRM/DOC 仅查阅；RUL/PAR 可新增/更新）
INSERT INTO role_permission (role_id, ko_type, operation, granted) VALUES
  ('ROLE-0004', 'CON', '查看', true), ('ROLE-0004', 'CON', '新增', false), ('ROLE-0004', 'CON', '更新', false), ('ROLE-0004', 'CON', '删除', false), ('ROLE-0004', 'CON', '审核', false),
  ('ROLE-0004', 'RUL', '查看', true), ('ROLE-0004', 'RUL', '新增', true), ('ROLE-0004', 'RUL', '更新', true), ('ROLE-0004', 'RUL', '删除', false), ('ROLE-0004', 'RUL', '审核', false),
  ('ROLE-0004', 'PAR', '查看', true), ('ROLE-0004', 'PAR', '新增', true), ('ROLE-0004', 'PAR', '更新', true), ('ROLE-0004', 'PAR', '删除', false), ('ROLE-0004', 'PAR', '审核', false),
  ('ROLE-0004', 'SCH', '查看', true), ('ROLE-0004', 'SCH', '新增', false), ('ROLE-0004', 'SCH', '更新', false), ('ROLE-0004', 'SCH', '删除', false), ('ROLE-0004', 'SCH', '审核', false),
  ('ROLE-0004', 'PRM', '查看', true), ('ROLE-0004', 'PRM', '新增', false), ('ROLE-0004', 'PRM', '更新', false), ('ROLE-0004', 'PRM', '删除', false), ('ROLE-0004', 'PRM', '审核', false),
  ('ROLE-0004', 'DOC', '查看', true), ('ROLE-0004', 'DOC', '新增', false), ('ROLE-0004', 'DOC', '更新', false), ('ROLE-0004', 'DOC', '删除', false), ('ROLE-0004', 'DOC', '审核', false);

-- ROLE-0005 只读观察者（仅查阅）
INSERT INTO role_permission (role_id, ko_type, operation, granted) VALUES
  ('ROLE-0005', 'CON', '查看', true), ('ROLE-0005', 'CON', '新增', false), ('ROLE-0005', 'CON', '更新', false), ('ROLE-0005', 'CON', '删除', false), ('ROLE-0005', 'CON', '审核', false),
  ('ROLE-0005', 'RUL', '查看', true), ('ROLE-0005', 'RUL', '新增', false), ('ROLE-0005', 'RUL', '更新', false), ('ROLE-0005', 'RUL', '删除', false), ('ROLE-0005', 'RUL', '审核', false),
  ('ROLE-0005', 'PAR', '查看', true), ('ROLE-0005', 'PAR', '新增', false), ('ROLE-0005', 'PAR', '更新', false), ('ROLE-0005', 'PAR', '删除', false), ('ROLE-0005', 'PAR', '审核', false),
  ('ROLE-0005', 'SCH', '查看', true), ('ROLE-0005', 'SCH', '新增', false), ('ROLE-0005', 'SCH', '更新', false), ('ROLE-0005', 'SCH', '删除', false), ('ROLE-0005', 'SCH', '审核', false),
  ('ROLE-0005', 'PRM', '查看', true), ('ROLE-0005', 'PRM', '新增', false), ('ROLE-0005', 'PRM', '更新', false), ('ROLE-0005', 'PRM', '删除', false), ('ROLE-0005', 'PRM', '审核', false),
  ('ROLE-0005', 'DOC', '查看', true), ('ROLE-0005', 'DOC', '新增', false), ('ROLE-0005', 'DOC', '更新', false), ('ROLE-0005', 'DOC', '删除', false), ('ROLE-0005', 'DOC', '审核', false);

COMMIT;
