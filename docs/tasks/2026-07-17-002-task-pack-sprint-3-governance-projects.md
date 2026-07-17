---
title: "辽港伐谋 KM 平台 MVP — Sprint 3 知识治理 + 快照 + 审计/项目/字典 + 文档预览 Task Pack（spec-write-tasks 编译版）"
type: "task-pack"
status: "derived"
date: "2026-07-17"
spec_id: "2026-07-13-001-liaogang-famou-km-platform"
source_plan: "docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
source_plan_hash: "sha256:d5f212f065946b2fd5f533a81aa25e03ad02295a2572be3af9810de79bb8e402"
generated_by: "spec-write-tasks"
mode: "derived"
source_sections:
  - "Summary"
  - "Requirements"
  - "Scope Boundaries"
  - "Implementation Units (U7, U8, U9, U10)"
  - "System-Wide Impact"
  - "Risks & Dependencies"
  - "Completion Criteria"
target_repo: "."
---

# 辽港伐谋 KM 平台 MVP — Sprint 3 Task Pack (spec-write-tasks)

## Overview

Sprint 3（计划 2026-08-31 完成）对应 PRD v0.32 §八 里程碑 5 的 4 个 Implementation Unit (U7-U10)。

本 task pack 由 `spec-write-tasks` 编译（沿用上游 `docs/tasks/2026-08-01-001-task-pack-sprint-3-governance-projects.md` 的 Sprint 3 范围，但合并 Q-I4 §3 弹层基础 + 升级为 Task Pack Contract JSON + 可验证 `sha256:<64-hex>` source_plan_hash + 合规 generated_by 标识）。

**前提基础（Sprint 1+2 + Q-I4 §3 已落地）**：
- U1-U6 + F-53 + F-54 commit 9980417 合并
- Q-I4 §3 ManualSubItems array schema + U3 dual-write 已 commit c04014a/63606c8/946eda5
- composer.ts ManualSubItem interface + 38 演示值重放测试 5 个已 PASS
- R15b 命名空间约定 `{section.sectionIndex}.{varName}` 局部约定已落地（commit 63606c8）

## Source Summary

- **Source plan**: `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`（约 1000 行, 10 U 完整定义 + Sprint 拆分 + 责任分配）
- **Source PRD**: `docs/brainstorms/liaogang-famou-km-platform-requirements.md`（PRD v0.32 + 20 OQ 全部 grill 闭合）
- **Consumed source sections**: Implementation Units U7-U10 + Risk & Dependencies + System-Wide Impact
- **Scope boundaries**:
  - **保留 in-scope**: U7 知识治理 (6 C 类 + 1 H 类 + LLM 建议 + 仲裁 + 治理报告) / U8 SNP+PRP 关系 + ko_assembly_hash + 时间线 + 陈旧快照 / U9 审计 + 项目 + 字典 (3 子模块) / U10 文档预览 (MinIO + PDF.js + LibreOffice)
  - **移除 out-of-scope** (U7-U10 中明确): 业务侧 prompt 内容 (业务专家 / 算法工程师自行编辑) / 运维层监控告警 / SSO IdP 部署 / AI 自动生成 KO / C5 时序冲突 / H1/H3-H6 健康问题
  - **存疑保留 deferred**: 跨项目共享 / 多租户 / 移动端 / 外部 ERP/TOS 集成 → 二期 R-01~R-10
- **Sprint 3 启动前 owner 输入**:
  - Q-I3 MinIO 生产部署位置 + K8s 跨域配置（U10 文档预览依赖）
  - Q-I4 §3 手动子项弹层 (已闭环 → status=ratified, 与 U8 强耦合)
  - Q-I2 慧应用 OIDC 端点 (生产联调时由 IT/安全 提供)
- **Implementation-time unknowns summary**:
  - U7 LLM 建议 ≤ 5s 响应 (NFR-28) 需 swe 实施前 baseline perf 测量
  - U8 ko_assembly_hash SHA256 16 位截断的 collision risk 需在 spec plan 拍板后实测
  - U9 审计 12 月保留按月分区表的冷存储归档策略（R6 实施细节；冷存储由基础架构团队另行规划，非二期 R-09 多租户 scope）
  - U10 LibreOffice headless K8s 资源 (CPU / 内存) 限额

## Traceability Matrix

| Source Unit | PRD Requirement | Task(s) | Validation |
|-------------|------------------|---------|------------|
| U7 | R4 (知识治理 + 13 项处置项 + LLM 建议 + 仲裁) | T301, T302, T303, T304 | mvn verify + ConflictDetectorTest C1-C6 + LlmSuggestionTest DeepSeek mock + ConflictArbitratorTest OQ-8 + 仲裁 4 状态 |
| U8 | R5 (SNP/PRP + ko_assembly_hash + 时间线 + 陈旧快照) | T305, T306, T307 | mvn verify + SnapshotServiceTest (OQ-16 113 KO 装配) + StaleSnapshotJobTest (PAR 变更触发 stale) + 渲染期消费 ManualSubItems array schema |
| U9 | R6, R7, R8 (审计 12 月保留 + 项目 4 CRUD + 字典 6 + 9 量纲) | T308, T309, T310, T311, T312 | mvn verify + AuditServiceTest (OQ-5 简化 + OQ-11 ID 三重暴露) + ProjectServiceTest (OQ-7 归档仅冻结 KO) + DictServiceTest (软硬删除 + 引用完整性) + vitest |
| U10 | R9 (MinIO + PDF.js + LibreOffice + 30 分钟 token) | T313, T314, T315 | mvn verify + MinIOServiceTest (签名 token 30 分钟过期) + LibreOfficeConverterTest (DOCX→PDF + 缓存) + vitest |

## Task Graph

```
T301 (U7 数据模型 + 6 检测器) ─┐
                                ├─ T302 (U7 LLM 建议 + 仲裁 + 后端 API)
T301 ──────────────────────────┤
                                └─ T303 (U7 前端 ConflictsView + 仲裁面板)
                                            │
                                            └─ T304 (U7 后端测试)

T305 (U8 快照数据模型 + ko_assembly_hash) ─┐
                                             ├─ T306 (U8 前端 + 陈旧快照 Job)
T305 ──────────────────────────────────────┘
                                             └─ T307 (U8 测试)

T308 (U9 审计后端) ─────────┬─ T311 (U9 前端 AuditLogView)
                              ├─ T311 (U9 ProjectMgmtView)
T309 (U9 项目后端) ─────────┤
                              ├─ T311 (U9 DictMgmtView)
T310 (U9 字典后端) ─────────┘
                              │
                              └─ T312 (U9 测试)

T313 (U10 文档预览后端) ─────────┬─ T314 (U10 前端 DocPreviewModal)
                                  │
                                  └─ T315 (U10 测试)
```

