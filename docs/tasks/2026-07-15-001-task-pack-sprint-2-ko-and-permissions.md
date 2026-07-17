---
title: "辽港伐谋 KM 平台 MVP — Sprint 2 KO 库 + 权限 + 提示词 Task Pack"
type: "task-pack"
status: "derived"
date: "2026-07-15"
spec_id: "2026-07-13-001-liaogang-famou-km-platform"
source_plan: "docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
source_plan_hash: "sha256:pending-validation"
generated_by: "manual-compile-after-sprint-1-template"
mode: "derived"
source_sections:
  - "Summary"
  - "Requirements"
  - "Scope Boundaries"
  - "Key Technical Decisions"
  - "Implementation Units (U4, U5, U6)"
  - "System-Wide Impact"
  - "Risks & Dependencies"
  - "Open Questions"
  - "Completion Criteria"
target_repo: "."
---

# 辽港伐谋 KM 平台 MVP — Sprint 2 KO 库 + 权限 + 提示词 Task Pack

## Overview

Sprint 2（计划 2026-07-31 完成）对应 PRD §八 里程碑 4 + Sprint 2 交付物，**包含 U4 + U5 + U6 三个 Implementation Unit**：
- **U4 KO 库模块**（6 类型 + 状态机 + 生命周期 + 审核流 + 跨类搜索）
- **U5 权限与角色模块**（5 预置 + 默认矩阵 + 角色 CRUD + 权限 UI + 下次登录生效 OQ-12 + 跨设备撤销不可行 OQ-5）
- **U6 提示词系统模块**（PRM 模板 + Section + 变量绑定 + 自研 Handlebars 子集 OQ-15 + 三栏组装器）

**Sprint 2 完成后**：KO 库可用 6 类型 CRUD/列表/详情/搜索；权限矩阵可调整（管理员通过 UI）；提示词组装器可用（三栏预览 + Handlebars 渲染）；mvn clean verify 全通过；TP-2 seed 完整性达部署门禁（V9002~V9004 schema + 5 预置角色 + 3 PRM 模板 seed）。

**Sprint 2 启动前依赖**（已就绪 / 不阻塞）：
- ✅ U1（Monorepo + K8s）+ U2（后端基础）+ U3（前端基础）已完成（Sprint 1）
- ✅ 集成测试环境（MySQL + Redis + MinIO 真实环境）
- ✅ P1 三层编译门禁（pre-commit / pre-push / GitHub Actions）
- ✅ Q-I1 完整 4 项已收齐（端点 百度千帆 + deepseek-v4-flash + 配额 100K TPM / 10M TPD / 5000 元月成本 / 100K 上下文）
- ✅ 不依赖 Q-I1（U6 实时预览是前端 handlebars 渲染，不调 LLM API）

**Sprint 0 / Sprint 1 启动前 owner 输入**（已部分收齐）：
- Q-I2 辽港慧应用 OIDC 端点（Q-I2）— dev/test mock 已实现，prod 联调时由 IT/安全 提供
- Q-I3 MinIO 部署位置（Q-I3）— 基础架构
- ✅ Q-I4 §3 手动子项弹层 UI 细节（Q-I4）— 已闭环（2026-07-17）：业务专家 sparring 议程决议归档 `docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md`；Round 1 minimum viable 8 项 + Round 2 跟进 5 项 + 5 项 PRD 已锁边界共 13+5=18 项决议完成占位拍板；U2-U5 实施批次已启动（`docs/plans/2026-07-17-001-feat-liaogang-section3-manual-subitem-modal-plan.md`）

## Source Summary

- **Source plan**: `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`（约 1000 行，10 U 完整定义 + Sprint 拆分 + 责任分配）
- **Source PRD**: `docs/brainstorms/liaogang-famou-km-platform-requirements.md`（PRD v0.32 + 22 OQ 全部 grill 闭合）
- **Consumed source sections**: Summary / Requirements / Scope Boundaries / Key Technical Decisions / Implementation Units (U4, U5, U6) / System-Wide Impact / Risks & Dependencies / Open Questions / Completion Criteria
- **Scope boundaries**:
  - **保留 in-scope**: KO 6 类型实体 + 状态机 + 5 状态转换 + 跨类搜索 + DOC/PRM 豁免 + 5 预置角色 + 权限矩阵 150 cells + 角色 CRUD + 用户角色分配 + 3 PRM 模板 + Section 卡片 + Handlebars 子集 + 三栏组装器
  - **移除 out-of-scope**（Sprint 3 / Sprint 4）：KO 知识治理（U7，冲突检测 + LLM 建议 DeepSeek v4 + 仲裁快路径）/ 提示词快照（U8，SNP/PRP 陈旧快照）/ 审计+项目+字典模块（U9）/ 文档预览（U10，PDF.js + LibreOffice）
  - **存疑保留 deferred**: U7 LLM 建议接真服务时再升级 token 限流（当前 LlmQuotaService 已配置就位）
- **Implementation-time unknowns**: KO 状态机边界（DRAFT 期间不能新建 IN-FLIGHT，跨项目 KO 隔离规则）；权限矩阵 UI 性能（13 菜单 × 5 操作 × 5 角色 渲染耗时）；自研 Handlebars 子集 vs 第三方库（OQ-15 已决策用自研）

