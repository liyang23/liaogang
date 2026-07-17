---
title: "辽港伐谋 KM 平台 MVP — Sprint 3 知识治理 + 快照 + 审计/项目/字典 + 文档预览 Task Pack"
type: "task-pack"
status: "derived"
date: "2026-08-01"
spec_id: "2026-07-13-001-liaogang-famou-km-platform"
source_plan: "docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
source_plan_hash: "sha256:pending-validation"
generated_by: "manual-compile-after-sprint-2-template"
mode: "derived"
source_sections:
  - "Summary"
  - "Requirements"
  - "Scope Boundaries"
  - "Key Technical Decisions"
  - "Implementation Units (U7, U8, U9, U10)"
  - "System-Wide Impact"
  - "Risks & Dependencies"
  - "Open Questions"
  - "Completion Criteria"
target_repo: "."
---

# 辽港伐谋 KM 平台 MVP — Sprint 3 知识治理 + 快照 + 审计/项目/字典 + 文档预览 Task Pack

## Overview

Sprint 3（计划 2026-08-31 完成）对应 PRD §八 里程碑 5 + Sprint 3 交付物，**包含 U7 / U8 / U9 / U10 四个 Implementation Unit**：
- **U7 知识治理模块**（6 C 类冲突 C1-C6 + 1 H 类 H2 检测 + LLM 主动建议 OQ-9 接 DeepSeek v4 + 仲裁快路径 OQ-8 + 批量处置 + 治理报告导出）
- **U8 提示词快照模块**（SNP/PRP + ko_assembly_hash + 时间线 + 陈旧快照流程，§5.2.4 v0.32 重构要点）
- **U9 审计日志 + 项目管理 + 字典管理模块**（3 独立子模块：审计 12 月保留 FR-26 + 项目 CRUD 4 个 + 字典CRUD 6 字典 + LLM 配额 + OQ-11 三重暴露）
- **U10 文档预览模块**（MinIO + PDF.js + LibreOffice headless 容器镜像 + 30 分钟 token 鉴权 + 跨域 K8s 资源）

**Sprint 3 完成后**：知识治理实时检测 + LLM 主动建议（≤5s）+ 管理员仲裁快路径；PRP 快照支持陈旧检测与一键回滚；审计可导出 12 月记录 + 项目/字典 CRUD 完整；DOC 类型 PDF 浏览器内预览 + LibreOffice 跨格式支持（docx/xlsx/pptx）。

**F-53 教训引用**（Frontend 任务必读）：
- Sprint 1+2 阶段 task pack done_signal 全是后端指标，前端任务因没视觉验收导致 9 个 view 视觉与 V3 原型差距大
- Sprint 3 强制前端任务 done_signal 必含"V3 视觉对比"（依据 [frontend-standards.md](../../contracts/frontend-standards.md) + [check-v3-style.sh](../../scripts/check-v3-style.sh)）
- PR review 必跑 `bash scripts/check-v3-style.sh` 全部通过

**Sprint 3 启动前依赖**：
- ✅ Sprint 1（U1+U2+U3）+ Sprint 2（U4+U5+U6 + 11 task + 41/41 backend 测试 + 33/33 vitest + 320+ modules frontend）已落地
- ✅ 集成测试环境（MySQL 真实环境 km_platform_it） + 集成测试 4 集成用例
- ✅ P1 三层编译门禁（pre-commit / pre-push / GitHub Actions）
- ✅ Q-I1 完整 4 项已收齐（端点 百度千帆 + deepseek-v4-flash + 配额 100K TPM / 10M TPD / 5000 元月成本 / 100K 上下文）
- ✅ Q-I2 慧应用 OIDC 端点（生产联调时由 IT/安全 提供 + dev/test 端用 mock fallback）
- ✅ Q-I4 §3 手动子项弹层 UI 细节（2026-07-17 业务专家 sparring 已闭环，参见 docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md；与 T310 U9 无技术依赖）
- ⏳ Q-I3 MinIO 生产部署（K8s 资源 + 跨域配置 — U10 文档预览依赖）

## Source Summary

- **Source plan**: `docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`（约 1000 行，10 U 完整定义 + Sprint 拆分 + 责任分配）
- **Source PRD**: `docs/brainstorms/liaogang-famou-km-platform-requirements.md`（PRD v0.32 + 22 OQ 全部 grill 闭合）
- **Consumed source sections**: Summary / Requirements / Scope Boundaries / Key Technical Decisions / Implementation Units (U7, U8, U9, U10) / System-Wide Impact / Risks & Dependencies / Open Questions / Completion Criteria
- **Scope boundaries**:
  - **保留 in-scope**: U7 知识治理（6 C 冲突 + 1 H 健康 + LLM 建议 DeepSeek v4 + OQ-8 仲裁快路径 + OQ-9 配额管理 + 治理报告导出）/ U8 PRP 快照（§5.2.4 v0.32 重构：ko_assembly_hash + 陈旧快照流程）/ U9 审计 12 月 + 项目 CRUD 4 个 + 字典CRUD 6 个 + LLM 配额 + OQ-11 三重暴露（hover tooltip / 弹窗 / CSV 导出）/ U10 DOC 预览（MinIO + PDF.js + LibreOffice headless 30 分钟 token）
  - **移除 out-of-scope**（Sprint 4 / 后期）：U7 跨项目冲突检测（仅同项目内冲突）/ U8 完整时间机器回放（仅快照 + 陈旧检测，不含完整 diff 历史）/ U9 跨租户权限 / U10 实时协同编辑（PDF.js 静态预览为主）
  - **存疑保留 deferred**: U7 仲裁准确率（LLM 建议仅作参考，最终人工仲裁）/ U8 快照存储成本（SNP/PRP 关联 ko_version + prm_template）/ U9 字典 schema 跨版本兼容 / U10 LibreOffice 容器 OOM 调优（K8s 资源）