并行约束：
- T301 完成前 T302 / T303 / T304 不能启动（数据模型基础）
- T302 / T303 可在 T301 完成后并行
- T305 完成前 T306 / T307 不能启动（快照数据基础，依赖 Q-I4 §3 array schema 已落地）
- T308 / T309 / T310 可并行（3 子模块独立数据模型）
- T311 依赖 T308 / T309 / T310（前端消费 3 个后端）
- T313 完成前 T314 / T315 不能启动（PDF.js + LibreOffice 容器基础）

## Execution Waves

本 task pack 内 4 个 wave（按 Implementation Unit 依赖 + 4 端跨端协作）

| Wave | Tasks | 阶段描述 |
|------|-------|----------|
| **Wave 1** | T301, T305, T308, T309, T310, T313 | 6 端数据模型基础 (V9005 + V9006 + V9007 + V9008 + V9009 + V9010) 并行启动 |
| **Wave 2** | T302, T303, T306, T311, T314 | 业务逻辑 + 前端 + LLM 建议 + 仲裁 + 快照前端 + U9 前端 + U10 前端并行 |
| **Wave 3** | T304, T307, T312, T315 | 集成测试 + E2E + 报告导出 + 部署验证 |

> 详细任务级 JSON 见下文 Task Pack Contract 段 (machine-readable canonical source)。

## Task Pack Contract

