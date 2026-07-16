---
title: "前端样式 vs 原型 V3 差距大（任务规划阶段没把'还原 V3 视觉'纳入 done_signal）"
type: "solution"
status: "captured"
date: "2026-07-16"
spec_id: "2026-07-13-001-liaogang-famou-km-platform"
source_issue: "项目预览时用户发现页面样式与原型 V3 差距非常大"
source_refs:
  - "/Users/liyang129/data/liaogang/辽港伐谋知识管理平台_原型_V3.html"
  - "DESIGN.md"
  - "docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md"
  - "docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md"
  - "frontend/src/views/**/*.vue"
related_docs:
  - "CHANGELOG.md v1.16.7"
  - "CHANGELOG.md v1.17.x（TP-2 完成）"
  - "docs/solutions/integration-issues/minio-port-semantics.md"
captured_by: "manual-F-53"
severity: "high"
recurrence_risk: "high（若无明确 done_signal，下次 sprint 会重蹈覆辙）"
invalidation_condition: "若项目放弃 V3 视觉还原（改用 Element Plus 默认主题 + 重新设计系统），本方案失效"
source_refs:
  - "docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md"
  - "frontend/src/components/Sidebar.vue"
  - "frontend/src/views/prompts/ComposerView.vue"
  - "DESIGN.md"
tags:
  - "frontend"
  - "prototype-v3"
  - "ui-regression"
  - "design-system"
  - "task-planning"
  - "sprint-1"
  - "sprint-2"
  - "v1.16.7"
---

# F-53：前端样式 vs 原型 V3 差距大（任务规划阶段失误）

## Problem（现象）

项目预览时用户访问 http://localhost:5173/ 发现页面样式与原型 V3 差距非常大：

- 当前用 Element Plus 默认主题（蓝色按钮 + 圆角卡片 + 浅色背景）
- V3 用深色 Rail 侧栏（#0F1E2E）+ 端口蓝主题色（#0F4C75）+ JetBrains Mono + Noto Sans SC 字体 + 大量自定义 class（`.stat-card` / `.page-header` / `.toolbar` 等）
- 8 个 view 是 Sprint 1 路由预配但**未实施**的占位文件（dashboard / audit-log / conflicts / dict-mgmt / project-mgmt / permissions-prompts / snapshots / NotFound）

## Root Cause（根因 — 任务规划阶段）

### 五个阶段评估

| 阶段 | 状态 | 说明 |
|------|------|------|
| 1. 设计阶段（DESIGN.md） | ✅ 已定义 | port-blue #0F4C75 + JetBrains Mono + Noto Sans SC + 6 个主题色 token |
| 2. 需求阶段（plan 文档） | ⚠️ 部分 | §10.7.3.1 只引用 V3 函数名（`handleTypeSearch` / `renderComposerSections` / `onPRMTemplateChange`）— 业务逻辑对齐，**未要求视觉 1:1 还原** |
| **3. 任务规划阶段（TP-2 task pack）** | **❌ 失败** | **T201-T211 全部 11 个 task 的 `done_signal` 都是后端指标（"X/Y tests pass" / "vite build 通过"）— 没有任何 task 把"前端视觉 1:1 还原 V3"作为验收条件** |
| 4. 实施阶段（Sprint 1 + Sprint 2 view） | ❌ 偏离 | Sprint 1 router 预配 13 个 view + Sprint 2 实施 6 个 view（KoLibrary / KoTypeList / KoDetail / Permissions / Composer / Login）— 全部只用 Element Plus 默认主题 + 简单 class，**未应用 V3 自定义 class 系统**（`page-header` / `stat-card` / `toolbar` / `btn-primary` 等）|
| 5. 验证阶段 | ⚠️ 部分 | mvn verify + vite build 通过 ≠ 视觉符合 V3。后端 + 前端编译指标都过，但**没视觉验收** |

### 直接成因（具体证据）

#### 证据 1：TP-2 task pack T203（U4 前端 KO 库）的 done_signal

```yaml
- test_focus: pnpm dev 启动 + KoLibraryView 渲染 6 类型入口卡片 + 列表页 Tab 切换 + 详情页形式化定义 + 跨类搜索
- done_signal: pnpm dev 无控制台错误 + 6 类型列表页可达 + 详情页加载 1 条种子数据
- review_focus: UI 与原型 V3 line 3799-3846 (handleLibSearch) 一致性
```