- **Implementation-time unknowns**: U7 冲突指纹算法边界（OQ-9 LLM 异步处理 + 配额扣减原子性）/ U8 ko_assembly_hash 算法选择（MD5 vs SHA-256 + 时间线 snapshot 粒度）/ U9 LLM 配额审计周期（12 月保留 + 监控告警）/ U10 LibreOffice 容器 K8s 资源 request/limit

## Traceability Matrix

| Source Unit | PRD Requirement | Task(s) | Validation |
|-------------|------------------|---------|------------|
| U7 | R4（知识治理：6 C 冲突 + LLM 建议 + 仲裁快路径）| T301, T302, T303, T304, T305 | mvn verify + ConflictDetectorTest C1-C6 + H2 + LLM mock ≤5s 响应 + 仲裁 4 状态一次性完成 + 配额管理 + 治理报告 CSV |
| U8 | R5（PRP 快照：SNP/PRP + ko_assembly_hash + 时间线 + 陈旧快照）| T306, T307 | mvn verify + SnapshotServiceTest（hash 一致性 + 陈旧检测 + 一键回滚）+ 快照列表/详情 API |
| U9 | R6（审计 12 月 + 项目CRUD + 字典CRUD + LLM 配额 + OQ-11 三重暴露）| T308, T309, T310, T311 | mvn verify + AuditLogServiceTest 12 月保留 + ProjectMgmtServiceTest 4 个项目 CRUD + DictMgmtServiceTest 6 字典CRUD + LlmQuotaServiceTest 配额管理 + 前端 hover/弹窗/CSV 三重暴露 |
| U10 | R7（DOC 预览：MinIO + PDF.js + LibreOffice headless + 30 分钟 token）| T312, T313 | mvn verify + DocPreviewServiceTest（PDF.js + LibreOffice）+ PreviewControllerTest（30 分钟 token + MinIO 签名 URL）|

## Task Graph

```
T301 (U7 冲突检测：ConflictDetector + 6 C + 1 H 指纹算法) ─┐
                                                                   ├─> T302 (U7 LLM 建议：DeepSeek v4 + 配额管理 + ≤5s)
T301 ─> T303 (U7 仲裁快路径：ConflictArbitrator + 4 状态自动) ─┤
                                                                   └─> T304 (U7 前端：ConflictsView + ConflictRow + LlmQuotaTag + BatchActionBar)
                                                                                       │
T301 ─> T305 (U7 后端测试：ConflictDetectorTest + LlmSuggestionTest + ConflictArbitratorTest) ─┘

T306 (U8 后端：SnapshotService + ko_assembly_hash + 时间线 + 陈旧检测) ─┬─> T307 (U8 前端：SnapshotsView V3 + 还原按钮)
                                                                                          │
T306 ─> T308 (U8 测试：SnapshotServiceTest 哈希一致性 + 陈旧检测) ───────────────────────┘

T309 (U9 审计后端：AuditLogService 12 月保留 FR-26 + OQ-11 三重暴露) ─┬─> T310 (U9 项目后端：ProjectMgmtService 4 个 CRUD) ─┬─> T311 (U9 字典后端：DictMgmtService 6 个 CRUD)
                                                                                                                                                                                  ├─> T312 (U9 前端：AuditLogView + ProjectMgmtView + DictMgmtView V3 完整)
                                                                                                                                                                                  ├─> T313 (U9 测试：AuditLogServiceTest + ProjectMgmtServiceTest + DictMgmtServiceTest)

T314 (U10 后端：DocPreviewService + PDF.js + LibreOffice headless 30 分钟 token) ─┬─> T315 (U10 前端：DocPreviewView + PDF.js + MinIO 签名 URL)
                                                                                                   │
T314 ─> T316 (U10 测试：DocPreviewServiceTest PDF.js + LibreOffice + PreviewControllerTest 30 分钟 token) ─┘
```

并行约束：
- T301 完成前 T302 / T303 / T304 / T305 都不能启动（冲突检测算法基础）
- T302 / T303 可在 T301 完成后并行
- T306 完成前 T307 / T308 都不能启动（快照服务基础）
- T309 / T310 / T311 可在 U9 启动后并行
- T314 完成前 T315 / T316 都不能启动（预览服务基础）