```json
{
  "schema_version": "task-pack/v1",
  "execution_waves": [
    {"wave": 1, "tasks": ["T301", "T305", "T308", "T309", "T310", "T313"]},
    {"wave": 2, "tasks": ["T302", "T303", "T306", "T311", "T314"]},
    {"wave": 3, "tasks": ["T304", "T307", "T312", "T315"]}
  ],
  "tasks": [
    {
      "task_id": "T301",
      "source_unit": "U7",
      "requirement_refs": ["R4"],
      "goal": "建立 U7 知识治理数据模型 (6 C 类 + 1 H 类冲突 + LLM 配额 + ConflictEntity + LlmQuotaEntity + KoStateMachine 5 状态转换守卫) + V9005 迁移",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/governance/model/ConflictEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/governance/model/LlmQuotaEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/governance/repository/ConflictMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/governance/repository/LlmQuotaMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/governance/service/ConflictDetector.java",
        "backend/src/main/resources/db/migration/V9005__create_governance_tables.sql",
        "backend/src/main/resources/prompts/conflict-suggestion.txt",
        "backend/src/test/java/com/liaogang/famou/km/governance/ConflictDetectorTest.java"
      ],
      "expected_side_effects": ["backend/pom.xml"],
      "test_focus": "ConflictDetector 6 C 类冲突 + 1 H 类健康 + 指纹算法去重 (MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12])",
      "done_signal": "mvn clean verify 通过 + ConflictDetectorTest 5+/5+ 覆盖 C1-C6 + H2 + 指纹命中复用旧 ID + V9005 迁移幂等执行 + conflict / llm_quota 表自动创建",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "数据模型完整性 + 冲突指纹算法边界 + DeepSeek v4 prompt 模板 (KO 双方内容 + 作用域 + 字段 → 建议/置信度/理由) 字段完整性",
      "stop_if": "需要新增冲突类型 / 调整指纹算法 / V9005 schema 变更但本 plan 未拍板"
    },
    {
      "task_id": "T302",
      "source_unit": "U7",
      "requirement_refs": ["R4"],
      "goal": "实现 U7 LLM 建议 (DeepSeek v4 + 配额管理 + ≤ 5s 超时 NFR-28) + 仲裁快路径 OQ-8 (Draft→Review→Approved→Published 自动 4 状态转换) + GovernanceController API",
      "dependencies": ["T301"],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/governance/service/LlmSuggestionService.java (wrapper over Sprint 1 已落地的 DeepSeekClient; 不重建底层 LLM client 以避免双重配额扣减)",
        "backend/src/main/java/com/liaogang/famou/km/governance/service/ConflictArbitrator.java",
        "backend/src/main/java/com/liaogang/famou/km/governance/controller/GovernanceController.java",
        "backend/src/test/java/com/liaogang/famou/km/governance/LlmSuggestionServiceTest.java",
        "backend/src/test/java/com/liaogang/famou/km/governance/ConflictArbitratorTest.java"
      ],
      "expected_side_effects": ["backend/src/main/java/com/liaogang/famou/km/audit/model/AuditLogEntity.java"],
      "test_focus": "mvn verify + LlmSuggestionServiceTest (DeepSeek v4 mock + ≤ 5s + 配额管理 + NFR-28) + ConflictArbitratorTest (OQ-8 快路径: 单次自动状态转换 + C6 <0.8 二次确认)",
      "done_signal": "mvn clean verify 通过 + LlmSuggestionServiceTest 通过 + ConflictArbitratorTest 通过 + 仲裁 4 状态一次性完成 + 审计日志 USER_CONFLICT_ARBITRATE 触发",
      "wave": 2,
      "review_gate": "required",
      "review_focus": "后端: 超时控制 + 配额扣减原子性 (Redis Lua 脚本) ; 前端: V3 视觉对比 + CSS 变量 + 响应式 ; 集成: LLM 异步处理 + 用户体验",
      "stop_if": "DeepSeek v4 端点 / 调用配额 / NFR-28 超时阈值调整",
      "risk_note": "DeepSeek v4 端点 42901 限流处理 + 5s 超时重试"
    },
    {
      "task_id": "T303",
      "source_unit": "U7",
      "requirement_refs": ["R4"],
      "goal": "实现 U7 前端 ConflictsView + 6 检测器卡 + 13 治理项 + 仲裁面板 + LlmQuotaTag + BatchActionBar",
      "dependencies": ["T301"],
      "files": [
        "frontend/src/views/conflicts/ConflictsView.vue",
        "frontend/src/components/ConflictRow.vue",
        "frontend/src/components/LlmQuotaTag.vue",
        "frontend/src/components/BatchActionBar.vue",
        "frontend/src/api/governance.ts",
        "frontend/src/router/governance.ts",
        "frontend/src/test/views/conflicts/ConflictsView.test.ts"
      ],
      "expected_side_effects": [],
      "test_focus": "pnpm dev + ConflictsView 渲染 6 检测器卡 + 13 治理项 + 仲裁面板 + LLM 调用 + 批量处置工具栏",
      "done_signal": "pnpm dev 无控制台错误 + 6 检测器卡全显 + 13 治理项全显 + LLM 配额 chip 可见 + 批量处置工具栏可用 + F-53.3 V3 视觉对比通过",
      "wave": 2,
      "review_gate": "optional",
      "review_focus": "前端: V3 视觉对比 + CSS 变量 + 响应式 ; 集成: LLM 异步 + 撤销",
      "stop_if": "重做 UI 框架 / 重做 router 框架",
      "risk_note": "前端当前与 §3 弹层共 Element Plus + 大列表渲染性能需压测"
    },
    {
      "task_id": "T304",
      "source_unit": "U7",
      "requirement_refs": ["R4"],
      "goal": "U7 后端集成测试 (ConflictDetector + LlmSuggestion + ConflictArbitrator 集成测试 + 报告 CSV 导出 + 批量处置 E2E)",
      "dependencies": ["T302", "T303"],
      "files": [
        "backend/src/test/java/com/liaogang/famou/km/governance/GovernanceIntegrationTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + 集成测试 6 C 类 + H2 + LLM mock 真实响应 + 仲裁 4 状态 + 批量处置 + CSV 导出",
      "done_signal": "mvn verify 通过 + 集成测试 PASS + 治理报告 CSV 含 6 列 + 仲裁发布后原 Active KO 被替换 + 旧版本归档",
      "wave": 3,
      "review_gate": "required",
      "review_focus": "集成: 跨模块交互 + 仲裁原子性 + 审计日志 USER_CONFLICT_ARBITRATE 触发",
      "stop_if": "集成测试覆盖不足或仲裁原子性破坏"
    },
    {
      "task_id": "T305",
      "source_unit": "U8",
      "requirement_refs": ["R5"],
      "goal": "建立 U8 提示词快照数据模型 (PromptSnapshotEntity + PromptRecordEntity + ko_assembly_hash 计算 + V9006 迁移) + 消费 Q-I4 §3 ManualSubItems array schema",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/snapshot/model/PromptSnapshotEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/model/PromptRecordEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/repository/PromptSnapshotMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/repository/PromptRecordMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/service/SnapshotService.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/service/StaleSnapshotJob.java",
        "backend/src/main/java/com/liaogang/famou/km/snapshot/controller/SnapshotController.java",
        "backend/src/main/resources/db/migration/V9006__create_snapshot_tables.sql",
        "backend/src/test/java/com/liaogang/famou/km/snapshot/SnapshotServiceTest.java"
      ],
      "expected_side_effects": ["backend/src/main/java/com/liaogang/famou/km/audit/model/AuditLogEntity.java"],
      "test_focus": "mvn verify + SnapshotServiceTest (OQ-16 113 KO 装配 + ko_assembly_hash 命中复用 + 陈旧快照标记 + 消费 ManualSubItems array schema)",
      "done_signal": "mvn verify 通过 + SnapshotServiceTest PASS + 113 KO 装配演示值 PASS + V9006 迁移幂等 + prompt_snapshot / prompt_record 表自动创建 + ko_assembly_hash 命中复用旧 SNP",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "后端: ko_assembly_hash 算法 (SHA256 16 位截断的 collision risk) + 陈旧快照扫描性能 ; 集成: 与 ComposerRenderService 中 manualSubItems array schema 兼容",
      "stop_if": "Q-I4 §3 §3 弹层改动未 merge 进 Sprint 3 分支 / V9006 schema 变更未拍板",
      "risk_note": "U8 强依赖 Q-I4 §3 ManualSubItems array schema (ManualSubItems DTO + dual-write dual-read)"
    },
    {
      "task_id": "T306",
      "source_unit": "U8",
      "requirement_refs": ["R5"],
      "goal": "U8 快照前端 (SnapshotsView 版本时间线 V1.0~V3.0 + SnapshotSelector PRP 选择下拉 + SnapshotTimelineNode 节点) + 陈旧快照 UI 标识",
      "dependencies": ["T305"],
      "files": [
        "frontend/src/views/snapshots/SnapshotsView.vue",
        "frontend/src/components/SnapshotSelector.vue",
        "frontend/src/components/SnapshotTimelineNode.vue",
        "frontend/src/components/StaleSnapshotBadge.vue",
        "frontend/src/api/snapshots.ts",
        "frontend/src/router/snapshots.ts",
        "frontend/src/test/views/snapshots/SnapshotsView.test.ts"
      ],
      "expected_side_effects": [],
      "test_focus": "pnpm dev + SnapshotsView V1.0~V3.0 时间线节点渲染 + MAJOR/MINOR/PATCH 颜色 + 变更清单 [+]/[~]/[-] + PRP 选择下拉可用",
      "done_signal": "pnpm dev 无控制台错误 + 时间线 17 个版本节点全显 + MAJOR 橙色 + MINOR 绿色 + PATCH 灰色 + 变更清单按符号显示 + 陈旧快照黄色 badge + F-53.3 V3 视觉对比通过",
      "wave": 2,
      "review_gate": "optional",
      "review_focus": "前端: V3 视觉对比 + 时间线 MAJOR/MINOR/PATCH 颜色 + 响应式",
      "stop_if": "V3 视觉对比不通过 / 时间线节点组件重构",
      "risk_note": "陈旧快照 badge 在大列表 (100+) 时的渲染性能"
    },
    {
      "task_id": "T307",
      "source_unit": "U8",
      "requirement_refs": ["R5"],
      "goal": "U8 后端集成测试 (SnapshotService + StaleSnapshotJob 集成测试 + PAR 变更触发陈旧快照 E2E + 113 KO 装配演示值回归)",
      "dependencies": ["T305"],
      "files": [
        "backend/src/test/java/com/liaogang/famou/km/snapshot/StaleSnapshotJobTest.java",
        "backend/src/test/java/com/liaogang/famou/km/snapshot/SnapshotIntegrationTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + StaleSnapshotJobTest (PAR KO current_value 变更触发 SNP_STALE_DETECTED 审计) + SnapshotIntegrationTest (渲染 → hash 命中复用 → PAR 变更 → stale)",
      "done_signal": "mvn verify 通过 + StaleSnapshotJobTest PASS + PAR 变更触发 stale + 审计 SNP_STALE_DETECTED 触发 + 113 KO 装配回归",
      "wave": 3,
      "review_gate": "required",
      "review_focus": "集成: hash 命中/未命中 + PAR 变更 stale + 13 段结构正确按 §10.5.1 装配 + 113 KO 计算",
      "stop_if": "陈旧快照扫描失败或审计 SNP_STALE_DETECTED 丢失"
    },
    {
      "task_id": "T308",
      "source_unit": "U9",
      "requirement_refs": ["R6"],
      "goal": "实现 U9 审计日志后端 (AuditLogService 12 月保留 + OQ-11 ID 三重暴露 + OQ-5 仅前端撤销) + V9007 迁移 (PK 复合 id + created_at 以支持按月分区表)",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/audit/model/AuditLogEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/audit/service/AuditLogService.java",
        "backend/src/main/java/com/liaogang/famou/km/audit/controller/AuditController.java",
        "backend/src/main/java/com/liaogang/famou/km/audit/repository/AuditLogMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/audit/enums/AuditAction.java",
        "backend/src/main/resources/db/migration/V9007__create_audit_tables.sql",
        "backend/src/test/java/com/liaogang/famou/km/audit/AuditLogServiceTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + AuditLogServiceTest (OQ-5 简化模型: 6 列不含 reverted_*, USER_DELETE_REVERT 等仅前端回滚不写 audit_log) + OQ-11 ID 三重暴露 (hover/详情/CSV) + V9007 按月分区表 PK 复合形态 (id + created_at)",
      "done_signal": "mvn verify 通过 + AuditServiceTest 通过 + 5 类操作写入 audit_log + ID 唯一 + V9007 迁移幂等 + 按月分区表结构就绪 + 12 月保留策略实现",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "OQ-5 简化 (无 reverted_* 字段) + OQ-11 ID 三重暴露 + 12 月按月分区表",
      "stop_if": "需要引入 reverted_at 字段 (违反 OQ-5) / 12 月保留要求变更"
    },
    {
      "task_id": "T309",
      "source_unit": "U9",
      "requirement_refs": ["R7"],
      "goal": "实现 U9 项目管理后端 (ProjectService 4 项目 CRUD + 切换器 + OQ-7 归档仅冻结 KO 不影响 PRP/SNP) + V9008 迁移",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/project/model/ProjectEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/project/repository/ProjectMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/project/service/ProjectService.java",
        "backend/src/main/java/com/liaogang/famou/km/project/controller/ProjectController.java",
        "backend/src/main/resources/db/migration/V9008__create_project_tables.sql",
        "backend/src/test/java/com/liaogang/famou/km/project/ProjectServiceTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + ProjectServiceTest (4 个项目 CRUD + status active/archived 切换 + OQ-7 归档仅冻结 KO 不影响 PRP/SNP + OQ-1 数据隔离 X-Project-Id 头过滤)",
      "done_signal": "mvn verify 通过 + 4 项目 CRUD 测试 PASS + status 切换 PASS + 归档仅冻结 KO PASS + V9008 迁移幂等 + project 表自动创建",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "OQ-1 数据隔离 (X-Project-Id 头) + OQ-7 归档不冻结 PRP/SNP + 跨项目 KO 不可见 (403)",
      "stop_if": "需要新增项目成员 / 项目角色 (违反 OQ-1 维持 v0.37) / 项目数量扩展 (4→其他)"
    },
    {
      "task_id": "T310",
      "source_unit": "U9",
      "requirement_refs": ["R8"],
      "goal": "实现 U9 字典管理后端 (DictService 6 字典 + 9 预制量纲 + 软/硬删除 + 引用完整性校验) + V9009 迁移",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/dict/model/DictEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/dict/repository/DictMapper.java",
        "backend/src/main/java/com/liaogang/famou/km/dict/service/DictService.java",
        "backend/src/main/java/com/liaogang/famou/km/dict/controller/DictController.java",
        "backend/src/main/java/com/liaogang/famou/km/dict/repository/DefaultDictLoader.java",
        "backend/src/main/java/com/liaogang/famou/km/dict/model/UnitEntity.java",
        "backend/src/main/resources/db/migration/V9009__create_dict_tables.sql",
        "backend/src/test/java/com/liaogang/famou/km/dict/DictServiceTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + DictServiceTest (6 字典 CRUD + 9 量纲 CRUD + 软删除禁用 + 硬删除引用完整性校验)",
      "done_signal": "mvn verify 通过 + DictServiceTest 通过 + 6 字典 + 9 量纲加载完成 + 软删除保留显示带 ⓘ + 硬删除被引用返回 403 + V9009 迁移幂等",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "软删除标记 disabled: true 保留显示 + 硬删除引用完整性校验 (SELECT COUNT 模式) + 9 量纲不可少 (部署门禁)",
      "stop_if": "9 量纲缺失 / 6 字典增加 / 软硬删除逻辑调整"
    },
    {
      "task_id": "T311",
      "source_unit": "U9",
      "requirement_refs": ["R6", "R7", "R8"],
      "goal": "实现 U9 前端 (AuditLogView 6 列 + ID hover tooltip + 详情 Modal + 筛选 + CSV 导出; ProjectMgmtView 4 项目 + 创建/编辑 + 归档; DictMgmtView 6 Tab + 编辑弹窗; ProjectSwitcher 顶部栏下拉)",
      "dependencies": ["T308", "T309", "T310"],
      "files": [
        "frontend/src/views/audit-log/AuditLogView.vue",
        "frontend/src/components/AuditLogDetailModal.vue",
        "frontend/src/components/AuditLogCsvExportButton.vue",
        "frontend/src/views/project-mgmt/ProjectMgmtView.vue",
        "frontend/src/components/ProjectSwitcher.vue",
        "frontend/src/views/dict-mgmt/DictMgmtView.vue",
        "frontend/src/components/DictEditModal.vue",
        "frontend/src/components/UnitConfigPanel.vue",
        "frontend/src/api/audit-log.ts",
        "frontend/src/api/project.ts",
        "frontend/src/api/dict.ts",
        "frontend/src/router/audit-log.ts",
        "frontend/src/router/project-mgmt.ts",
        "frontend/src/router/dict-mgmt.ts",
        "frontend/src/test/views/audit-log/AuditLogView.test.ts",
        "frontend/src/test/views/project-mgmt/ProjectMgmtView.test.ts",
        "frontend/src/test/views/dict-mgmt/DictMgmtView.test.ts"
      ],
      "expected_side_effects": [],
      "test_focus": "pnpm dev + AuditLogView 6 列渲染 + ID hover ≤100ms + 详情 Modal + CSV 导出 + ProjectMgmtView 4 项目 + 切换器 + DictMgmtView 6 Tab + 编辑 + vitest",
      "done_signal": "pnpm dev 无控制台错误 + AuditLogView 6 列全显 + hover tooltip ≤100ms + 项目 4 全显 + 字典 6 Tab 全显 + F-53.3 V3 视觉对比通过",
      "wave": 2,
      "review_gate": "optional",
      "review_focus": "前端: V3 视觉对比 + CSS 变量 + 响应式",
      "stop_if": "V3 视觉对比不通过 / 切换器重构",
      "risk_note": "AuditLogView 6 列 + ID 三重暴露组件一致性需与 VariableBindingModal 共享 design token (frontend-standards §7.1-7.3 + V3 视觉对比规范)"
    },
    {
      "task_id": "T312",
      "source_unit": "U9",
      "requirement_refs": ["R6", "R7", "R8"],
      "goal": "U9 后端集成测试 (AuditService + ProjectService + DictService 集成测试 + 跨模块 E2E)",
      "dependencies": ["T308", "T309", "T310"],
      "files": [
        "backend/src/test/java/com/liaogang/famou/km/audit/AuditIntegrationTest.java",
        "backend/src/test/java/com/liaogang/famou/km/project/ProjectIntegrationTest.java",
        "backend/src/test/java/com/liaogang/famou/km/dict/DictIntegrationTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + 集成测试 12 月保留按月分区 + 项目数据隔离 + 字典引用完整性 + 软硬删除 + E2E 跨模块",
      "done_signal": "mvn verify 通过 + 3 集成测试 PASS + 审计 12 月保留 PASS + 项目跨项目隔离 PASS + 字典硬删除 403 PASS",
      "wave": 3,
      "review_gate": "required",
      "review_focus": "集成: 跨模块交互 + 审计写入原子性 + 跨项目 KO 隔离",
      "stop_if": "审计写入失败 / 项目数据隔离破坏"
    },
    {
      "task_id": "T313",
      "source_unit": "U10",
      "requirement_refs": ["R9"],
      "goal": "实现 U10 文档预览后端 (MinIOService 签名 token 30 分钟 + DocPreviewService 6 格式分发 + LibreOfficeConverter DOCX/XLSX/PPTX 转 PDF) + V9010 迁移",
      "dependencies": [],
      "files": [
        "backend/src/main/java/com/liaogang/famou/km/document/model/DocEntity.java",
        "backend/src/main/java/com/liaogang/famou/km/document/service/MinIOService.java",
        "backend/src/main/java/com/liaogang/famou/km/document/service/DocPreviewService.java",
        "backend/src/main/java/com/liaogang/famou/km/document/service/LibreOfficeConverter.java",
        "backend/src/main/java/com/liaogang/famou/km/document/controller/DocPreviewController.java",
        "backend/src/main/resources/db/migration/V9010__create_doc_tables.sql",
        "backend/src/test/java/com/liaogang/famou/km/document/MinIOServiceTest.java",
        "backend/src/test/java/com/liaogang/famou/km/document/LibreOfficeConverterTest.java"
      ],
      "expected_side_effects": [
        "backend/pom.xml",
        "backend/src/main/java/com/liaogang/famou/km/audit/model/AuditLogEntity.java"
      ],
      "test_focus": "mvn verify + MinIOServiceTest (签名 token 生成 + 30 分钟过期校验 HMAC-SHA256) + LibreOfficeConverterTest (DOCX→PDF 转换 + 缓存命中)",
      "done_signal": "mvn verify 通过 + MinIOServiceTest 通过 + LibreOfficeConverterTest 通过 + V9010 迁移幂等 + 签名 token 30 分钟失效 + DOC_PREVIEW_ACCESSED 审计触发",
      "wave": 1,
      "review_gate": "required",
      "review_focus": "HMAC-SHA256 token 安全 + 30 分钟过期 + LibreOffice headless K8s 资源 (CPU/内存) + MinIO 跨域 CORS",
      "stop_if": "MinIO 部署位置未确认 (Q-I3) / token 过期阈值变更 / LibreOffice 资源超限 / pom.xml scope 未从 provided 改 compile"
    },
    {
      "task_id": "T314",
      "source_unit": "U10",
      "requirement_refs": ["R9"],
      "goal": "U10 文档预览前端 (DocPreviewModal 900px 宽弹窗 + DocPreviewPDF / DocPreviewText / DocPreviewImage 3 子组件 + UnsupportedFormatFallback)",
      "dependencies": ["T313"],
      "files": [
        "frontend/src/views/ko-doc/components/DocPreviewModal.vue",
        "frontend/src/components/DocPreviewPDF.vue",
        "frontend/src/components/DocPreviewText.vue",
        "frontend/src/components/DocPreviewImage.vue",
        "frontend/src/components/UnsupportedFormatFallback.vue",
        "frontend/src/api/document.ts",
        "frontend/src/router/document.ts",
        "frontend/src/test/components/DocPreviewModal.test.ts"
      ],
      "expected_side_effects": [],
      "test_focus": "pnpm dev + DocPreviewModal 渲染 6 格式分发 (PDF iframe / DOCX 转 PDF iframe / TXT 等宽 / PNG img / 其他 fallback 下载入口) + 客户端预校验 ≤10MB",
      "done_signal": "pnpm dev 无控制台错误 + DocPreviewModal 打开 + 6 格式分发全显 + 客户端预校验 ≤10MB PASS + F-53.3 V3 视觉对比通过",
      "wave": 2,
      "review_gate": "optional",
      "review_focus": "前端: V3 视觉对比 + 900px 弹窗宽度 + 格式 fallback 体验 + 响应式",
      "stop_if": "V3 视觉对比不通过 / PDF.js 重做",
      "risk_note": "PDF.js 自托管 + LibreOffice headless 容器镜像 K8s 资源 (Q-I3 依赖)"
    },
    {
      "task_id": "T315",
      "source_unit": "U10",
      "requirement_refs": ["R9"],
      "goal": "U10 后端集成测试 (MinIO + LibreOffice 集成测试 + 6 格式预览 E2E + token 过期 401)",
      "dependencies": ["T313"],
      "files": [
        "backend/src/test/java/com/liaogang/famou/km/document/DocPreviewIntegrationTest.java"
      ],
      "expected_side_effects": [],
      "test_focus": "mvn verify + 集成测试 6 格式预览 E2E + token 过期 401 + 缓存命中秒级返回 + DOC_PREVIEW_ACCESSED 审计",
      "done_signal": "mvn verify 通过 + DocPreviewIntegrationTest PASS + 6 格式全 PASS + token 过期 401 PASS + LibreOffice 缓存命中 + DOC_PREVIEW_ACCESSED 审计触发",
      "wave": 3,
      "review_gate": "required",
      "review_focus": "集成: MinIO + LibreOffice 6 格式 E2E + token 过期原子性 + DOC_PREVIEW_ACCESSED 审计",
      "stop_if": "MinIO 集成失败 / LibreOffice 不可达 / token 校验绕过"
    }
  ]
}
```

