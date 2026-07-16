# TP-3+ Task Pack 模板（含 F-53 视觉验收）

> 目的：Sprint 3+ task pack 编写标准模板
> 关键改进（F-53）：done_signal 模板**必含前端 V3 视觉验收项**，避免 Sprint 1+2 的"任务规划阶段失误"重蹈覆辙
> 适用：所有后续 task pack（Sprint 3+ 实施时直接复制此模板）
> 关联：[F-53 solution](../solutions/build-errors/2026-07-16-001-frontend-style-divergence.md) · [frontend-standards.md](../contracts/frontend-standards.md) · [check-v3-style.sh](../../scripts/check-v3-style.sh)

---

## Frontmatter（必须按此格式）

```yaml
---
title: "辽港伐谋 KM 平台 MVP — Sprint N [模块名] Task Pack"
type: "task-pack"
status: "derived"
date: "YYYY-MM-DD"
spec_id: "2026-07-13-001-liaogang-famou-km-platform"  # 与 plan 文档一致
source_plan: "docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
source_plan_hash: "sha256:pending-validation"
generated_by: "spec-write-tasks 或 manual-compile-after-template"
mode: "derived"
source_sections:
  - "Summary"
  - "Requirements"
  - "Scope Boundaries"
  - "Key Technical Decisions"
  - "Implementation Units (U-X, U-Y, U-Z)"  # 列出本 sprint 涉及的 Implementation Unit
  - "System-Wide Impact"
  - "Risks & Dependencies"
  - "Open Questions"
  - "Completion Criteria"
target_repo: "."  # 仓库根（不要写子目录如 backend/，保持仓库级）
---
```

---

## Overview 模板

> **重要**：Overview 必含 F-53 教训引用（"为什么本 sprint done_signal 强调视觉验收"）。

```markdown
## Overview

Sprint N（计划 YYYY-MM-DD 完成）对应 PRD §八 里程碑 X + Sprint N 交付物，**包含 U-X / U-Y / U-Z 三个 Implementation Unit**：
- **U-X** 模块 1 ...
- **U-Y** 模块 2 ...
- **U-Z** 模块 3 ...

**Sprint N 完成后**：具体可测的成果...

**F-53 教训引用**（Frontend 任务必读）：
- Sprint 1+2 阶段，task pack done_signal 全部是后端指标（"X/Y tests pass"），前端任务因没视觉验收导致 8 个 view 视觉与 V3 原型差距大
- 本 sprint 强制前端任务 done_signal 必含"V3 视觉对比"（依据：[frontend-standards.md](../contracts/frontend-standards.md) + [check-v3-style.sh](../../scripts/check-v3-style.sh)）
- PR review 必跑 `bash scripts/check-v3-style.sh` 全部通过

**Sprint N 启动前依赖**（已就绪 / 不阻塞）：
- ✅ / ❌ ...
```

---

## Task Graph 模板

```markdown
## Task Graph

```
T201 (Implementation Unit-X: 任务名) ─┬─> T202 (任务名)
                                         ├─> T203 (任务名)
                                         └─> T204 (任务名)
                                                      │
T201 ─> T205 (Implementation Unit-Y: 任务名) ─> T206 (任务名)
                                                                                │
T201 + T205 ─> T208 (Implementation Unit-Z: 任务名) ─> T209 (任务名) ─> T210 (任务名) ─> T211 (任务名)
```

并行约束：
- T201 完成前 T202 / T203 / T204 / T205 / T208 都不能启动（实体基础）
- T202 / T203 / T204 可在 T201 完成后并行
- T205 / T206 / T207 在 T201 完成后启动
- T208 / T209 / T210 / T211 在 T201 + T205 完成后启动
```

---

## Traceability Matrix 模板

```markdown
## Traceability Matrix

| Source Unit | PRD Requirement | Task(s) | Validation |
|-------------|------------------|---------|------------|
| U-X | R1 | T201, T202 | 实体 + CRUD 测试 |
| U-Y | R2 | T203, T204 | UI + 审核流测试 |
| U-Z | R3 | T205, T206 | 模板 + 搜索测试 |
```

---

## Task Cards 模板（重点：done_signal 含视觉验收）

```markdown
## Task Cards

### T201
- **source_unit**: U-X
- **goal**: 任务目标（1-2 句）
- **dependencies**: T200（前置 task）/ []
- **files**:
  - `backend/src/main/java/.../Xxx.java`
  - `backend/src/test/java/.../XxxTest.java`
  - `frontend/src/views/.../XxxView.vue`（如果 frontend 任务）
- **test_focus**: 测试目标
- **done_signal**（**必含 V3 视觉验收**，F-53 强制项）：
  - [ ] 后端 mvn clean verify 通过（X/Y tests pass）
  - [ ] 端点 /api/xxx curl HTTP 200
  - [ ] **前端：bash scripts/check-v3-style.sh 全部通过**
  - [ ] **前端：V3 视觉对比**（V3 原型 vs 当前 view 截图，标注差异）
  - [ ] **前端：CSS 变量使用 ≥ 5**（grep "var(--" 输出验证）
  - [ ] **前端：响应式 1024px + 768px 双断点截图**
  - [ ] 集成：端到端流程
- **review_focus**（**必含 V3 视觉对比**）：
  - 后端：业务逻辑 + 边界条件 + 性能
  - **前端：V3 视觉对比 + CSS 变量使用 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端流程
- **risk_note**: 风险点
- **stop_if**: 遇到 X 情况则停止（触发回 plan）
- **wave**: 1 / 2 / 3 / 4

### T202
（同 T201 格式）
```