## Execution Waves

| Wave | Tasks | 阶段描述 |
|------|-------|----------|
| 1 | T301, T306, T309, T310, T311, T314 | 6 个服务基础（U7 冲突检测 + U8 快照 + U9 审计/项目/字典 + U10 预览）并行启动 |
| 2 | T302, T303, T307, T312, T313, T315 | 业务逻辑 + 前端 + LLM 建议 + 仲裁 + 快照前端 + U9 前端 + U10 前端并行 |
| 3 | T304, T305, T308, T316 | 集成测试 + E2E + 报告导出 + 部署验证 |

## Task Cards

### T301
- **source_unit**: U7
- **goal**: 实现 U7 知识治理核心 — 6 C 类冲突（C1-C2-C3-C4-C5-C6）+ 1 H 类健康（H2）检测算法（§5.2.3.1 指纹算法 MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12]）
- **dependencies**: T201 / T202（U4 KO 库已落地）
- **files**:
  - `backend/src/main/java/.../governance/model/ConflictEntity.java`
  - `backend/src/main/java/.../governance/model/LlmQuotaEntity.java`
  - `backend/src/main/java/.../governance/service/ConflictDetector.java`
  - `backend/src/main/java/.../governance/service/LlmSuggestionService.java`
  - `backend/src/main/java/.../governance/service/ConflictArbitrator.java`
  - `backend/src/main/java/.../governance/repository/ConflictMapper.java`
  - `backend/src/main/java/.../governance/repository/LlmQuotaMapper.java`
  - `backend/src/main/resources/db/migration/V9005__create_governance_tables.sql`
  - `backend/src/main/resources/prompts/conflict-suggestion.txt`（DeepSeek v4 prompt 模板：KO 双方内容 + 作用域 + 字段 → 建议 / 置信度 / 理由）
  - `backend/src/test/java/.../governance/ConflictDetectorTest.java`（C1-C6 + H2 指纹算法）
- **test_focus**: mvn verify + ConflictDetectorTest 覆盖 6 C 类冲突 + 1 H 类健康 + 指纹算法去重
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过（X/Y tests pass）
  - [ ] 6 个 C 类冲突检测器 + 1 个 H 类健康检测器按 §5.2.3.1 实现
  - [ ] **前端：bash scripts/check-v3-style.sh 全部通过**（F-53 强制）
  - [ ] **前端：V3 视觉对比**（V3 原型 §5.2.3 区块 vs 当前 PR 截图）
  - [ ] **前端：CSS 变量使用 ≥ 5**（grep "var(--" 输出验证）
  - [ ] **前端：响应式 1024px + 768px 双断点截图**
  - [ ] **前端：F-54 验证**（handlebars evaluateCondition 支持无空格写法）
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.18.X 入口更新
- **review_focus**:
  - 后端：业务逻辑 + 边界条件 + 性能（指纹算法 O(n²) → 大规模 KO 性能）
  - **前端：V3 视觉对比 + CSS 变量使用 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端流程
  - **PR 截图附件**：V3 原型 vs 当前 view 至少 4 张
- **risk_note**: 指纹算法边界（KO 内容变更后冲突不成立需标记 auto_resolved）；LLM 异步处理 + 配额扣减原子性（Redis Lua）；冲突类型清单 §5.2.3.1.1 9 行 × 4 列
- **stop_if**: 需新增冲突类型 / 调整指纹算法（重新做 plan review）；或 Q-I1 LLM 端点变更
- **wave**: 1

### T302
- **source_unit**: U7
- **goal**: 实现 LLM 主动建议服务 — 调用 DeepSeek v4（Q-I1）+ 配额管理（Redis INCR 每日 0 点重置）+ ≤5s 响应（NFR-28）
- **dependencies**: T301
- **files**:
  - `backend/src/main/java/.../governance/service/LlmSuggestionService.java`（含 5s 超时控制）
  - `backend/src/main/java/.../governance/service/LlmQuotaService.java`（平台级 + 用户级配额）
  - `backend/src/main/java/.../governance/repository/LlmQuotaMapper.java`
  - `backend/src/main/resources/prompts/conflict-suggestion.txt`（DeepSeek v4 prompt 模板）
  - `backend/src/test/java/.../governance/LlmSuggestionServiceTest.java`（mock DeepSeek + ≤5s + 配额管理）
- **test_focus**: mvn verify + LlmSuggestionServiceTest（mock DeepSeek + 5s 超时 + 配额管理 + NFR-28）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] LLM 建议接口 ≤5s 响应（NFR-28 严格）
  - [ ] 配额管理（平台 + 用户级 80/20）
  - [ ] 前端：bash check-v3-style + V3 视觉对比 + CSS 变量 ≥ 5 + 响应式 1024px+768px
  - [ ] F-54 验证（handlebars evaluateCondition）
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.18.X 入口更新
- **review_focus**:
  - 后端：超时控制（避免挂起）+ 配额扣减原子性（Redis Lua 脚本）
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：LLM 异步处理 + 用户体验
- **risk_note**: DeepSeek v4 端点 42901 限流处理 + 5s 超时重试
- **stop_if**: Q-I1 LLM 端点变更；或 NFR-28 超时阈值调整
- **wave**: 2