## Task Cards

### T301
- **source_unit**: U7
- **goal**: 建立 U7 知识治理数据模型 (6 C 类 + 1 H 类冲突 + LLM 配额 + ConflictEntity + LlmQuotaEntity + KoStateMachine 5 状态转换守卫) + V9005 迁移
- **dependencies**: []
- **files**: ConflictEntity.java + LlmQuotaEntity.java + ConflictMapper.java + LlmQuotaMapper.java + ConflictDetector.java + V9005 migration + conflict-suggestion.txt + ConflictDetectorTest.java
- **test_focus**: ConflictDetector 6 C 类冲突 + 1 H 类健康 + 指纹算法去重 (MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12])
- **done_signal**: mvn clean verify 通过 + ConflictDetectorTest 5+/5+ 覆盖 C1-C6 + H2 + 指纹命中复用旧 ID + V9005 迁移幂等
- **wave**: 1
- **review_gate**: required
- **review_focus**: 数据模型完整性 + 冲突指纹算法边界 + DeepSeek v4 prompt 模板字段完整性
- **stop_if**: 需要新增冲突类型 / 调整指纹算法 / V9005 schema 变更未拍板
- **risk_note**: KO ID 格式 KO-{TYPE}-{NNNN} 按类型独立自增；migration 顺序在 V9001-V9004 之后