### done_signal 必含项（F-53 强制）

**Frontend 任务的 done_signal 必须包含**：

```yaml
done_signal: |
  - [ ] 后端：mvn clean verify 通过（X/Y tests pass）
  - [ ] 后端：端点 /api/xxx curl HTTP 200
  - [ ] **前端：bash scripts/check-v3-style.sh 全部通过**（F-53 强制）
  - [ ] **前端：V3 视觉对比**（V3 原型 line XXX-line YYY 区块 vs 当前 PR 截图）
  - [ ] **前端：CSS 变量使用 ≥ 5**（grep "var(--" 输出验证）
  - [ ] **前端：响应式 1024px + 768px 双断点截图**
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.17.X 入口更新
```

**Backend 任务的 done_signal 不强制视觉验收**，但应包含：

```yaml
done_signal: |
  - [ ] 后端：mvn clean verify 通过（X/Y tests pass）
  - [ ] 后端：端点 /api/xxx curl HTTP 200
  - [ ] 集成：端到端流程截图
  - [ ] 文档：CHANGELOG.md v1.17.X 入口更新
```

### review_focus 必含项

**Frontend 任务**：
```yaml
review_focus:
  - 后端：业务逻辑 + 边界条件 + 性能
  - **前端：V3 视觉对比 + CSS 变量使用 + 响应式**（依据 frontend-standards.md §7.1-7.3）
  - 集成：端到端流程
  - **PR 截图附件**：V3 原型 vs 当前 view 双截图（1024px + 768px 至少 4 张）
```

**Backend 任务**：
```yaml
review_focus:
  - 后端：业务逻辑 + 边界条件 + 性能
  - 数据库：迁移幂等 + 索引设计
  - 集成：端到端流程
```

---

## Validation Notes 模板

```markdown
## Validation Notes

- **Source plan derivation**: Plan v0 已通过 9 轮 spec-doc-review；Implementation Unit X / Y / Z 详细描述齐全可直接拆 task pack
- **Source plan hash verification**: 跑 `npx spec-first tasks validate docs/tasks/[TP-N].md --json` 验证（待执行）
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
- **Task validation (Sprint N 启动后)**:
  - T201 验证：mvn verify + XxxTest 通过 + ...
  - T202 验证：... + **check-v3-style.sh 通过**（frontend 任务）
```

---

## Regeneration Rules 模板

```markdown
## Regeneration Rules

- **重建触发条件**（任一）：
  - Plan 文档内容变更（source_plan_hash 不匹配）
  - 22 OQ 决议变更
  - spec_id 变更
  - U-X / U-Y / U-Z 中任一 Implementation Unit 的 Files / Test scenarios / Verification 变更
  - Sprint 拆分边界需调整
- **不重建条件**：
  - 后续 Sprint 独立编译
  - PRD 内容变更但 U-X/Y/Z 实施细节未变
  - CHANGELOG / Design 文档更新
- **校验命令**（executable 前必跑）：
  ```bash
  npx spec-first tasks validate docs/tasks/[TP-N].md --json
  ```
- **F-53 强制校验**（frontend 任务额外跑）：
  ```bash
  bash scripts/check-v3-style.sh
  ```

---

## Quick Win 自查命令

```bash
# 后端
cd backend && ./mvnw clean verify -Dspring.profiles.active=it

# 前端（PR review 必跑）
cd frontend
bash scripts/check-v3-style.sh   # 必含 5 项检测
./node_modules/.bin/vite build    # 必含 320+ modules
pnpm dev                          # 浏览器实测 + 1024px/768px 双断点截图
```

---

## 相关文档

- [F-53 solution](../solutions/build-errors/2026-07-16-001-frontend-style-divergence.md)：F-53 根因分析
- [frontend-standards.md](../contracts/frontend-standards.md)：V3 视觉验证标准（10 章节）
- [check-v3-style.sh](../../scripts/check-v3-style.sh)：自动检测 5 项
- [Sprint 1 TP-1 模板](../../tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md)：参考
- [Sprint 2 TP-2 实例](../../tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md)：参考

---

**维护者**：Sprint 3+ 实施时所有 task pack 必用此模板。done_signal 漏前端视觉验收 = 视觉回归风险（F-53 教训）。