### T303
- **source_unit**: U7
- **goal**: 实现仲裁快路径 OQ-8 — 管理员点击"仲裁并发布" → 自动 Draft→Review→Approved→Published 4 状态转换
- **dependencies**: T301
- **files**:
  - `backend/src/main/java/.../governance/service/ConflictArbitrator.java`
  - `backend/src/main/java/.../governance/controller/GovernanceController.java`（API：检测 / 建议 / 仲裁 / 批量处置 / 报告导出）
  - `backend/src/test/java/.../governance/ConflictArbitratorTest.java`（OQ-8 快路径：单次自动状态转换 + C6 置信度 <0.8 二次确认）
- **test_focus**: mvn verify + ConflictArbitratorTest（单次自动 4 状态转换 + 审计日志 USER_CONFLICT_ARBITRATE + C6 <0.8 弹 Modal）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 仲裁快路径（OQ-8）一次操作完成 4 状态转换
  - [ ] 审计日志 USER_CONFLICT_ARBITRATE 触发
  - [ ] C6 置信度 <0.8 二次确认 Modal
  - [ ] 前端：bash check-v3-style + V3 视觉对比 + CSS 变量 ≥ 5 + 响应式 1024px+768px
  - [ ] F-54 验证
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：审计日志写入原子性 + 状态转换一致性
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：批查 + 报告导出
- **risk_note**: 二次确认边界（C6 0.8 阈值可调）；批查性能（1000+ 冲突场景）
- **stop_if**: 仲裁准确率持续低 / 审计日志丢失
- **wave**: 2

### T304
- **source_unit**: U7
- **goal**: 实现 U7 前端 — ConflictsView V3 完整还原 + 6 检测器卡 + 13 治理项 + 仲裁面板 + LlmQuotaTag + BatchActionBar
- **dependencies**: T302, T303
- **files**:
  - `frontend/src/views/conflicts/ConflictsView.vue`（替换占位）
  - `frontend/src/components/ConflictRow.vue`（冲突详情行 + LLM 建议 + 置信度条 + 处置下拉 + 仲裁按钮）
  - `frontend/src/components/LlmQuotaTag.vue`（"余 1/2 次" 配额 chip）
  - `frontend/src/components/BatchActionBar.vue`（批量处置工具栏）
  - `frontend/src/api/conflict.ts`（API 客户端）
  - `frontend/src/test/views/conflicts/ConflictsView.test.ts`（F-53 强制 vitest）
- **test_focus**: pnpm dev + ConflictsView 渲染 6 检测器卡 + 13 治理项 + 仲裁按钮 + 批量操作
- **done_signal**:
  - [ ] 前端：bash check-v3-style.sh 全部通过（F-53 强制）
  - [ ] 前端：V3 视觉对比（V3 原型 §5.2.3 治理页 vs 当前 PR）
  - [ ] 前端：CSS 变量使用 ≥ 5
  - [ ] 前端：响应式 1024px + 768px 双断点
  - [ ] 前端：F-54 验证（handlebars evaluateCondition 支持无空格）
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端流程
  - **PR 截图附件**：V3 原型 vs 当前 view 至少 4 张
- **risk_note**: F-53.3 强制 — done_signal 漏 V3 视觉验收 = 视觉回归风险
- **stop_if**: 需新增 6 类以外冲突类型 / 调整 LLM 配额策略
- **wave**: 2

### T305
- **source_unit**: U7
- **goal**: U7 后端测试 + 集成 — ConflictDetectorTest + LlmSuggestionServiceTest + ConflictArbitratorTest 完整覆盖
- **dependencies**: T301, T302, T303
- **files**:
  - `backend/src/test/java/.../governance/ConflictDetectorTest.java`（6 C 冲突 + 1 H 健康 + 指纹算法）
  - `backend/src/test/java/.../governance/LlmSuggestionServiceTest.java`（mock DeepSeek + ≤5s + 配额管理）
  - `backend/src/test/java/.../governance/ConflictArbitratorTest.java`（OQ-8 快路径）
- **test_focus**: mvn verify + 41 → 60+ 测试目标（含治理 12+ 测试）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过（X/Y tests pass ≥ 60）
  - [ ] 6 检测器 + 1 健康 + LLM mock + 仲裁 + 指纹去重全部覆盖
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式（同步 F-53 强制项）
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：测试覆盖 + 边界条件
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: LLM mock 5s 超时边缘 case 漏测
- **stop_if**: 6 C 类冲突测试覆盖不足
- **wave**: 3