### T302
- **source_unit**: U7
- **goal**: 实现 U7 LLM 建议 + 仲裁快路径 OQ-8 + GovernanceController API
- **dependencies**: T301
- **files**: LlmSuggestionService.java + ConflictArbitrator.java + GovernanceController.java + LlmSuggestionServiceTest.java + ConflictArbitratorTest.java
- **test_focus**: DeepSeek v4 mock + ≤5s 超时 + 配额管理 + 仲裁 4 状态一次性完成
- **done_signal**: mvn verify 通过 + DeepSeek mock ≤5s + 4 状态一次性完成 + USER_CONFLICT_ARBITRATE 审计
- **wave**: 2
- **review_gate**: required
- **review_focus**: 超时控制 + 配额扣减原子性 (Redis Lua) ; V3 视觉对比 ; LLM 异步处理
- **stop_if**: DeepSeek v4 端点 / NFR-28 阈值变更
- **risk_note**: DeepSeek v4 端点 42901 限流处理 + 5s 超时重试

### T303
- **source_unit**: U7
- **goal**: 实现 U7 前端 ConflictsView + 6 检测器卡 + 13 治理项 + 仲裁面板 + LlmQuotaTag + BatchActionBar
- **dependencies**: T301
- **files**: ConflictsView.vue + ConflictRow.vue + LlmQuotaTag.vue + BatchActionBar.vue + governance.ts + governance router + ConflictsView.test.ts
- **test_focus**: ConflictsView 渲染 6 检测器卡 + 13 治理项 + 仲裁面板 + LLM 调用 + 批量处置
- **done_signal**: pnpm dev 无控制台错误 + 6 卡全显 + 13 项全显 + 配额 chip + 批量处置 + V3 视觉对比通过
- **wave**: 2
- **review_gate**: optional
- **review_focus**: V3 视觉对比 + CSS 变量 + 响应式 ; LLM 异步 + 撤销
- **stop_if**: 重做 UI 框架 / 重做 router
- **risk_note**: 与 §3 弹层共用 Element Plus + 大列表渲染性能