`done_signal` 只看"无控制台错误" + "可达" + "加载数据" — **没有"视觉与 V3 一致"**。`review_focus` 提了"与原型 V3 handleLibSearch 一致性"，但只指函数名一致性，不是视觉一致性。

#### 证据 2：当前 frontend 实际状态

```
$ find src -name "*.scss" -o -name "*.css"
src/styles/theme.scss                ← 只有 1 个 CSS 文件（DESIGN 主题色定义）

$ grep -l "占位 view" src/views -r
src/views/NotFoundView.vue
src/views/conflicts/ConflictsView.vue
src/views/project/ProjectMgmtView.vue
src/views/prompts/SnapshotsView.vue
src/views/dict/DictMgmtView.vue
src/views/dashboard/DashboardView.vue
src/views/audit/AuditLogView.vue
src/views/prompts/PromptsView.vue      ← 8 个占位 view
```

8 个 view 是 Sprint 1 路由预配但**完全未实施**的占位文件。

#### 证据 3：V3 CSS 设计系统（实际定义）

```css
:root {
  --bg-canvas: #F5F6F8;
  --bg-paper: #FFFFFF;
  --bg-rail: #0F1E2E;          /* 深色侧栏 */
  --bg-rail-active: #173552;
  --bg-grid: #ECEEF1;
  --line: #DCE0E6;
  --line-strong: #B0B7C0;
  --text-primary: #1A2332;
  --text-secondary: #5A6373;
  --text-tertiary: #8A92A0;
  --text-on-dark: #E5EAF0;
  --text-on-dark-dim: #8FA0B5;
  --port-blue: #0F4C75;         /* 主题色 */
  --port-blue-light: #1E6A9D;
  --steel: #2D3748;
  --signal-orange: #ED8936;
  --signal-orange-deep: #C26418;
  --signal-red: #C53030;
  --signal-green: #2F855A;
  --signal-yellow: #D69E2E;
}

font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
.mono { font-family: 'JetBrains Mono', monospace; }
```

V3 定义了**20+ 个 CSS 变量** + 2 套字体系统。**当前 frontend 完全没引入这套设计系统**。

## What Didn't Work（之前尝试 / 失败方案）

1. **Sprint 1 实施 view 失败**：只关注"router 跑通" + "占位 placeholder"，没要求"视觉符合 V3"
2. **TP-2 task pack 缺视觉验收**：T201-T211 全部 11 个 done_signal 都是后端指标
3. **DESIGN.md 没强约束**：定义了主题色但 task pack 没引用 DESIGN.md 主题
4. **plan 文档 §10.7.3.1 引用 V3 不充分**：只列函数名（业务逻辑），没要求"按 V3 视觉还原"

## Solution（修复方案 — 短期 + 长期）

### F-53.1 短期 — 立即应用 DESIGN.md 主题色（最少改动）

1. **引入 V3 CSS 变量**到 `src/styles/theme.scss`：
   ```scss
   :root {
     --bg-canvas: #F5F6F8;
     --bg-paper: #FFFFFF;
     --bg-rail: #0F1E2E;
     --bg-rail-active: #173552;
     --port-blue: #0F4C75;
     --port-blue-light: #1E6A9D;
     --steel: #2D3748;
     --signal-orange: #ED8936;
     --signal-red: #C53030;
     --signal-green: #2F855A;
     --signal-yellow: #D69E2E;
     --text-primary: #1A2332;
     --text-secondary: #5A6373;
     --text-on-dark: #E5EAF0;
   }
   ```
2. **改 Sidebar** 应用 `--bg-rail` 深色侧栏 + `--port-blue` active 状态
3. **改 TopBar** 应用 `--bg-paper` + `--text-primary`
4. **改 KO Library / List / Detail** 应用 `.page-header` + `.stat-card` + `.toolbar` V3 风格

### F-53.2 中期 — 实施占位 view + 还原 V3 关键页面（T212-T222）