### T306
- **source_unit**: U8
- **goal**: 实现 U8 提示词快照核心 — SnapshotService + ko_assembly_hash（MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12]）+ 时间线 + 陈旧快照检测
- **dependencies**: T208, T211
- **files**:
  - `backend/src/main/java/.../prompt/model/SnapshotEntity.java`
  - `backend/src/main/java/.../prompt/service/SnapshotService.java`
  - `backend/src/main/java/.../prompt/repository/SnapshotMapper.java`
  - `backend/src/main/java/.../prompt/service/HashService.java`（ko_assembly_hash 计算）
  - `backend/src/main/java/.../prompt/controller/SnapshotController.java`（API：列表/详情/创建/还原）
  - `backend/src/main/resources/db/migration/V9006__create_snapshot_tables.sql`
  - `backend/src/test/java/.../prompt/SnapshotServiceTest.java`（哈希一致性 + 陈旧检测 + 一键回滚）
- **test_focus**: mvn verify + SnapshotServiceTest（哈希一致 + 陈旧检测 + 一键回滚 + 关联 ko_version + prm_template）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 快照 ko_assembly_hash 算法（MD5 12 字符）
  - [ ] 陈旧快照检测（关联 ko_version + prm_template 已变更 → 标记 stale）
  - [ ] 一键回滚（创建新 snapshot + 恢复关联 KO 状态）
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] F-54 验证
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：哈希算法一致性 + 存储成本
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: 快照存储成本（SNP/PRP 关联 ko_version + prm_template 增长快）
- **stop_if**: 哈希算法选型变更 / 存储成本失控
- **wave**: 1

### T307
- **source_unit**: U8
- **goal**: 实现 U8 前端 — SnapshotsView V3 完整还原（快照列表 + 时间线 + 还原按钮 + 哈希显示）
- **dependencies**: T306
- **files**:
  - `frontend/src/views/prompts/SnapshotsView.vue`（替换占位 · 完整 V3 还原）
  - `frontend/src/components/SnapshotRow.vue`（快照详情行 + 哈希 badge + 还原按钮）
  - `frontend/src/api/snapshot.ts`（API 客户端）
  - `frontend/src/test/views/prompts/SnapshotsView.test.ts`（F-53 强制 vitest）
- **test_focus**: pnpm dev + SnapshotsView 渲染快照列表 + 时间线 + 还原按钮
- **done_signal**:
  - [ ] 前端：bash check-v3-style.sh 全部通过（F-53 强制）
  - [ ] 前端：V3 视觉对比（V3 原型 §5.2.4 快照页）
  - [ ] 前端：CSS 变量使用 ≥ 5
  - [ ] 前端：响应式 1024px + 768px 双断点
  - [ ] 前端：F-54 验证（handlebars evaluateCondition）
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端流程
  - **PR 截图附件**：V3 原型 vs 当前 view 至少 4 张
- **risk_note**: F-53.3 强制
- **stop_if**: 需新增快照类型 / 调整哈希算法
- **wave**: 2

### T308
- **source_unit**: U8
- **goal**: U8 后端测试 — SnapshotServiceTest 完整覆盖（哈希一致性 + 陈旧检测 + 一键回滚 + 边界）
- **dependencies**: T306
- **files**:
  - `backend/src/test/java/.../prompt/SnapshotServiceTest.java`（覆盖 6 个边界 case）
- **test_focus**: mvn verify + SnapshotServiceTest
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 哈希一致性 + 陈旧检测 + 一键回滚全部覆盖
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式（同步 F-53 强制项）
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：测试覆盖
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
- **risk_note**: 陈旧检测边界
- **stop_if**: 哈希算法变更
- **wave**: 3

### T309
- **source_unit**: U9
- **goal**: 实现 U9 审计后端 — AuditLogService 12 月保留（FR-26 验收 #6）+ OQ-11 三重暴露（hover tooltip / 弹窗 / CSV 导出）
- **dependencies**: T202
- **files**:
  - `backend/src/main/java/.../audit/AuditLogController.java`（增强：列表 / 详情 / CSV 导出）
  - `backend/src/main/java/.../audit/service/AuditQueryService.java`（12 月保留 + 索引优化）
  - `backend/src/main/java/.../audit/repository/AuditLogMapper.java`（增强：按时间 / 用户 / action 索引）
  - `backend/src/test/java/.../audit/AuditLogServiceTest.java`（12 月保留 + 三重暴露）
- **test_focus**: mvn verify + AuditLogServiceTest（12 月保留 + hover + 弹窗 + CSV 导出）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 12 月保留（FR-26 验收 #6）
  - [ ] OQ-11 三重暴露（hover + 弹窗 + CSV）
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] F-54 验证
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：12 月保留 + 索引
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: 12 月保留成本（按月分区 + 老数据归档）
- **stop_if**: 12 月保留要求变更
- **wave**: 1

### T310
- **source_unit**: U9
- **goal**: 实现 U9 项目管理后端 — ProjectMgmtService 4 个项目 CRUD + 状态管理（active/archived）+ KO 关联
- **dependencies**: T201
- **files**:
  - `backend/src/main/java/.../project/model/ProjectEntity.java`（已存在，扩展 status 字段）
  - `backend/src/main/java/.../project/service/ProjectService.java`（扩展 CRUD）
  - `backend/src/main/java/.../project/repository/ProjectMapper.java`（扩展分页）
  - `backend/src/main/java/.../project/controller/ProjectController.java`（扩展 status 切换）
  - `backend/src/test/java/.../project/ProjectServiceTest.java`（4 个项目 + status 切换）