### T304
- **source_unit**: U7
- **goal**: U7 后端集成测试 (冲突 + LLM + 仲裁 + 批量处置 + CSV 导出)
- **dependencies**: T302, T303
- **files**: GovernanceIntegrationTest.java
- **test_focus**: 6 C 类 + H2 + LLM mock + 4 状态 + 批量处置 + CSV
- **done_signal**: mvn verify + 集成 PASS + CSV 6 列 + 仲裁发布 + 旧版本归档
- **wave**: 3
- **review_gate**: required
- **review_focus**: 集成: 跨模块交互 + 仲裁原子性 + USER_CONFLICT_ARBITRATE 审计
- **stop_if**: 集成覆盖不足 / 仲裁原子性破坏

### T305
- **source_unit**: U8
- **goal**: 建立 U8 快照数据模型 + ko_assembly_hash 计算 + V9006 迁移 + 消费 Q-I4 §3 array schema
- **dependencies**: []
- **files**: PromptSnapshotEntity.java + PromptRecordEntity.java + SnapshotMapper.java + PromptRecordMapper.java + SnapshotService.java + StaleSnapshotJob.java + SnapshotController.java + V9006 migration + SnapshotServiceTest.java
- **test_focus**: OQ-16 113 KO 装配 + ko_assembly_hash 命中复用 + 陈旧快照标记
- **done_signal**: mvn verify + 113 KO 装配演示值 PASS + V9006 幂等 + ko_assembly_hash 命中复用
- **wave**: 1
- **review_gate**: required
- **review_focus**: ko_assembly_hash SHA256 16 位截断 collision risk + 陈旧快照扫描性能 + 与 ComposerRenderService 中 manualSubItems 兼容
- **stop_if**: Q-I4 §3 §3 弹层改动未 merge / V9006 schema 变更未拍板
- **risk_note**: U8 强依赖 Q-I4 §3 ManualSubItems array schema (ManualSubItems DTO + dual-write dual-read)

### T306
- **source_unit**: U8
- **goal**: U8 快照前端 (SnapshotsView V1.0~V3.0 + SnapshotSelector + SnapshotTimelineNode + StaleSnapshotBadge)
- **dependencies**: T305
- **files**: SnapshotsView.vue + SnapshotSelector.vue + SnapshotTimelineNode.vue + StaleSnapshotBadge.vue + snapshots.ts + router + SnapshotsView.test.ts
- **test_focus**: V1.0~V3.0 时间线 + MAJOR/MINOR/PATCH 颜色 + 变更清单 + PRP 选择
- **done_signal**: pnpm dev + 17 节点全显 + MAJOR 橙色 + MINOR 绿色 + PATCH 灰色 + 陈旧 badge + V3 视觉对比通过
- **wave**: 2
- **review_gate**: optional
- **review_focus**: V3 视觉对比 + MAJOR/MINOR/PATCH + 响应式
- **stop_if**: V3 视觉对比不通过 / 节点组件重构
- **risk_note**: 100+ 节点列表渲染性能

### T307
- **source_unit**: U8
- **goal**: U8 后端集成测试 (Snapshot + StaleSnapshotJob + 113 KO 装配回归)
- **dependencies**: T305
- **files**: StaleSnapshotJobTest.java + SnapshotIntegrationTest.java
- **test_focus**: hash 命中/未命中 + PAR 变更 stale + 13 段装配 + 113 KO
- **done_signal**: mvn verify + StaleSnapshotJobTest PASS + stale 触发 + SNP_STALE_DETECTED 审计
- **wave**: 3
- **review_gate**: required
- **review_focus**: hash 命中/未命中 + PAR 变更 stale + 13 段结构正确按 §10.5.1 装配 + 113 KO 计算
- **stop_if**: 陈旧快照扫描失败 / 审计 SNP_STALE_DETECTED 丢失