## Traceability Matrix

| Source Unit | PRD Requirement | Task(s) | Validation |
|-------------|------------------|---------|------------|
| U4 | R1（KO 6 类型 + CRUD + 搜索 + 审核流）| T201, T202, T203, T204 | mvn verify + KoControllerIT 跑通 + 6 类型 KO 各创建/查询/审核 1 条 |
| U5 | R2（5 预置角色 + 默认矩阵 + CRUD + UI + OQ-12 下次登录生效）| T205, T206, T207 | mvn verify + 5 预置角色自动 seed + 角色变更下次登录测试通过 |
| U6 | R3（PRM 模板 + Section + Handlebars + 组装器）| T208, T209, T210, T211 | mvn verify + ComposerView 端到端 + Handlebars 子集单测 + PRP 装配数动态计算 |

## Task Graph

```
T201 (U4 数据模型 + 状态机) ─┬─> T202 (U4 CRUD + 跨类搜索)
                             ├─> T203 (U4 前端 KO 库 + 详情页)
                             └─> T204 (U4 审核流 + DOC/PRM 豁免)
                                          │
T201 ─> T205 (U5 默认矩阵 + 角色 CRUD) ─> T206 (U5 权限矩阵 UI) ─> T207 (U5 下次登录生效)
                                                                                    │
T201 + T205 ─> T208 (U6 PRM 模板) ─> T209 (U6 Section + Handlebars) ─> T210 (U6 三栏组装器) ─> T211 (U6 PRP 装配数 + 字符数)
```

并行约束：
- T201 完成前 T202 / T203 / T204 / T205 / T208 都不能启动（实体基础）
- T202 / T203 / T204 可在 T201 完成后并行
- T205 / T206 / T207 在 U4 CRUD（T202）完成后启动
- T208 / T209 / T210 / T211 在 T205 完成后启动

## Execution Waves

本 task pack 内 4 个 wave（按 U 依赖 + Sprint 1 经验）：

| Wave | Tasks | 阶段描述 |
|------|-------|----------|
| **Wave 1** | T201 | U4 数据模型基础（KoEntity + KoVersionEntity + V9002 migration + KoStateMachine）所有 U 后续任务的前置 |
| **Wave 2** | T202, T203, T204, T205 | U4 业务 + 前端 + 审核流 + U5 数据模型（默认矩阵 + 角色 CRUD） |
| **Wave 3** | T206, T207, T208 | U5 前端权限矩阵 UI + 下次登录生效 + U6 PRM 模板 |
| **Wave 4** | T209, T210, T211 | U6 Section + Handlebars 子集 + 三栏组装器 + PRP 装配数 |


> 详细任务级 JSON 见下文 Task Pack Contract 段（machine-readable canonical source）。

## Task Pack Contract

> Machine-readable canonical source for validators. JSON wins on any conflict with the human-readable `## Task Cards` section.