| Task | View | V3 关键还原点 |
|------|------|-----------------|
| T212 | DashboardView | `.stat-card` × 4（6 类型 KO 数量统计 + 项目数 + 活动数）+ 趋势图 |
| T213 | ConflictsView | `.alert-item` 列表 + 6 类型冲突标签 + LLM 建议 |
| T214 | AuditLogView | `.alert-item` 审计列表 + 3 重暴露 (Hover/弹窗/CSV) |
| T215 | DictMgmtView | `.lst-value` 字典值大数字 + 关联 KO 数 |
| T216 | ProjectMgmtView | 4 项目卡片（活动/归档）+ KO 数 + 最后活动 |
| T217 | PromptsView | PRM 模板卡片 × 3（KO-PRM-0001/0002/0003）+ Section 数 |
| T218 | SnapshotsView | PRP 快照列表（版本号 + 时间 + 装配数）|
| T219 | NotFoundView | 404 页面 + 链接回首页 |

### F-53.3 长期 — 完善任务规划约束

1. **TP-2 task pack 后续 task 必含视觉验收**：
   - done_signal 加："页面符合 V3 原型视觉（端口蓝 + 深色侧栏 + stat-card 等）"
   - review_focus 加："对比 V3 原型 line XXX-line YYY 区块"
2. **DESIGN.md 引用强化**：所有 frontend task 显式 import + 应用 V3 CSS 变量
3. **PR review checklist 加视觉对比项**：每 view 必须有"与 V3 原型对比截图"附件

## Prevention（预防措施）

### 立即做

1. **TP-2 task pack v2 更新**：T212+ task 在 done_signal 加视觉验收
2. **`docs/contracts/frontend-standards.md`** 新文件：定义 frontend task 必含 V3 视觉对比验证
3. **`scripts/check-v3-style.sh`**：自动检测 view 是否引用 V3 CSS 变量类（如 `.page-header` / `.stat-card`）

### 长期做

1. **TP-3+ task pack 模板**：done_signal 必含"前端视觉 + 后端功能"双验收
2. **PR review template**：每 frontend PR 必含 V3 视觉对比截图
3. **V3 → Vue 组件迁移脚本**：自动化把 V3 关键 class 转为 Vue SFC（`<style scoped>`）

## Related Issues（关联问题）

- **F-52** Sidebar.vue ref 导入遗漏（项目预览时发现，本次已修）
- **TP-2 完成 commit e7bc5a1**：11 个 task 全部 done_signal 满足，但都未涉及视觉验收

## Quick Win（一行命令自查）

```bash
# 检查 view 是否应用 V3 CSS 变量
grep -lrE "var\(--(port-blue|bg-rail|bg-paper|stat-card|page-header)\)" frontend/src/views/
# 期望输出：v3-styled-views 列表
# 当前输出：（空）— 0 个 view 应用 V3 变量
```

## Verification（验证）

修复 F-53.1 + F-53.2 后：
- [ ] `grep -rE "var\(--port-blue\)" frontend/src/` 输出 ≥ 1
- [ ] `grep -rE "class=\"page-header\"" frontend/src/views/` 输出 ≥ 5（每个 KO 类型 list page）
- [ ] 浏览器访问 `/ko-library` 显示 V3 风格深色侧栏 + 6 类型入口卡片
- [ ] 浏览器访问 `/permissions` 显示 V3 风格权限矩阵（13 菜单 × 5 操作）
- [ ] 8 个占位 view 被真实业务逻辑替换

## Captured Lessons（沉淀教训）

> **任务规划阶段的 done_signal 必须覆盖完整验收维度**。Sprint 1 + Sprint 2 实施时只关注"功能跑通"（mvn verify + vite build），但**视觉是产品体验的最终形态**，不是"功能跑通"就算完成。V3 原型 + DESIGN.md 已存在，但 task pack 没把"视觉符合"作为约束 → 实施时各 view 自由发挥，累积成"大不一样"。

> **F-53 是 Sprint 1 + Sprint 2 范围性失误**。修复成本：
> - 短期：1-2 天（应用主题色 + Sidebar/TopBar/KO 库视觉）
> - 中期：1 周（10 个占位 view + 6 个已实施 view 视觉还原）
> - 长期：流程改进（task pack 模板 + 视觉验收 checklist）