### T308
- **source_unit**: U9
- **goal**: 实现 U9 审计日志后端 (AuditService 12 月保留 + OQ-11 ID 三重暴露 + OQ-5 仅前端撤销) + V9007 迁移
- **dependencies**: []
- **files**: AuditLogEntity.java + AuditService.java + AuditController.java + AuditLogMapper.java + V9007 migration + AuditServiceTest.java
- **test_focus**: OQ-5 简化模型 (6 列) + OQ-11 ID 三重暴露
- **done_signal**: mvn verify + 5 类操作写入 audit_log + ID 唯一 + V9007 幂等 + 按月分区表 + 12 月保留
- **wave**: 1
- **review_gate**: required
- **review_focus**: OQ-5 简化 (无 reverted_*) + OQ-11 ID 三重暴露 + 12 月按月分区表
- **stop_if**: 引入 reverted_at 字段 (违反 OQ-5) / 12 月保留要求变更

### T309
- **source_unit**: U9
- **goal**: 实现 U9 项目管理后端 (ProjectService 4 项目 CRUD + 切换器 + OQ-7 归档仅冻结 KO) + V9008 迁移
- **dependencies**: []
- **files**: ProjectEntity.java + ProjectMapper.java + ProjectService.java + ProjectController.java + V9008 migration + ProjectServiceTest.java
- **test_focus**: 4 项目 CRUD + status active/archived + OQ-7 归档 + OQ-1 数据隔离 X-Project-Id 头
- **done_signal**: mvn verify + 4 项目 CRUD + status 切换 + 归档仅冻结 KO + V9008 幂等
- **wave**: 1
- **review_gate**: required
- **review_focus**: OQ-1 数据隔离 + OQ-7 归档不冻结 PRP/SNP + 跨项目 KO 不可见 (403)
- **stop_if**: 新增项目成员 / 项目角色 / 项目数量扩展

### T310
- **source_unit**: U9
- **goal**: 实现 U9 字典管理后端 (DictService 6 字典 + 9 量纲 + 软/硬删除 + 引用完整性) + V9009 迁移
- **dependencies**: []
- **files**: DictEntity.java + DictMapper.java + DictService.java + DictController.java + DefaultDictLoader.java + UnitEntity.java + V9009 migration + DictServiceTest.java
- **test_focus**: 6 字典 + 9 量纲 + 软删除 + 硬删除引用完整性
- **done_signal**: mvn verify + 6 字典 + 9 量纲加载 + 软删除 + 硬删除 403 + V9009 幂等
- **wave**: 1
- **review_gate**: required
- **review_focus**: 软删除 + 硬删除引用完整性 + 9 量纲不可少 (部署门禁)
- **stop_if**: 9 量纲缺失 / 6 字典增加 / 软硬删除逻辑调整

### T311
- **source_unit**: U9
- **goal**: 实现 U9 前端 (AuditLogView + ProjectMgmtView + DictMgmtView + ProjectSwitcher + 各 dialog)
- **dependencies**: T308, T309, T310
- **files**: AuditLogView.vue + AuditLogDetailModal.vue + AuditLogCsvExportButton.vue + ProjectMgmtView.vue + ProjectSwitcher.vue + DictMgmtView.vue + DictEditModal.vue + UnitConfigPanel.vue + 3 个 api 文件 + 3 个 router 文件 + 3 个 vitest
- **test_focus**: AuditLogView 6 列 + ID hover ≤100ms + CSV + ProjectMgmtView 4 项目 + 切换器 + DictMgmtView 6 Tab
- **done_signal**: pnpm dev + 6 列全显 + hover ≤100ms + 项目 4 全显 + 字典 6 Tab + V3 视觉对比通过
- **wave**: 2
- **review_gate**: optional
- **review_focus**: V3 视觉对比 + CSS 变量 + 响应式
- **stop_if**: V3 视觉对比不通过 / 切换器重构
- **risk_note**: AuditLogView 6 列 + ID 三重暴露组件一致性需与 VariableBindingModal 共享 design token (frontend-standards §7.1-7.3 + V3 视觉对比规范)

### T312
- **source_unit**: U9
- **goal**: U9 后端集成测试 (Audit + Project + Dict 集成 + 跨模块 E2E)
- **dependencies**: T308, T309, T310
- **files**: AuditIntegrationTest.java + ProjectIntegrationTest.java + DictIntegrationTest.java
- **test_focus**: 12 月按月分区 + 项目跨项目隔离 + 字典引用完整性 + 软硬删除
- **done_signal**: mvn verify + 3 集成 PASS + 12 月保留 + 数据隔离 + 硬删除 403
- **wave**: 3
- **review_gate**: required
- **review_focus**: 集成: 跨模块 + 审计写入原子性 + 跨项目 KO 隔离
- **stop_if**: 审计写入失败 / 项目数据隔离破坏

### T313
- **source_unit**: U10
- **goal**: 实现 U10 文档预览后端 (MinIOService 签名 token 30 分钟 + DocPreviewService 6 格式 + LibreOfficeConverter) + V9010 迁移
- **dependencies**: []
- **files**: DocEntity.java + MinIOService.java + DocPreviewService.java + LibreOfficeConverter.java + DocPreviewController.java + V9010 migration + 2 个 test
- **test_focus**: MinIOService 签名 token 30 分钟 + LibreOffice DOCX→PDF + 缓存命中
- **done_signal**: mvn verify + MinIOServiceTest + LibreOfficeConverterTest + V9010 幂等 + token 30 分钟失效 + DOC_PREVIEW_ACCESSED 审计
- **wave**: 1
- **review_gate**: required
- **review_focus**: HMAC-SHA256 token 安全 + 30 分钟过期 + LibreOffice headless K8s 资源 + MinIO 跨域 CORS
- **stop_if**: MinIO 部署位置未确认 (Q-I3) / token 过期阈值变更 / LibreOffice 资源超限
- **risk_note**: 部署依赖 Q-I3 (MinIO 生产部署位置 + K8s 跨域配置)

### T314
- **source_unit**: U10
- **goal**: U10 文档预览前端 (DocPreviewModal 900px + DocPreviewPDF / Text / Image + UnsupportedFormatFallback)
- **dependencies**: T313
- **files**: DocPreviewModal.vue + DocPreviewPDF.vue + DocPreviewText.vue + DocPreviewImage.vue + UnsupportedFormatFallback.vue + 2 个文件 + DocPreviewModal.test.ts
- **test_focus**: 6 格式分发渲染 + 客户端预校验 ≤10MB
- **done_signal**: pnpm dev + DocPreviewModal 打开 + 6 格式分发全显 + ≤10MB 预校验 PASS + V3 视觉对比通过
- **wave**: 2
- **review_gate**: optional
- **review_focus**: V3 视觉对比 + 900px 弹窗宽度 + 格式 fallback 体验 + 响应式
- **stop_if**: V3 视觉对比不通过 / PDF.js 重做
- **risk_note**: PDF.js 自托管 + LibreOffice headless 容器镜像 K8s 资源 (Q-I3 依赖)