- **test_focus**: mvn verify + ProjectServiceTest（4 个项目 + 状态管理）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 4 个项目 CRUD（active/archived status + KO 关联数）
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] F-54 验证
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：4 个项目 + status 切换
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: ~~Q-I4 §3 手动子项弹层 UI 细节（sparring 业务专家）~~ **已闭环 2026-07-17**：Q-I4 sparring 议程决议归档 `docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md`；Round 1 minimum viable 8 项 + Round 2 跟进 5 项 + 5 项 PRD 已锁边界全部拍板完成占位决议（待业务专家复审）。Q-I4 是 §3 弹层实施批次（`docs/plans/2026-07-17-001-feat-liaogang-section3-manual-subitem-modal-plan.md`）的事，与 T310 U9 项目管理无技术依赖；T310 启动可推进，但 §3 弹层 U8 快照前置依赖 U3 ManualSubItems 类型迁移完成。
- **stop_if**: 新增项目类型 / status enum 变更
- **wave**: 1

### T311
- **source_unit**: U9
- **goal**: 实现 U9 字典管理后端 — DictMgmtService 6 字典CRUD（类型介绍 / 效力分级 / 权威分级 / 类型分组 / 知识对象概念 / 量纲配置）
- **dependencies**: T201
- **files**:
  - `backend/src/main/java/.../dict/model/DictionaryEntity.java`
  - `backend/src/main/java/.../dict/model/DictionaryItemEntity.java`
  - `backend/src/main/java/.../dict/service/DictMgmtService.java`
  - `backend/src/main/java/.../dict/repository/DictionaryMapper.java`
  - `backend/src/main/java/.../dict/repository/DictionaryItemMapper.java`
  - `backend/src/main/java/.../dict/controller/DictController.java`
  - `backend/src/main/java/.../dict/service/DefaultDictLoader.java`（V9001 seed 6 字典）
  - `backend/src/main/resources/db/migration/V9007__create_dict_tables.sql`
  - `backend/src/main/resources/seed/default-dicts.yaml`
  - `backend/src/test/java/.../dict/DictMgmtServiceTest.java`（6 字典 + 项 CRUD）
- **test_focus**: mvn verify + DictMgmtServiceTest（6 字典 + 项CRUD + 跨版本 schema 兼容）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] 6 字典 + 默认 seed 加载完成
  - [ ] 项CRUD + 跨版本 schema 兼容
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] F-54 验证
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：6 字典 + schema 兼容
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: 跨版本 schema 兼容（V9001 seed 6 字典可能 V2 → V3 不兼容）
- **stop_if**: 新增字典类型 / schema 大改
- **wave**: 1

### T312
- **source_unit**: U9
- **goal**: 实现 U9 前端 — AuditLogView + ProjectMgmtView + DictMgmtView V3 完整还原（12 个 + 4 个 + 6 个）
- **dependencies**: T309, T310, T311
- **files**:
  - `frontend/src/views/audit/AuditLogView.vue`（V3 完整还原 · 已有占位 · 替换）
  - `frontend/src/views/project/ProjectMgmtView.vue`（V3 完整还原 · 已有占位 · 替换）
  - `frontend/src/views/dict/DictMgmtView.vue`（V3 完整还原 · 已有占位 · 替换）
  - `frontend/src/api/audit.ts` / `frontend/src/api/project.ts` / `frontend/src/api/dict.ts`
  - `frontend/src/test/views/audit/AuditLogView.test.ts` / `project/ProjectMgmtView.test.ts` / `dict/DictMgmtView.test.ts`（F-53 强制 vitest）
- **test_focus**: pnpm dev + 3 个 view 渲染完整 V3 风格
- **done_signal**:
  - [ ] 前端：bash check-v3-style.sh 全部通过（F-53 强制 · **T312 重点校验**）
  - [ ] 前端：V3 视觉对比（V3 原型 §5.2.5 审计页 vs 当前 PR）
  - [ ] 前端：CSS 变量使用 ≥ 5
  - [ ] 前端：响应式 1024px + 768px 双断点
  - [ ] 前端：F-54 验证
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**（F-53 强制 · 重点）
  - 集成：端到端流程
  - **PR 截图附件**：3 个 view 至少 6 张（每个 view 2 断点）
- **risk_note**: F-53.3 强制 — 视觉回归风险
- **stop_if**: 新增视图类型 / 调整 V3 视觉
- **wave**: 2

### T313
- **source_unit**: U9
- **goal**: U9 后端测试 + 集成 — AuditLogServiceTest + ProjectServiceTest + DictMgmtServiceTest 完整覆盖
- **dependencies**: T309, T310, T311
- **files**:
  - `backend/src/test/java/.../audit/AuditLogServiceTest.java`（12 月保留 + OQ-11 三重暴露）
  - `backend/src/test/java/.../project/ProjectServiceTest.java`（4 个项目 + status 切换）
  - `backend/src/test/java/.../dict/DictMgmtServiceTest.java`（6 字典 + 项CRUD）