```json
{
  "schema_version": "task-pack/v1",
  "execution_waves": [
    {"wave": 1, "tasks": ["T201"]},
    {"wave": 2, "tasks": ["T202", "T203", "T204", "T205"]},
    {"wave": 3, "tasks": ["T206", "T207", "T208"]},
    {"wave": 4, "tasks": ["T209", "T210", "T211"]}
  ],
  "tasks": [
    {
      "task_id": "T201",
      "source_unit": "U4",
      "requirement_refs": ["R1"],
      "goal": "建立 KO 库数据模型（6 类型 + 状态机 + 生命周期），含 Flyway V9002 建表 + KoEntity + KoVersionEntity + KoStateMachine 5 状态转换守卫",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/ko/model/KoEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/model/KoVersionEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/model/KoReferenceEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/repository/KoMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/repository/KoVersionMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/service/KoStateMachine.java",
        "backend/src/main/resources/db/migration/V9002__create_ko_tables.sql"
      ],
      "test_focus": "mvn compile + KoStateMachineTest 单测（5 状态转换 + 类型豁免）",
      "done_signal": "mvn clean verify 通过 + KoStateMachineTest 5/5 测试通过 + V9002 migration 幂等执行 + ko/ko_version/ko_references 3 表自动创建",
      "risk_note": "KO ID 格式 KO-{TYPE}-{NNNN} 按类型独立自增；migration 顺序在 V9001 seed 之后（V9001 已写入 4 项目 + 5 角色）",
      "review_gate": "required",
      "review_focus": "数据模型完整性 + 状态机边界（Active 期间不允许创建新工作版本 OQ-12 状态机约束）",
      "stop_if": "需要新增 KO 类型或状态（C7 / S6 等），或需修改 V9001 seed 数据结构",
      "wave": 1
    },
    {
      "task_id": "T202",
      "source_unit": "U4",
      "requirement_refs": ["R1"],
      "goal": "实现 KO CRUD + 跨类搜索 + 列表 + 详情 REST API（KoService + KoController）",
      "dependencies": ["T201"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/ko/service/KoService.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/controller/KoController.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/dto/KoListItem.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/dto/KoDetail.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/dto/KoSearchResult.java",
        "backend/src/test/java/com/liaogang/famou/km/ko/KoControllerIntegrationTest.java"
      ],
      "test_focus": "mvn verify + KoControllerIT 6 类型 KO 各创建/查询/搜索 1 条 + 跨类搜索 OQ-4（按 title + id + typeName 匹配）",
      "done_signal": "KoControllerIT 6/6 通过 + 跨类搜索 /api/ko/search 返回正确格式",
      "risk_note": "跨项目 KO 隔离（PROJ-0001 用户查不到 PROJ-0002 的 KO）；列表分页（OQ-21 NFR-04 列表 ≤ 2s）",
      "review_gate": "required",
      "review_focus": "搜索匹配逻辑（OQ-4 title+id+typeName 3 字段）+ 跨项目隔离",
      "stop_if": "需要全文搜索（Elasticsearch 等）或需要非 KO 实体的统一搜索",
      "wave": 2
    },
    {
      "task_id": "T203",
      "source_unit": "U4",
      "requirement_refs": ["R1"],
      "goal": "实现 KO 库前端页面（全景概览页 + 6 类型列表页 + 详情页 + 通用表格组件 + 类型 Tab 容器）",
      "dependencies": ["T201"],
      "files": [
        "frontend/src/views/ko-library/KoLibraryView.vue",
        "frontend/src/views/ko-{con,rul,par,sch,prm,doc}/KoListView.vue",
        "frontend/src/views/ko-{type}/KoDetailView.vue",
        "frontend/src/components/KoTable.vue",
        "frontend/src/components/KoTypeTabContainer.vue",
        "frontend/src/components/KoNewEditModal.vue",
        "frontend/src/api/ko.ts",
        "frontend/src/router/ko.ts"
      ],
      "test_focus": "pnpm dev 启动 + KoLibraryView 渲染 6 类型入口卡片 + 列表页 Tab 切换 + 详情页形式化定义 + 跨类搜索",
      "done_signal": "pnpm dev 无控制台错误 + 6 类型列表页可达 + 详情页加载 1 条种子数据",
      "risk_note": "列表分页性能（OQ-21 NFR-04 ≤ 2s）；6 类型差异化列（CON 9 列 / RUL 9 列 / PAR 10 列 / SCH 9 列 / PRM 11 列 / DOC 7 列）",
      "review_gate": "optional",
      "review_focus": "UI 与原型 V3 line 3799-3846 (handleLibSearch) 一致性 + 6 类型列配置正确",
      "stop_if": "需要重做 UI 框架（换 Element Plus 其他组件库）或需要响应式设计（仅桌面）",
      "wave": 2
    },
    {
      "task_id": "T204",
      "source_unit": "U4",
      "requirement_refs": ["R1"],
      "goal": "实现 KO 审核流（DRAFT → REVIEW → APPROVED → PUBLISHED 4 状态）+ DOC/PRM 类型豁免规则",
      "dependencies": ["T201", "T202"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/ko/service/KoAuditService.java",
        "backend/src/main/java/com/liaogang/famou/km/ko/controller/KoAuditController.java",
        "backend/src/test/java/com/liaogang/famou/km/ko/KoAuditFlowTest.java"
      ],
      "test_focus": "mvn verify + KoAuditFlowTest 4 状态转换 + DOC 上传直接 Active（不经 Review）+ PRM 走标准流程（OQ-6）",
      "done_signal": "KoAuditFlowTest 5/5 通过 + DOC 豁免规则生效 + 合规审核员审核自己提交的 KO Version 403",
      "risk_note": "Active 期间不允许创建新工作版本（OQ-12）；同一 KO 同时最多 1 个 in-flight 工作版本",
      "review_gate": "required",
      "review_focus": "状态机守卫（5 状态合法转换）+ 类型豁免规则正确性 + 权限拒绝（自己审自己）",
      "stop_if": "需要新增审核角色或流程分支（如紧急审核快路径）",
      "wave": 2
    },
    {
      "task_id": "T205",
      "source_unit": "U5",
      "requirement_refs": ["R2"],
      "goal": "实现权限数据模型 + 默认矩阵 seed 加载 + 5 预置角色 CRUD（RoleEntity + RolePermissionEntity + UserRoleEntity + V9003 migration + seed/role-permissions.yaml）",
      "dependencies": ["T201"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/role/model/RoleEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/role/model/RolePermissionEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/role/model/UserRoleEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/role/repository/RoleMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/role/service/RoleService.java",
        "backend/src/main/java/com/liaogang/famou/km/role/controller/RoleController.java",
        "backend/src/main/java/com/liaogang/famou/km/role/service/DefaultMatrixLoader.java",
        "backend/src/main/resources/db/migration/V9003__create_role_tables.sql",
        "backend/src/main/resources/seed/role-permissions.yaml"
      ],
      "test_focus": "mvn verify + RoleServiceTest 5 预置角色自动 seed + 默认矩阵 150 cells 加载正确 + 角色 CRUD",
      "done_signal": "RoleServiceTest 5/5 通过 + V9003 migration 幂等 + 5 角色 150 cells 权限加载完成",
      "risk_note": "5 预置角色不可删除（v0.32 §4.1.1）；自定义角色可创建/编辑/删除（未被引用时）",
      "review_gate": "required",
      "review_focus": "默认矩阵 150 cells 完整性（5 角色 × 6 KO 类型 × 5 操作）+ 预置角色不可删除",
      "stop_if": "需要支持角色继承 / 角色层级（v0.32 不在范围）",
      "wave": 2
    },
    {
      "task_id": "T206",
      "source_unit": "U5",
      "requirement_refs": ["R2"],
      "goal": "实现权限矩阵 UI（菜单项 × 5 操作 勾选矩阵 + 角色 CRUD + 用户角色分配 + 3 秒 Toast 撤销仅前端 UI 回滚 OQ-5）",
      "dependencies": ["T205"],
      "files": [
        "frontend/src/views/permissions/PermissionsView.vue",
        "frontend/src/components/PermissionMatrix.vue",
        "frontend/src/components/RoleAssignmentModal.vue",
        "frontend/src/api/role.ts",
        "frontend/src/router/permissions.ts"
      ],
      "test_focus": "pnpm dev + PermissionMatrix 渲染 13 菜单 × 5 操作 + 角色保存 + 用户分配 + 3 秒 Toast 撤销",
      "done_signal": "pnpm dev 无控制台错误 + 权限矩阵 UI 可调整 + Toast 显示 3 秒",
      "risk_note": "权限矩阵 UI 性能（13 菜单 × 5 操作 × 5 角色 = 325 cells 渲染）；3 秒撤销仅前端（OQ-5 已删除后端 revert 接口）",
      "review_gate": "optional",
      "review_focus": "UI 与原型 V3 line 3799-3846 函数映射（switchRoleTab / openRoleAssignmentModal / saveRole / saveUserRole）",
      "stop_if": "需要支持权限继承 / 角色层级 UI",
      "wave": 3
    },
    {
      "task_id": "T207",
      "source_unit": "U5",
      "requirement_refs": ["R2"],
      "goal": "实现角色变更下次登录生效 OQ-12（审计 USER_ROLE_CHANGE + effective_at + JwtAuthFilter 读取新 role + 旧 session 保持）",
      "dependencies": ["T205", "T206"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/auth/RoleChangeListener.java",
        "backend/src/main/java/com/liaogang/famou/km/auth/RoleChangeAuditService.java",
        "backend/src/test/java/com/liaogang/famou/km/auth/RoleChangeEffectTest.java"
      ],
      "test_focus": "mvn verify + RoleChangeEffectTest：角色变更后旧 session 仍可用 + 新登录生效（OQ-12）",
      "done_signal": "RoleChangeEffectTest 3/3 通过 + 跨设备撤销不可行（OQ-5 后端不调 revert 接口）",
      "risk_note": "Redis cache 失效（不需要，因为 JWT 自包含 role claim）；OQ-5 已删除后端 /api/audit/{id}/revert 接口",
      "review_gate": "required",
      "review_focus": "JWT 签发时机（旧 session 仍用旧 role；新登录用新 role）+ 审计日志 USER_ROLE_CHANGE 触发",
      "stop_if": "需要实时推送角色变更（OQ-12 已决策不推送，删除 WebSocket）",
      "wave": 3
    },
    {
      "task_id": "T208",
      "source_unit": "U6",
      "requirement_refs": ["R3"],
      "goal": "实现 PRM 模板数据模型 + seed 加载（KO-PRM-0001 9 段 / KO-PRM-0002 3 段 / KO-PRM-0003 5 段）",
      "dependencies": ["T201"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/prompt/model/PrmTemplateEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/model/PrmSectionEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/repository/PrmTemplateMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/service/PrmService.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/controller/PrmController.java",
        "backend/src/main/resources/db/migration/V9004__create_prm_tables.sql",
        "backend/src/main/resources/seed/prm-templates.yaml"
      ],
      "test_focus": "mvn verify + PrmServiceTest 3 模板自动 seed + 9+3+5 段加载正确",
      "done_signal": "PrmServiceTest 3/3 通过 + V9004 migration 幂等 + 3 模板 17 段加载完成",
      "risk_note": "PRM 模板加载顺序在 V9001 / V9002 / V9003 之后；自研 Handlebars 子集在 T209 实现",
      "review_gate": "optional",
      "review_focus": "PRM 模板 17 段完整性（9+3+5）+ Section 字段（FIXED/DYNAMIC 类型 + selectedKOs + manualSubItems + varBindings）",
      "stop_if": "需要支持 PRM 模板版本管理 / 多语言（v0.32 不在范围）",
      "wave": 3
    },
    {
      "task_id": "T209",
      "source_unit": "U6",
      "requirement_refs": ["R3"],
      "goal": "实现自研 Handlebars 子集（OQ-15：`{{var}}` / `{{#each}}` / `{{#if}}`）+ Markdown 渲染器（H2-H4 / 粗斜体 / 代码块 / 表格 / 列表 / 引用 / 链接 / 分隔线 / 变量高亮）",
      "dependencies": ["T208"],
      "files": [
        "frontend/src/utils/handlebars.ts",
        "frontend/src/utils/markdown-renderer.ts",
        "frontend/src/test/utils/handlebars.test.ts",
        "frontend/src/test/utils/markdown-renderer.test.ts"
      ],
      "test_focus": "pnpm test 跑 handlebars.test.ts（`{{var}}` 替换 / `{{#each items}}` 循环 / `{{#if}}` 条件）+ markdown-renderer.test.ts（9 类 Markdown 元素）",
      "done_signal": "pnpm test 18+/18+ 通过 + 自研 Handlebars 子集覆盖率 ≥ 90%",
      "risk_note": "自研实现复杂度（OQ-15 决策：自研 vs 第三方库 评估过自研成本更低 / 收缩到产品需求范围）",
      "review_gate": "required",
      "review_focus": "Handlebars 子集边界（OQ-15 不实现 partials / helpers）+ Markdown 渲染边界（OQ-15 收缩到 PRD §10.5.3 列表）",
      "stop_if": "需要支持 partials / 自定义 helpers（OQ-15 决策不做）",
      "wave": 4
    },
    {
      "task_id": "T210",
      "source_unit": "U6",
      "requirement_refs": ["R3"],
      "goal": "实现三栏组装器 UI（顶部 PRM 选择 + 中栏 Section 编排 + 右栏实时预览）+ Section 卡片（FIXED 变量赋值 / DYNAMIC KO 选择 / 手动子项）+ 变量绑定弹窗",
      "dependencies": ["T208", "T209"],
      "files": [
        "frontend/src/views/composer/ComposerView.vue",
        "frontend/src/components/SectionCard.vue",
        "frontend/src/components/VariableBindingModal.vue",
        "frontend/src/api/composer.ts",
        "frontend/src/router/composer.ts"
      ],
      "test_focus": "pnpm dev + ComposerView 顶部 PRM 切换 + 中栏 Section 拖拽 + 右栏实时渲染 + 变量绑定弹窗 ⚡自动 / ✎当前 / ⊕重选 / ×解除",
      "done_signal": "pnpm dev 无控制台错误 + 三栏联动 + 实时预览响应 ≤ g(M) ms（NFR-03）",
      "risk_note": "实时预览性能（OQ-15 NFR-03 字符数 / Token 数 g(M) 实时显示）；变量绑定 PAR 单选弹层 UI 复杂度（OQ-16）",
      "review_gate": "optional",
      "review_focus": "UI 与原型 V3 handleTypeSearch / renderComposerSections / onPRMTemplateChange 函数一致",
      "stop_if": "需要支持 Section 拖拽排序（v0.32 不在范围）",
      "wave": 4
    },
    {
      "task_id": "T211",
      "source_unit": "U6",
      "requirement_refs": ["R3"],
      "goal": "实现 PRP 装配数动态计算 OQ-16（= selectedKOs + varBindings + manualSubItems）+ 字符数 / Token 数实时显示 g(M) + Handlebars 语法错误检测",
      "dependencies": ["T209", "T210"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/prompt/service/TemplateEngine.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/service/ComposerRenderService.java",
        "backend/src/main/java/com/liaogang/famou/km/prompt/controller/ComposerController.java",
        "backend/src/test/java/com/liaogang/famou/km/prompt/TemplateEngineTest.java",
        "backend/src/test/java/com/liaogang/famou/km/prompt/ComposerRenderTest.java"
      ],
      "test_focus": "mvn verify + TemplateEngineTest（Handlebars 语法错误检测 / 变量未绑定占位）+ ComposerRenderTest（PRP 装配数动态）",
      "done_signal": "TemplateEngineTest 8/8 + ComposerRenderTest 5/5 通过 + PRP 装配数 OQ-16 动态计算正确",
      "risk_note": "PRP 装配数动态（OQ-16 删模板作者登记字段，运行时计算）+ 字符数估算（g(M) 函数，2 chars/token 保守）",
      "review_gate": "required",
      "review_focus": "PRP 装配数动态公式（= selectedKOs.length + varBindings.size + manualSubItems.size）+ Handlebars 错误检测",
      "stop_if": "需要支持 PRP 导出（PDF / Markdown 文件下载）",
      "wave": 4
    }
  ]
}
```

## Task Cards

### T201
- **source_unit**: U4
- **goal**: 建立 KO 库数据模型（6 类型 + 状态机 + 生命周期），含 Flyway V9002 建表 + KoEntity + KoVersionEntity + KoStateMachine 5 状态转换守卫
- **dependencies**: []
- **files**: KoEntity / KoVersionEntity / KoReferenceEntity / KoMapper / KoVersionMapper / KoStateMachine / V9002 migration
- **test_focus**: mvn compile + KoStateMachineTest 单测（5 状态转换 + 类型豁免）
- **done_signal**: mvn clean verify 通过 + KoStateMachineTest 5/5 测试通过 + V9002 migration 幂等执行 + ko/ko_version/ko_references 3 表自动创建
- **risk_note**: KO ID 格式 KO-{TYPE}-{NNNN} 按类型独立自增；migration 顺序在 V9001 seed 之后
- **review_gate**: required
- **review_focus**: 数据模型完整性 + 状态机边界（Active 期间不允许创建新工作版本 OQ-12 状态机约束）
- **stop_if**: 需要新增 KO 类型或状态，或需修改 V9001 seed 数据结构
- **wave**: 1

### T202
- **source_unit**: U4
- **goal**: 实现 KO CRUD + 跨类搜索 + 列表 + 详情 REST API
- **dependencies**: T201
- **files**: KoService / KoController / DTOs / KoControllerIntegrationTest
- **test_focus**: mvn verify + KoControllerIT 6 类型 KO 各创建/查询/搜索 1 条 + 跨类搜索 OQ-4
- **done_signal**: KoControllerIT 6/6 通过 + 跨类搜索 /api/ko/search 返回正确格式
- **risk_note**: 跨项目 KO 隔离（PROJ-0001 用户查不到 PROJ-0002 的 KO）；列表分页（OQ-21 NFR-04 ≤ 2s）
- **review_gate**: required
- **review_focus**: 搜索匹配逻辑（OQ-4 title+id+typeName 3 字段）+ 跨项目隔离
- **stop_if**: 需要全文搜索（Elasticsearch 等）或需要非 KO 实体的统一搜索
- **wave**: 2

### T203
- **source_unit**: U4
- **goal**: 实现 KO 库前端页面（全景概览 + 6 类型列表 + 详情 + 通用表格 + Tab 容器 + 新建/编辑弹窗）
- **dependencies**: T201
- **files**: KoLibraryView / KoListView × 6 / KoDetailView / KoTable / KoTypeTabContainer / KoNewEditModal / api/ko.ts / router/ko.ts
- **test_focus**: pnpm dev + KoLibraryView 渲染 6 类型入口卡片 + 列表页 Tab 切换 + 详情页形式化定义 + 跨类搜索
- **done_signal**: pnpm dev 无控制台错误 + 6 类型列表页可达 + 详情页加载 1 条种子数据
- **risk_note**: 列表分页性能（OQ-21 NFR-04 ≤ 2s）；6 类型差异化列（CON 9 列 / RUL 9 列 / PAR 10 列 / SCH 9 列 / PRM 11 列 / DOC 7 列）
- **review_gate**: optional
- **review_focus**: UI 与原型 V3 line 3799-3846 (handleLibSearch) 一致性 + 6 类型列配置正确
- **stop_if**: 需要重做 UI 框架或需要响应式设计
- **wave**: 2

### T204
- **source_unit**: U4
- **goal**: 实现 KO 审核流（DRAFT → REVIEW → APPROVED → PUBLISHED 4 状态）+ DOC/PRM 类型豁免
- **dependencies**: T201, T202
- **files**: KoAuditService / KoAuditController / KoAuditFlowTest
- **test_focus**: mvn verify + KoAuditFlowTest 4 状态转换 + DOC 上传直接 Active（不经 Review）+ PRM 走标准流程（OQ-6）
- **done_signal**: KoAuditFlowTest 5/5 通过 + DOC 豁免规则生效 + 合规审核员审核自己提交的 KO Version 403
- **risk_note**: Active 期间不允许创建新工作版本（OQ-12）；同一 KO 同时最多 1 个 in-flight 工作版本
- **review_gate**: required
- **review_focus**: 状态机守卫（5 状态合法转换）+ 类型豁免规则正确性 + 权限拒绝（自己审自己）
- **stop_if**: 需要新增审核角色或流程分支
- **wave**: 2

### T205
- **source_unit**: U5
- **goal**: 实现权限数据模型 + 默认矩阵 seed 加载 + 5 预置角色 CRUD + V9003 migration + seed/role-permissions.yaml
- **dependencies**: T201
- **files**: RoleEntity / RolePermissionEntity / UserRoleEntity / RoleMapper / RoleService / RoleController / DefaultMatrixLoader / V9003 migration / role-permissions.yaml
- **test_focus**: mvn verify + RoleServiceTest 5 预置角色自动 seed + 默认矩阵 150 cells 加载正确 + 角色 CRUD
- **done_signal**: RoleServiceTest 5/5 通过 + V9003 migration 幂等 + 5 角色 150 cells 权限加载完成
- **risk_note**: 5 预置角色不可删除（v0.32 §4.1.1）；自定义角色可创建/编辑/删除（未被引用时）
- **review_gate**: required
- **review_focus**: 默认矩阵 150 cells 完整性（5 角色 × 6 KO 类型 × 5 操作）+ 预置角色不可删除
- **stop_if**: 需要支持角色继承 / 角色层级（v0.32 不在范围）
- **wave**: 2

### T206
- **source_unit**: U5
- **goal**: 实现权限矩阵 UI + 角色 CRUD + 用户角色分配 + 3 秒 Toast 撤销仅前端 UI 回滚（OQ-5）
- **dependencies**: T205
- **files**: PermissionsView / PermissionMatrix / RoleAssignmentModal / api/role.ts / router/permissions.ts
- **test_focus**: pnpm dev + PermissionMatrix 渲染 13 菜单 × 5 操作 + 角色保存 + 用户分配 + 3 秒 Toast 撤销
- **done_signal**: pnpm dev 无控制台错误 + 权限矩阵 UI 可调整 + Toast 显示 3 秒
- **risk_note**: 权限矩阵 UI 性能（13 菜单 × 5 操作 × 5 角色 = 325 cells 渲染）；3 秒撤销仅前端（OQ-5）
- **review_gate**: optional
- **review_focus**: UI 与原型 V3 line 3799-3846 函数映射
- **stop_if**: 需要支持权限继承 / 角色层级 UI
- **wave**: 3

### T207
- **source_unit**: U5
- **goal**: 实现角色变更下次登录生效 OQ-12（审计 USER_ROLE_CHANGE + effective_at + JwtAuthFilter 读取新 role + 旧 session 保持）
- **dependencies**: T205, T206
- **files**: RoleChangeListener / RoleChangeAuditService / RoleChangeEffectTest
- **test_focus**: mvn verify + RoleChangeEffectTest：角色变更后旧 session 仍可用 + 新登录生效（OQ-12）
- **done_signal**: RoleChangeEffectTest 3/3 通过 + 跨设备撤销不可行（OQ-5 后端不调 revert 接口）
- **risk_note**: Redis cache 失效（不需要，因为 JWT 自包含 role claim）；OQ-5 已删除后端 /api/audit/{id}/revert 接口
- **review_gate**: required
- **review_focus**: JWT 签发时机（旧 session 仍用旧 role；新登录用新 role）+ 审计日志 USER_ROLE_CHANGE 触发
- **stop_if**: 需要实时推送角色变更（OQ-12 已决策不推送）
- **wave**: 3

### T208
- **source_unit**: U6
- **goal**: 实现 PRM 模板数据模型 + seed 加载（KO-PRM-0001 9 段 / KO-PRM-0002 3 段 / KO-PRM-0003 5 段）+ V9004 migration
- **dependencies**: T201
- **files**: PrmTemplateEntity / PrmSectionEntity / PrmTemplateMapper / PrmService / PrmController / V9004 migration / prm-templates.yaml
- **test_focus**: mvn verify + PrmServiceTest 3 模板自动 seed + 9+3+5 段加载正确
- **done_signal**: PrmServiceTest 3/3 通过 + V9004 migration 幂等 + 3 模板 17 段加载完成
- **risk_note**: PRM 模板加载顺序在 V9001 / V9002 / V9003 之后；自研 Handlebars 子集在 T209 实现
- **review_gate**: optional
- **review_focus**: PRM 模板 17 段完整性（9+3+5）+ Section 字段（FIXED/DYNAMIC 类型 + selectedKOs + manualSubItems + varBindings）
- **stop_if**: 需要支持 PRM 模板版本管理 / 多语言
- **wave**: 3

### T209
- **source_unit**: U6
- **goal**: 实现自研 Handlebars 子集（OQ-15：`{{var}}` / `{{#each}}` / `{{#if}}`）+ Markdown 渲染器（9 类元素 + 变量高亮）
- **dependencies**: T208
- **files**: frontend/src/utils/handlebars.ts + markdown-renderer.ts + 2 个 test
- **test_focus**: pnpm test 跑 handlebars.test.ts（3 类 Handlebars 语法）+ markdown-renderer.test.ts（9 类 Markdown 元素）
- **done_signal**: pnpm test 18+/18+ 通过 + 自研 Handlebars 子集覆盖率 ≥ 90%
- **risk_note**: 自研实现复杂度（OQ-15 决策：自研 vs 第三方库 评估过自研成本更低 / 收缩到产品需求范围）
- **review_gate**: required
- **review_focus**: Handlebars 子集边界（OQ-15 不实现 partials / helpers）+ Markdown 渲染边界（OQ-15 收缩到 PRD §10.5.3 列表）
- **stop_if**: 需要支持 partials / 自定义 helpers（OQ-15 决策不做）
- **wave**: 4

### T210
- **source_unit**: U6
- **goal**: 实现三栏组装器 UI（顶部 PRM 选择 + 中栏 Section 编排 + 右栏实时预览）+ Section 卡片 + 变量绑定弹窗
- **dependencies**: T208, T209
- **files**: ComposerView / SectionCard / VariableBindingModal / api/composer.ts / router/composer.ts
- **test_focus**: pnpm dev + ComposerView 顶部 PRM 切换 + 中栏 Section 拖拽 + 右栏实时渲染 + 变量绑定弹窗
- **done_signal**: pnpm dev 无控制台错误 + 三栏联动 + 实时预览响应 ≤ g(M) ms（NFR-03）
- **risk_note**: 实时预览性能（OQ-15 NFR-03 字符数 / Token 数 g(M) 实时显示）；变量绑定 PAR 单选弹层 UI 复杂度（OQ-16）
- **review_gate**: optional
- **review_focus**: UI 与原型 V3 handleTypeSearch / renderComposerSections / onPRMTemplateChange 函数一致
- **stop_if**: 需要支持 Section 拖拽排序（v0.32 不在范围）
- **wave**: 4

### T211
- **source_unit**: U6
- **goal**: 实现 PRP 装配数动态计算 OQ-16（= selectedKOs + varBindings + manualSubItems）+ 字符数 / Token 数实时显示 g(M) + Handlebars 语法错误检测
- **dependencies**: T209, T210
- **files**: TemplateEngine / ComposerRenderService / ComposerController / TemplateEngineTest / ComposerRenderTest
- **test_focus**: mvn verify + TemplateEngineTest（Handlebars 语法错误检测 / 变量未绑定占位）+ ComposerRenderTest（PRP 装配数动态）
- **done_signal**: TemplateEngineTest 8/8 + ComposerRenderTest 5/5 通过 + PRP 装配数 OQ-16 动态计算正确
- **risk_note**: PRP 装配数动态（OQ-16 删模板作者登记字段，运行时计算）+ 字符数估算（g(M) 函数，2 chars/token 保守）
- **review_gate**: required
- **review_focus**: PRP 装配数动态公式（= selectedKOs.length + varBindings.size + manualSubItems.size）+ Handlebars 错误检测
- **stop_if**: 需要支持 PRP 导出（PDF / Markdown 文件下载）
- **wave**: 4

## Orientation Evidence

- **provider**: direct-repo-reads
- **posture**: bounded
- **evidence_refs**:
  - `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md#U4-KO-库模块` — U4 完整定义
  - `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md#U5-权限与角色模块` — U5 完整定义
  - `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md#U6-提示词系统模块` — U6 完整定义
  - `docs/brainstorms/liaogang-famou-km-platform-requirements.md#v0.32-22-OQ-决议` — 22 OQ 全部 grill 闭合
  - `docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md` — Sprint 1 task pack 模板
  - `辽港伐谋知识管理平台_原型_V3.html` line 3799-3846 — U4 / U5 / U6 前端函数映射
- **limitations**: source_plan_hash 待 spec-first tasks validate 工具验证（Sprint 2 启动后立即验证）

## Validation Notes

- **Source plan derivation**: Plan v0 已通过 9 轮 spec-doc-review（Sprint 1）；U4 / U5 / U6 详细描述齐全可直接拆 task pack
- **Source plan hash verification**: 待执行 `spec-first tasks validate docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md --json` 验证
- **Old task pack rejection criteria**:
  - `source_plan_hash` 与 `spec-first tasks hash` 计算结果不匹配
  - `spec_id` 与 Plan frontmatter `spec_id: 2026-07-13-001-liaogang-famou-km-platform` 不一致
  - `mode: derived` 缺失或被改为 `transient` / `draft`
  - `status: derived` 缺失或被改为 `draft`
- **Task validation (Sprint 2 启动后)**:
  - T201 验证：mvn compile + KoStateMachineTest 5/5 通过 + V9002 migration 幂等 + ko/ko_version/ko_references 3 表自动创建
  - T202 验证：KoControllerIT 6/6 通过 + 跨类搜索 /api/ko/search 返回正确格式
  - T203 验证：pnpm dev + 6 类型列表页可达 + 详情页加载 1 条种子数据
  - T204 验证：KoAuditFlowTest 5/5 通过 + DOC 豁免规则生效 + 合规审核员审核自己 403
  - T205 验证：RoleServiceTest 5/5 通过 + V9003 migration 幂等 + 5 角色 150 cells 加载完成
  - T206 验证：pnpm dev + 权限矩阵 UI 可调整 + Toast 显示 3 秒
  - T207 验证：RoleChangeEffectTest 3/3 通过 + 跨设备撤销不可行
  - T208 验证：PrmServiceTest 3/3 通过 + V9004 migration 幂等 + 3 模板 17 段加载完成
  - T209 验证：pnpm test 18+/18+ 通过 + Handlebars 子集覆盖率 ≥ 90%
  - T210 验证：pnpm dev + 三栏联动 + 实时预览响应 ≤ g(M) ms
  - T211 验证：TemplateEngineTest 8/8 + ComposerRenderTest 5/5 通过 + PRP 装配数 OQ-16 动态计算正确

## Regeneration Rules

- **重建触发条件**（任一）：
  - Plan 文档内容变更（source_plan_hash 不匹配）
  - 22 OQ 决议变更（特别是 OQ-4 跨类搜索 / OQ-5 跨设备撤销 / OQ-6 PRM 流程 / OQ-12 下次登录 / OQ-15 Handlebars / OQ-16 PRP 装配数）
  - spec_id 变更
  - U4 / U5 / U6 中任一 Implementation Unit 的 Files / Test scenarios / Verification 变更
  - Sprint 2 拆分边界（TP-2 vs Sprint 3）需调整
- **不重建条件**：
  - Sprint 3 后续内容（U7 / U8 / U9 / U10）独立编译（不影响本 task pack）
  - Sprint 2 范围内 22 OQ 决议不变但 V9002 / V9003 / V9004 migration schema 调整 → 仅更新 migration 不重建 task pack
  - CHANGELOG / Design 文档更新
- **校验命令**（executable 前必跑）：
  ```bash
  npx spec-first tasks validate docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md --json
  ```

## Related Sprint 1 Artifacts

- **`docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md`**：Sprint 1 task pack 模板（12 commit + 9 task；本 TP-2 沿用同一规范）
- **`docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`**：Sprint 1 教训（已部署 P1 三层防御，Sprint 2 实施时自动防护）
- **`CHANGELOG.md` v1.16.0~v1.16.7**：Sprint 1 + P1 防御 + Q-I1 完整收齐的版本历史

## Quick Win（一行命令自查）

```bash
# Sprint 2 启动验证
cd backend && ./mvnw clean verify -Dspring.profiles.active=it -q
# 期望输出：BUILD SUCCESS，5+/5+ tests（已有 Sprint 1 5 个 + Sprint 2 实施后递增）
```

## 关联 commit / 分支

- **基础 commit**: `bb839ab chore(gitignore): 移 llm_client.txt 到 docs/private/`
- **分支**: `feat/sprint-2-ko-and-permissions`（worktree: `.worktrees/sprint-2`）
- **worktree 启动**: `git worktree add .worktrees/sprint-2 feat/sprint-2-ko-and-permissions`
- **worktree 清理**: `git worktree remove .worktrees/sprint-2 --force && git branch -d feat/sprint-2-ko-and-permissions`