### T315
- **source_unit**: U10
- **goal**: U10 后端集成测试 (MinIO + LibreOffice + 6 格式 E2E + token 过期 401)
- **dependencies**: T313
- **files**: DocPreviewIntegrationTest.java
- **test_focus**: 6 格式 E2E + token 过期 401 + 缓存命中秒级 + DOC_PREVIEW_ACCESSED 审计
- **done_signal**: mvn verify + 6 格式全 PASS + token 过期 401 PASS + 缓存命中 + DOC_PREVIEW_ACCESSED 审计
- **wave**: 3
- **review_gate**: required
- **review_focus**: 集成: MinIO + LibreOffice 6 格式 E2E + token 过期原子性 + DOC_PREVIEW_ACCESSED 审计
- **stop_if**: MinIO 集成失败 / LibreOffice 不可达 / token 校验绕过

## Orientation Evidence

- **provider**: direct-repo-reads
- **posture**: bounded
- **evidence_refs**:
  - `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md` U7-U10 sections（行 607-841）
  - `docs/brainstorms/liaogang-famou-km-platform-requirements.md` PRD v0.32 §5.2.3 + §5.2.4 + §5.2.6 + §5.2.2 + §5.2.5 + §5.2.11
  - `docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md` status=ratified 18 项决议
  - Q-I4 §3 commits: c04014a + 63606c8 + 946eda5（merged into main via ee5538b）
- **limitations**:
  - 当前 Sprint 3 实际代码实施尚未启动（`docs/tasks/2026-08-01-001-task-pack-sprint-3-governance-projects.md` 是 doc-only TP-3 准备）
  - Q-I3 MinIO 部署位置 (U10 依赖) 待基础架构团队在 Sprint 3 启动前确认
  - Q-I4 §3 §3 弹层改动已 merged into main via `ee5538b` (Sprint 3 worktree 含此 base via `7287b66`)
  - T303 / T306 / T311 / T314 frontend 模板态：F-53 V3 视觉对比依赖 Vite 构建跑通后截图

## Validation Notes

- **Source plan derivation**: Plan v0 已通过 Sprint 1+2 + Q-I4 共 3 轮 plan refinement; U7-U10 完整 definition 可直接编译 executable task pack
- **Source plan hash verification**: spec-first tasks hash 命令已跑，输出 `sha256:d5f212f065946b2fd5f533a81aa25e03ad02295a2572be3af9810de79bb8e402`
- **Old task pack rejection criteria**:
  - `source_plan_hash` 与 spec-first tasks hash 计算结果不匹配
  - `spec_id` 与 Plan frontmatter `spec_id: 2026-07-13-001-liaogang-famou-km-platform` 不一致
  - `mode: derived` 缺失或被改为 `transient`
  - `status: derived` 缺失或被改为 `draft`
- **Task validation (Sprint 3 启动后)**:
  - T301 验证: mvn clean verify + ConflictDetectorTest 5+ C1-C6 + H2 + 指纹命中复用 + V9005 幂等
  - T302 验证: LlmSuggestionServiceTest DeepSeek mock ≤5s + ConflictArbitratorTest 4 状态 + USER_CONFLICT_ARBITRATE 审计
  - T303 验证: ConflictsView 6 检测器卡 + 13 治理项 + 配额 chip + 批量处置 + F-53.3 V3 视觉对比
  - T304 验证: GovernanceIntegrationTest 集成 PASS + CSV 6 列 + 仲裁 4 状态 + 审计
  - T305 验证: SnapshotServiceTest OQ-16 113 KO 装配 + ko_assembly_hash 命中复用 + V9006 幂等
  - T306 验证: SnapshotsView V1.0~V3.0 时间线 + MAJOR/MINOR/PATCH 颜色 + StaleSnapshotBadge
  - T307 验证: StaleSnapshotJobTest PAR 变更 stale + SNP_STALE_DETECTED 审计
  - T308 验证: AuditServiceTest OQ-5 简化 + OQ-11 三重暴露 + V9007 幂等
  - T309 验证: ProjectServiceTest 4 项目 CRUD + status 切换 + OQ-7 归档 + V9008 幂等
  - T310 验证: DictServiceTest 6 字典 + 9 量纲 + 软硬删除 + V9009 幂等
  - T311 验证: AuditLogView / ProjectMgmtView / DictMgmtView 6 列 + 4 项目 + 6 Tab + V3 视觉对比
  - T312 验证: 3 个集成测试 PASS + 12 月保留 + 数据隔离 + 硬删除 403
  - T313 验证: MinIOServiceTest 签名 token 30 分钟 + LibreOfficeConverterTest + V9010 幂等
  - T314 验证: DocPreviewModal 6 格式分发 + ≤10MB 预校验 + V3 视觉对比
  - T315 验证: DocPreviewIntegrationTest 6 格式 E2E + token 过期 401 + DOC_PREVIEW_ACCESSED 审计

## Regeneration Rules

Rebuild this task pack when any of these changes:
- plan (U7-U10 Implementation Units or Scope Boundaries)
- V9005 / V9006 / V9007 / V9008 / V9009 / V9010 schema 变更
- Q-I3 MinIO 部署位置确认（影响 T313 / T314）
- Q-I4 §3 §3 弹层改动（已 merge 但若后续大改需同步）
- 9 量纲字典条目（影响 T310 部署门禁）
- DeepSeek v4 端点变更（影响 T302 LLM 建议）
- 5 预置角色权限矩阵变更（影响 U5，已 Sprint 2 落地）

If `source_plan_hash` does not match `sha256:d5f212f065946b2fd5f533a81aa25e03ad02295a2572be3af9810de79bb8e402`, execution must be rejected and the task pack must be rebuilt.

If `spec_id` does not match the current source plan (`2026-07-13-001-liaogang-famou-km-platform`), execution must be rejected as wrong-chain handoff and the task pack must be rebuilt from the source plan.

If execution triggers a task's `stop_if`, return to `spec-plan` or rerun `spec-write-tasks`.

## Related Sprint 1+2 Artifacts

- **`docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md`**：Sprint 1 task pack 模板（12 commit + 9 task；本 TP-3 沿用同一规范）
- **`docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md`**：Sprint 2 task pack 模板（U4 KO 库 + U5 权限 + U6 提示词；本 TP-3 沿用同一规范）
- **`docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md`**：Q-I4 §3 弹层 18 项决议（status=ratified，已 formal生效）
- **Q-I4 §3 commits**: c04014a + 63606c8 + 946eda5 (merged into main via ee5538b, Sprint 3 branch 含此 base via 7287b66)

## Quick Win (一行命令自查)

```bash
# Sprint 3 启动验证
cd backend && ./mvnw clean verify -Dspring.profiles.active=it -q
# 期望输出：BUILD SUCCESS，60+/60+ tests（前 41 backend + Sprint 2 增量 + Sprint 3 实施后递增）
```