- **test_focus**: mvn verify + 60+ 测试目标（含 U9 新增 15+ 测试）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过（X/Y tests pass ≥ 75）
  - [ ] U9 完整覆盖（12 月保留 + 4 项目 + 6 字典 + 配额 + 三重暴露）
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] 集成：端到端流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：测试覆盖
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端流程
- **risk_note**: 12 月保留成本
- **stop_if**: 12 月保留要求变更 / 字典 schema 改
- **wave**: 3

### T314
- **source_unit**: U10
- **goal**: 实现 U10 文档预览后端 — DocPreviewService + PDF.js（PDF 格式）+ LibreOffice headless（docx/xlsx/pptx 转换 PDF）+ 30 分钟 token 鉴权 + MinIO 签名 URL
- **dependencies**: T202 (U4 已有 ko_type='DOC' KO)
- **files**:
  - `backend/src/main/java/.../docpreview/service/DocPreviewService.java`（PDF.js + LibreOffice + token 管理）
  - `backend/src/main/java/.../docpreview/service/DocTokenService.java`（30 分钟 token + MinIO 签名 URL）
  - `backend/src/main/java/.../docpreview/controller/DocPreviewController.java`（API：token 签发 + 代理文件流）
  - `backend/src/main/java/.../docpreview/service/LibreOfficeService.java`（headless 转换 docx/xlsx/pptx → PDF）
  - `backend/src/main/java/.../docpreview/model/DocTokenEntity.java`
  - `backend/src/main/java/.../docpreview/repository/DocTokenMapper.java`
  - `backend/src/main/resources/db/migration/V9008__create_doc_preview_tables.sql`
  - `k8s/libreoffice/`（K8s Job/Deployment manifest 供 headless 转换）
  - `backend/src/test/java/.../docpreview/DocPreviewServiceTest.java`（PDF.js + LibreOffice + 30 分钟 token）
  - `backend/src/test/java/.../docpreview/PreviewControllerTest.java`（30 分钟 token + MinIO 签名 URL）
- **test_focus**: mvn verify + DocPreviewServiceTest（PDF.js + LibreOffice + 30 分钟 token）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过
  - [ ] PDF.js + LibreOffice 跨格式（PDF/docx/xlsx/pptx → PDF）
  - [ ] 30 分钟 token 鉴权 + MinIO 签名 URL
  - [ ] K8s 资源（headless 转换 Pod 资源 request/limit）
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] F-54 验证
  - [ ] 集成：端到端流程（PDF 浏览器内预览）
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：PDF.js + LibreOffice 集成 + K8s 资源
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端 PDF 预览
- **risk_note**: LibreOffice 容器 OOM（K8s 资源 request/limit 需调优）；Q-I3 MinIO 生产部署
- **stop_if**: 新增文档格式 / 调整 K8s 资源
- **wave**: 1

### T315
- **source_unit**: U10
- **goal**: 实现 U10 前端 — DocPreviewView V3 完整还原（PDF.js + 跨域 MinIO 签名 URL + 30 分钟 token 鉴权）
- **dependencies**: T314
- **files**:
  - `frontend/src/views/doc/DocPreviewView.vue`（替换占位 · V3 完整还原）
  - `frontend/src/components/PdfViewer.vue`（PDF.js wrapper · 跨域 CORS）
  - `frontend/src/api/doc.ts`（API 客户端）
  - `frontend/src/test/views/doc/DocPreviewView.test.ts`（F-53 强制 vitest）
- **test_focus**: pnpm dev + DocPreviewView 渲染 PDF + 30 分钟 token 鉴权 + MinIO 签名 URL
- **done_signal**:
  - [ ] 前端：bash check-v3-style.sh 全部通过（F-53 强制）
  - [ ] 前端：V3 视觉对比（V3 原型 §5.2.6 文档预览 vs 当前 PR）
  - [ ] 前端：CSS 变量使用 ≥ 5
  - [ ] 前端：响应式 1024px + 768px 双断点
  - [ ] 前端：F-54 验证
  - [ ] 集成：端到端 PDF 预览流程
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端 PDF 预览
  - **PR 截图附件**：V3 原型 vs 当前 view 至少 4 张
- **risk_note**: F-53.3 强制
- **stop_if**: 新增文档格式 / 调整 K8s 资源
- **wave**: 2

### T316
- **source_unit**: U10
- **goal**: U10 后端测试 + 集成 — DocPreviewServiceTest（PDF.js + LibreOffice）+ PreviewControllerTest（30 分钟 token + MinIO 签名 URL）
- **dependencies**: T314
- **files**:
  - `backend/src/test/java/.../docpreview/DocPreviewServiceTest.java`（PDF.js + LibreOffice 跨格式）
  - `backend/src/test/java/.../docpreview/PreviewControllerTest.java`（30 分钟 token + MinIO 签名 URL）
- **test_focus**: mvn verify + 80+ 测试目标（含 U10 新增 10+ 测试）
- **done_signal**:
  - [ ] 后端 mvn clean verify 通过（X/Y tests pass ≥ 85）
  - [ ] PDF.js + LibreOffice + token + MinIO 签名 URL 完整覆盖
  - [ ] 前端：bash check-v3-style + V3 + CSS + 响应式
  - [ ] 集成：端到端 PDF 预览
  - [ ] 文档：CHANGELOG.md v1.18.X
- **review_focus**:
  - 后端：PDF.js + LibreOffice + token
  - **前端：V3 视觉对比 + CSS 变量 + 响应式**
  - 集成：端到端
- **risk_note**: LibreOffice 容器 OOM
- **stop_if**: 新增文档格式
- **wave**: 3

## Validation Notes

- **Source plan derivation**: Plan v0 已通过 10 轮 spec-doc-review；U7 / U8 / U9 / U10 详细描述齐全可直接拆 task pack
- **Source plan hash verification**: 跑 `npx spec-first tasks validate docs/tasks/[TP-3].md --json` 验证（待执行）
- **Old task pack rejection criteria**:
  - `source_plan_hash` 与 `npx spec-first tasks hash` 计算结果不匹配
  - `spec_id` 与 Plan frontmatter 不一致
  - `mode: derived` 缺失或被改
  - `status: derived` 缺失或被改
- **F-53 强制项**（每个 frontend task 必查）：
  - [ ] view 文件从 `theme.scss` import V3 变量（不硬编码）
  - [ ] 使用 `.page-header` / `.stat-card` / `.lst-item` 工具类
  - [ ] 布局用 CSS Grid（不用 el-row + el-col）
  - [ ] 等宽数字用 `JetBrains Mono` 字体
  - [ ] stat-card value 字号 28px
  - [ ] stat-card breakdown 用虚线分隔
  - [ ] 按钮用 .btn / .btn-primary（圆角 2px）
  - [ ] list 用 .alert-item（border-bottom 分隔）
  - [ ] `bash scripts/check-v3-style.sh` 通过
  - [ ] 浏览器实测：1024px 宽 + 768px 窄双断点截图
- **Task validation (Sprint 3 启动后)**:
  - T301 验证：mvn verify + ConflictDetectorTest 6 + 1 检测
  - T302 验证：mvn verify + LlmSuggestionServiceTest ≤5s + 配额
  - T303 验证：mvn verify + ConflictArbitratorTest 4 状态自动
  - T304 验证：vite build + ConflictsView V3 还原 + 13 治理项
  - T305 验证：mvn verify + 60+ 测试目标
  - T306 验证：mvn verify + SnapshotServiceTest 哈希一致
  - T307 验证：vite build + SnapshotsView V3
  - T308 验证：mvn verify + 6 边界 case
  - T309 验证：mvn verify + 12 月保留 + OQ-11 三重暴露
  - T310 验证：mvn verify + 4 个项目 + status 切换
  - T311 验证：mvn verify + 6 字典 + 项CRUD
  - T312 验证：vite build + 3 view V3 完整 + **F-53 强制 check-v3-style**
  - T313 验证：mvn verify + 75+ 测试
  - T314 验证：mvn verify + PDF.js + LibreOffice + 30 分钟 token
  - T315 验证：vite build + DocPreviewView V3
  - T316 验证：mvn verify + 85+ 测试

## Regeneration Rules

- **重建触发条件**（任一）：
  - Plan 文档内容变更（source_plan_hash 不匹配）
  - 22 OQ 决议变更（特别是 OQ-8 仲裁快路径 / OQ-9 LLM 主动建议 / OQ-10 检测器版本号 / OQ-11 三重暴露）
  - spec_id 变更
  - U7 / U8 / U9 / U10 中任一 Implementation Unit 的 Files / Test scenarios / Verification 变更
  - Sprint 3 拆分边界需调整
- **不重建条件**：
  - 后续 Sprint 独立编译
  - PRD 内容变更但 U7-U10 实施细节未变
  - CHANGELOG / Design 文档更新
- **校验命令**（executable 前必跑）：
  ```bash
  npx spec-first tasks validate docs/tasks/[TP-3].md --json
  ```
- **F-53 强制校验**（frontend 任务额外跑）：
  ```bash
  bash scripts/check-v3-style.sh
  ```

## Related Documentation

- [Sprint 1 TP-1 模板](../../tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md)
- [Sprint 2 TP-2 实例](../../tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md)
- [F-53 处理链路](../solutions/build-errors/2026-07-16-001-frontend-style-divergence.md)
- [frontend-standards.md](../../contracts/frontend-standards.md)
- [check-v3-style.sh](../../scripts/check-v3-style.sh)
- [task-pack-template.md](../../templates/task-pack-template.md)
- [原型 V3 HTML](../../../辽港伐谋知识管理平台_原型_V3.html)
- [plan 文档](../../plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md)

---

**维护者**：Sprint 3 实施时所有 task 必用此模板。done_signal 漏前端视觉验收 = 视觉回归风险（F-53 教训）。
