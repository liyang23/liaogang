---
title: "feat: §3 手动子项弹层 + ManualSubItems 类型迁移（Q-I4 §3 sparring→实施）"
type: feat
status: active
date: 2026-07-17
spec_id: 2026-07-16-001-q-i4-section3-manual-subitem-modal
origin: docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md
origin_grade: brainstorm
origin_verification_status: not-applicable
origin_verification_reason_codes: []
deepened: 2026-07-17
implements_schemas: []
---

# §3 手动子项弹层 + ManualSubItems 类型迁移（Sparring → 实施）

## Summary

为 PRD v0.32 §10.5.1 KO-PRM-0001 §3「计算范围」DYNAMIC 手动子项模式做 §3 弹层落地 + ManualSubItems 类型迁移（`string` → 结构化数组）+ 跨段 `{{var}}` 运行时解析链路 + 校验重复检测。5 个 Implementation Units 按 `Sparring → 前端弹层 → 类型迁移（4 端）→ 跨段变量索引 → 校验重复` 顺序，先用 U1 召集业务专家 sparring 议程锁定 15 项产品决策，再用 U2-U5 实施批次。

## Decision Brief

- **Recommended approach:** 把 §3 弹层拆为 4 组合（容器形态 R0a × 内部布局 R1b 4 路径），通过 ManualSubItemModal.vue / ManualSubItemDrawer.vue 两个 Vue 组件落地；4 端（前端 SectionCard.vue + ComposerView.vue + 后端 ComposerController.parseContext + ComposerRenderService.mergeSectionContext）同步升级 ManualSubItems 类型；R15b 跨段 `{{var}}` 链路贯通 R7/R7a 三锁；R5 阶段 2 不留 plan 暗门。
- **Key decisions:**
  - U1 (Sparring) 15 项决策表必须先有产出再实施，否则 U2-U5 的关键设计（容器形态、字段 schema、软硬上下限确切数字）无依据。
  - U3 类型迁移必须四端同步，单端推进会让 R5 阶段 2 失去 OQ-16 重新推导输入。
  - U4 跨段 `{{var}}` 链路依赖 SectionCard.vue DYNAMIC 分支已升级的 ManualSubItems 数组结构（U3）+ U2 弹层视觉一致性基础。
- **Validation focus:** U2 V3 视觉对比通过 `bash scripts/check-v3-style.sh`；U3 演示值 38 重放测试；U4 §7 解除后 §3 引用断引用视觉信号；U5 R11 AE5 重复检测弹窗二选一覆盖。
- **Largest risks / boundaries:** SectionCard.vue 当前 textarea 整体替换可能破现有手动子项流（迁移脚本兜底）；后端 `String.replace` 改为逐条拼接可能引入 NPE（integration test 覆盖）；U3 如果 sparring 拍叠加 5 业务字段 schema，类型迁移工作量爆炸，R5 阶段 2 必答「schema 复杂度先评估」。

---

## Problem Frame

PRD v0.32 §10.5.1 §3「计算范围」是唯一一段需要业务专家一次性录入手动子项的 Section（KO-PRM-0001 §3 = 38 条 manualSubItems，全部由业务专家录入，selectedKOs / varBindings / SCHs 三列均为 0）。

当前 Sprint 2 T210 commit `651c334` 落地的 `SectionCard.vue` L58-66 DYNAMIC 分支仅为单 textarea + `v-model="manualSubItem"` + `<el-input type="textarea">` 占位实现，不支持列表式 / 表格式 / 抽屉式 Dialog 任一形态，且 `ManualSubItems` prop 仍是 `string`。这意味着：
- 业务专家面对 38 条 PRD §10.5.1 演示值时无批量录入工具；
- PRD §3.1.3 三栏「顶部 PRM 选择 + 中栏 Section 编排 + 右栏实时预览」联动在 §3 上无法承载；
- OQ-16 `manualSubItems.size` 在当前代码中按「已写入任何字符串的 section 数」计算（`composer.ts` L68 + `ComposerView.vue` L143），与 PRD §10.5.1 「实际装配数 38 是 38 条子项数」语义不一致。

Q-I4 在 Plan §Open Questions / Sprint 2 task pack / Sprint 3 TP-3 T310 risk_note 三处均被列为未解决，本 plan 是其 HOW 答案：先 sparring 锁定产品决策，再实施端到端方案。

---

## Requirements

- R1. §3 弹层支持 R0a × R1b = 4 组合路径（中央 Dialog / 右侧 push-out 抽屉 Drawer × 行内列表 / 表格列）全部可达，不出现 R1 旧措辞的容器 + 布局叠加歧义。
- R2. `SectionCard.vue` 当前 L58-66 单一 textarea 占位实现整体替换为弹层，原 textarea 字段永久下线；`ManualSubItems` prop 类型同步从 `string` 升级为结构化数组 `Array<{title, content, value?, unit?, lower_bound?, upper_bound?, range_type?}>`。
- R3. sparring 必答的 15 项产品决策（origin Requirements Success Criteria 第 1 条）全部拍板；R5 阶段 2 在 SectionCard.vue DYNAMIC 分支实施前完成，OQ-16 公式中 `manualSubItems.size` 在 §3 段的语义（= 子项数组长度）重新推导，确保 PRD §10.5.1 演示值 38 在新 schema 下仍可重放。
- R4. 前端组件库复用 VariableBindingModal / 权限矩阵 UI 已用 Element Plus `el-dialog` / `el-drawer` + 公用 design token；不引入新组件、不新增 design token。V3 视觉对比 + `bash scripts/check-v3-style.sh` 通过 + 响应式 1024 px + 768 px 双断点截图。
- R5. 后端 `ComposerController.parseContext` 反序列化逻辑 + `ComposerRenderService.mergeSectionContext` each-block 替换语义从整段 `String.replace` 改写为对 manualSubItem list 逐条拼接；保留对接 R5 阶段 2 业务字段（如启用）的能力，存量 string 字段向后兼容 1 个 sprint。
- R6. 跨段 `{{var}}` 链路：§3 嵌入 `{{var}}` 与 §4-§7 varBindings 字典打通；§7 PAR 解除后 §3 已写入引用断引用视觉信号（灰显 OR 黄色提示由 R7a 拍板）；子项计数与是否断引用无关。
- R7. dirty 检测：每行失焦即存；关闭弹层前 dirty 三按钮互斥（Save Draft & Close / Discard & Close / Cancel）；脏数据 dirty 锚点 = 弹层层「上次入栈的子项集合」（由 sparring R4b 拍板）。
- R8. a11y baseline：弹层 ≥1024 px 桌面（中央 Dialog）/ ≥1440 px（抽屉，与 frontend-standards xl 断点对齐）；全键盘可达（Tab / ESC / Enter）；Toast 用 `aria-live=polite`；必填字段 `aria-required=true` + `aria-describedby` 关联错误信息。

**Origin actors:** A1 业务专家（sparring 必答方）/ A2 知识库管理员（前端用户）/ A4 模板作者（不直接修改 §3）。
**Origin flows:** F1 §3 手动子项录入流程（含 F1 Step 3.5 批量粘贴仅 R3b 启用时）。
**Origin acceptance examples:** AE1 / AE1b / AE2 / AE3 / AE4 / AE5 / AE6 / AE7 / AE-empty / AE-dialog-grid（10 条 AE 覆盖 4 组合 + 边界 + 错误 + 跨段）。

---

## Assumptions

- A1. 业务专家可在 Sprint 2/Sprint 3 之间安排 30-60 分钟 sparring 议程一次拍板 15 项决策；如议程时间不足，R0a + R1a + R1b 必拍，其余按 §3 业务域相关性分组 2 次跟进。
- A2. Sprint 2 T210 commit `651c334` 的 SectionCard.vue 当前实现状态保持不变到 U2 实施日，Sprint 2 期间无文档外的隐式修改。
- A3. frontend-standards `§7.1-7.3` 双断点已锁 lg=1024 / xl=1440（待 swe 验证）；若标准后续修订，本 plan 抽屉式阈值需连锁修订。
- A4. `OQ-16` 公式中 `manualSubItems.size` 在 R5 阶段 2 重新推导后，从「section 数」改为「子项数组长度」； PRD §10.5.1 演示值 38 在新公式下重放（38 条子项每条 1 装配 = 38 装配数）。
- A5. V3 视觉对比 + `check-v3-style.sh` 在 U2 完成后通过；如未通过，本 plan 默认走 F-53.3 规定的迭代节奏补 1 个 sprint 修复。

---

## Scope Boundaries

- 关闭弹层 `dirty` 检测的 Toast 兜底与 Toast 位置、最大并发数由 R13 + R14 拍板决定（本 plan 实施阶段严格按拍板，不在 plan 阶段二次创意）。
- 国际化文案 + a11y 深度细节（焦点陷阱 aria-describedby 完整字段树 + 屏幕阅读器 live 区域策略）由 i18n 工作 + F-53 后续专项收口，不在 U2-U5 实施范围。
- 38 条子项列表式一次渲染的浏览器性能（DOM 节点数 / 滚动卡顿）由 frontend-standards R9 软上限 ≥ 软上限强制启用虚拟滚动 + pageSize=50 收敛；性能压测由 plan 阶段 U3/V3 视觉对比 + e2e 覆盖。
- 后端落库失败兜底「操作成功但同步失败，请刷新」具体字面文案由 U5 阶段 R17a 拍板，决定样式前覆盖。
- R15b 命名空间归属决策「§3 是局部约定，不反向约束 §4-§7」由 origin Key Decisions 第 1 条锁定，实施阶段不再重启 §4-§7 命名空间 brainstorm。

### Deferred to Follow-Up Work

- TP-3 T310 risk_note 措辞更新：由其 owner 在 Sprint 2 sparring 闭环后单独更新（Sprint 3 启动前）。
- PRD §3.1.3 三栏（顶部 + 中栏 + 右栏实时预览）在 §4-§9 DYNAMIC KO 模式弹层的共享对齐：origin 显式排除本 Q-I4 scope（Sprint 2 / 3 不打开）。
- 国际化文案 + a11y 深度细节（焦点陷阱 / live region / 屏幕阅读器完整树）：i18n 工作 + F-53 后续专项。

---

## Direct Evidence Readiness

- target_repo: `.`（主仓库，path: `/Users/liyang129/data/liaogang`）
- evidence_sources: Phase 1 spec-doc-review Round 1 + Round 2 共 52 项 fix 落地结果；上游 requirements doc 35 项 P0+P1 节归属 / 类型迁移 / 跨段变量索引 / 状态机 / 持久化语义决议；Sprint 2 41/41 backend 测试 + 33/33 vitest 累计；Sprint 3 启动前 scenario 全锁。
- source_refs:
  - `frontend/src/components/SectionCard.vue`（Sprint 2 T210 commit `651c334`，146 行，L58-66 textarea 占位 + L88 prop + L95 emit 类型）
  - `frontend/src/components/VariableBindingModal.vue`（Sprint 2 T210 已实现 4 按钮 ⚡/⊕/✎/× OQ-16）
  - `frontend/src/views/prompts/ComposerView.vue`（OQ-16 实现 + L143 manualSubItems.size 当前按 section 计）
  - `frontend/src/api/composer.ts`（L43-46 ManualSubItems = { [sectionIndex: number]: string }）
  - `backend/src/main/java/com/liaogang/famou/km/prompt/controller/ComposerController.java`（L70-76 String.valueOf(e.getValue())）
  - `backend/src/main/java/com/liaogang/famou/km/prompt/service/ComposerRenderService.java`（L102-109 content.replace each-block）
  - `docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md`（origin requirements doc，35 项 P0+P1 fix 落地）
- current_revision: `9980417` (Merge feat/sprint-2-ko-and-permissions: Sprint 1+2 + F-53 + F-54)
- worktree_status: 主仓干净（除 .worktrees/ 与两个本地文件 docx / md 之外无未提交修改）
- confidence: high（origin doc 已通过 2 轮 spec-doc-review + 35 项 P0/P1 决议 + sparring 议程前置资料已就绪；本 plan 的 spec-doc-review 将在 plan-handoff 阶段跑）
- limitations: sparring 议程实际召开时间未确定（A1 假设），如议程延期，U2-U5 实施批次顺延。

---

## Direct Evidence

- repo_scope: 主仓 `docs/`, `backend/`, `frontend/` + Sprint 2/3 worktrees (`.worktrees/sprint-2`, `.worktrees/sprint-3`)
- source_reads_completed: 52 项 fix 落地后的 origin requirements doc / SectionCard.vue / VariableBindingModal.vue / ComposerView.vue / composer.ts / ComposerController.java / ComposerRenderService.java
- source_reads_required: 计划新增 ManualSubItemModal.vue / ManualSubItemDrawer.vue / manual-sub-item-parser.ts / ManualSubItem.java 落地后实际代码行 / frontend-standards.md §7.1-7.3 实际断点表
- commands_or_tools_used: `git log --oneline`, `git show 651c334 --stat`, `rg "manualSubItem|ManualSubItems"`, `grep -n "manual\|手动"` SectionCard.vue, `find .worktrees -name "SectionCard.vue"`, `wc -l SectionCard.vue`
- impact_on_plan: U2 替换 SectionCard.vue 整体；U3 跨前后端 4 端类型迁移是本 plan 最大的实施面；U4 跨段 `{{var}}` 链路对 R15b 决议强依赖。
- key_findings:
  - 当前 SectionCard.vue L58-66 单一 textarea 占位；
  - 后端 ComposerRenderService L102-109 直接 `content.replace` 整段 `{{#each items}}...{{/each}}` 块；
  - OQ-16 `manualSubItems.size` 当前按 section 计（38 条子项 → size=1），与 PRD §10.5.1 演示值 38 不匹配 → 必须在 R5 阶段 2 重新推导；
  - VariableBindingModal.vue L131-138 `onUnbind` emit `update:bindings` 已为 §3 跨段响应式提供通路；
  - frontend-standards §7.1-7.3 CSS 变量已锁；具体断点表需在 U2 实施前 swe 验证 lg=1024 / xl=1440。
- limitations: backend manualSubItem schema 的 DTO 文件路径未在本轮 Read 中直接定位（搜索 `ManualSubItem.java`），按用户给定 backend path 推断存在，需在 U3 实施时 confirm 文件确切位置。

---

## Context & Research

### Relevant Code and Patterns

- `frontend/src/components/SectionCard.vue` DYNAMIC 分支当前实现（textarea 占位，`composer.ts` ManualSubItems 是 string map）。
- `frontend/src/components/VariableBindingModal.vue` 已实现的 4 按钮模式（⚡/⊕/✎/×）作为 U5 校验重复弹窗的参考结构。
- `frontend/src/views/prompts/ComposerView.vue` 顶部 PRM 选择 + 中栏 Section 编排 + 右栏实时预览的三栏布局（PRD §3.1.3 已锁），U2 弹层与三栏联动一致性 R12a 已拍板。
- `backend/src/main/java/com/liaogang/famou/km/prompt/controller/ComposerController.java` + `ComposerRenderService.java` 4 端（前端 2 + 后端 2）类型迁移工作量清单见 origin doc Outstanding Questions Deferred to Planning 第 4 项。
- `frontend-standards.md §7.1-7.3` 双断点 + design token 已锁；R12a 视觉一致性继承声明引用此标准。

### Institutional Learnings

- `docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`：Sprint 1 教训，U2-U5 已自动防护（Sprint 2 期间已部署 P1 三层编译门禁 pre-commit / pre-push / GitHub Actions）。
- Sprint 2 + F-53 + F-54 已合并到 `9980417`（含 PR-31 `edb5e66` TP-3+ 任务 pack 模板）；本 plan 是其下游产物。

### External References

- `frontend-standards.md`（仓内）：design token 名、CSS 变量、响应式断点 — U2 / U4 引用。
- PRD §10.5.1 + §10.5.3（origin doc 引用）：演示值 38 + 9 类 Markdown 元素 + Handlebars 子集 — U3 / U4 引用。

---

## Key Technical Decisions

- **规划顺序：U1 sparring-first，U2-U5 串行 + U4 与 U3 联动**: Sparring 未完成时 U2-U5 的关键设计（容器形态 / 字段 schema / 软硬上下限确切数字 / 命名空间归属）无依据，故 U1 必须先产出 sparring-decisions doc；U2 实施组件时 U3 类型升级未到位需要过渡层（U2 期间 toMultiString 兼容），U3 完成后再去掉过渡层。
- **4 端类型迁移同步进行**: 前端 `SectionCard.vue` / `ComposerView.vue` + 后端 `ComposerController.parseContext` / `ComposerRenderService.mergeSectionContext` 不接受单端推进；保留 1 个 sprint 向后兼容期。
- **`composer.ts` ManualSubItems 类型从 `Map<Integer, string>` 升级为 `Map<Integer, List<ManualSubItem>>`**: 不接受按 section 颗粒度保留 string，否则 OQ-16 size 语义无法统一到子项数组长度；string 兼容 1 sprint 后移除。
- **R5 阶段 2 必经**: 推导出 OQ-16 `manualSubItems.size = 子项数组长度`，演示值 38 重放测试通过；不留 §3 上 manualSubItems 语义争议。
- **抽屉式容器 ≥1440 px**: 不与 frontend-standards §7.1-7.3 双向突破，md-lg 回退中央 Dialog 避免悬挂态。
- **跨段 `{{var}}` 在 ManualSubItemModal 上 watch varBindings prop**: 不在 SectionCard.vue 上重复 watch，避免 props 嵌套循环。
- **R11 重复检测时机 onBlur + 去抖 300ms**: 不在 onChange，onSubmit 集中检测；批量粘贴触发 = 全部粘贴完成提交时逐条（避免逐 keyup 检测干扰)。
- **存量 string → array 双 schema 兼容 1 sprint**: 前后端均支持双 schema；Phase 1 feature flag `manual_sub_items.array_schema` 默认 0%，Phase 6 升至 100%；Phase 6 移除 gating = 客户端流量 ≥ 99% 新 schema + 后端 dual-read 反序列化错误率 < 0.1%/天。
- **V9xxx migration 是 conditional 而非必做**: 仅在 prm_section.content TEXT 不够承载（如需独立列 / 新表 / 列类型变更）时才落 V9xxx；否则跳过迁移节省 U3 工作量（U3 启动前 sweep 确认 schema 方案后再落地）。
- **术语约定（plan-wide）**：
  - API/Type 名 = `ManualSubItems`（复数，`Map<Integer, List<ManualSubItem>>`）
  - 数组元素类型 = `ManualSubItem`（单数，含 title / content / value? / unit? / lower_bound? / upper_bound? / range_type?）
  - Vue 组件 = `ManualSubItemModal` / `ManualSubItemDrawer`（Modal / Drawer 后缀）
  - Props = `manualSubItems`（前端小写 camelCase） / VariableBindingModal 使用 `bindings`（不带 var 前缀，emit 名 `update:bindings`）
- **跨段 `{{var}}` 在 §3 子树内部 watch（不修改共享 markdown-renderer.ts）**: 不在 SectionCard.vue 上重复 watch（避免 props 嵌套循环）；ManualSubItemModal 内置 renderer 函数。R15b 拍板翻转（全局命名空间）走 Plan B fallback 路径。

---

## Open Questions

### Resolved During Planning

- **Q-I4 决议入口**：开放为 spar-first，由 U1 业务专家 sparring 议程一次性拍板 15 项；R15b 命名空间归属决议推迟到 U4 实施前（与 §4-§7 跨段变量命名空间不冲突即可）。
- **后端类型迁移兼容期时长**：确认 1 个 sprint 兼容期 + 一次性 string → array 迁移脚本（U3 完成时打掉 string fallback）。

### Deferred to Implementation

- **空字符串 / 全空白子项视作合法还是非法**: U2 实施时由 Element Plus `el-form` rules 默认 trim 后非空判定；如有歧义由 R10 校验规则 sparring 决议覆盖。
- **存量 string 字段何时一次性 migration**: 在 U3 完成时一次性迁移（不在 migration 期间运行 = 部署期间脚本），具体迁移工具由 plan 阶段 finalize。
- **大数字 500+ 行实测分页策略（不限于 50）**: R9 软上限触发分页，但 pageSize=50 是 R11 阶段 sparring 推荐值；U2 实施时若 50 行高 reflow 卡顿可调。
- **R17a 后端 5xx 兜底实现依赖后端接口**：U5 实施期间假设后端 POST /api/composer/manual-subitems 返回 5xx 后前端能 fallback；如后端 API 尚未存在则 U5 不能完成。

---

## High-Level Technical Design

> *This illustrates the intended approach and is directional guidance for review, not implementation specification. The implementing agent should treat it as context, not code to reproduce.*

```
┌────────────────────────────────────────────────────────────────────┐
│              U2 前端弹层（SectionCard.vue DYNAMIC 替换）               │
├────────────────────────────────────────────────────────────────────┤
│ SectionCard.vue                                                      │
│   ├─ <textarea manualSubItem>  ↓  REPLACED  by  <ManualSubItemModal v-if="editingManualSubItem"/>│
│   ├─ Modal variant (R0a=中央 Dialog):                         │
│   │    ManualSubItemModal.vue  ←─────┐                                │
│   ├─ Drawer variant (R0a=抽屉, ≥1440): │                                │
│   │    ManualSubItemDrawer.vue ←─────┤                                │
│   └─ emit 'update:manualSubItems': Array<ManualSubItem>           │
│                            ↓                                          │
├────────────────────────────────────────────────────────────────────┤
│              U3 类型迁移（4 端同步）                                  │
├────────────────────────────────────────────────────────────────────┤
│ Frontend:                                                              │
│   composer.ts: ManualSubItems = { [sectionIndex]: List<ManualSubItem> }│
│   SectionCard.vue prop + emit 同步升级                                │
│   ComposerView.vue renderedHtml 数据形态契约同步                       │
│                            ↓                                          │
│ Backend:                                                               │
│   ComposerController.parseContext: 反序列化改为 List<ManualSubItem>   │
│   ComposerRenderService.mergeSectionContext: each-block 替换语义     │
│      从 String.replace 整段 改为 按 manualSubItem list 逐条拼接     │
│      含 R5 阶段 2 业务字段（如启用）+ R7 {{var}} 解析                  │
│                            ↓                                          │
├────────────────────────────────────────────────────────────────────┤
│              U4 跨段 {{var}} 运行时解析链路                          │
├────────────────────────────────────────────────────────────────────┤
│ ManualSubItemModal watch varBindings prop                              │
│   ├─ 输入 {{var}} → 高亮色与 §4-§7 DYNAMIC KO 复用 / 差异化（R7a）     │
│   ├─ §7 PAR 解除 → §3 引用断引用视觉信号（灰显 OR 黄色提示）         │
│   └─ 子项计数与是否断引用无关（R12 联动）                              │
│ VariableBindingModal.vue onUnbind → 触发 varBindings prop 变化         │
│ markdown-renderer.ts {{var}} 高亮层按 R7a 决议复用/差异化              │
│                            ↓                                          │
├────────────────────────────────────────────────────────────────────┤
│              U5 校验重复检测 + 批量粘贴（仅 R3b 启用）                 │
├────────────────────────────────────────────────────────────────────┤
│ Element Plus el-form + async-validator                                 │
│   ├─ R10 数值字段 [0, 1000] 上下界（如启用 value 字段）                │
│   ├─ R11 触发时机 = onBlur + 去抖 300ms（单条）                  │
│   │         全部粘贴完成提交时逐条检测（批量）                       │
│   └─ R11 重复检测策略由 spar 拍板（3 选项：title / title+content / 不）│
│ BatchPastedParser (manual-sub-item-parser.ts, 仅 R3b 启用时引入)    │
│ dirty 检测：每行失焦即存 + 关弹层前 dirty 三按钮互斥                │
│ 后端 5xx 兜底：R17a 「操作成功但同步失败，请刷新」                    │
└────────────────────────────────────────────────────────────────────┘
```

**实现细节（directional guidance，非实现规格）**：
- 弹层组件用 Element Plus `el-dialog` / `el-drawer` + VariableBindingModal 同套样式 wrapper
- `Array<ManualSubItem>` prop 在 Frontend / Backend 两端用同一个 `manual-sub-item.ts` 定义（仓内 copies 一份 json-schema，前后端各 generate）
- R15b 命名空间归属用 `{section.sectionIndex}.{varName}` 作为 §3 局部约定（不反向约束 §4-§7）
- 后端 `mergeSectionContext` each-block 替换语义：`{{#each items}}...{{/each}}` 块体在每次 render 时按 `manualSubItems` 列表逐条拼接；每条拼接规则走 `handlebars` 渲染

---

## Implementation Units

<!-- Each unit carries a stable plan-local U-ID (U1, U2, …). U-IDs never renumbered. -->

### U1. §3 弹层设计协同（Sparring 议程输入准备 + 决议归档）

**Goal:** 召集业务专家 sparring 议程锁定 15 项产品决策（与 origin Success Criteria 第 1 条数字口径对齐），产出 sparring-decisions 归档 doc，作为 U2-U5 实施批次的输入。**Minimum viable decisions 子集**（R0a + R1a + R1b + R11c + R4b + R10 + R11 + R15b = 8 项）必须 60 分钟内一次性拍板；其余 7 项（R3b 批量粘贴 / R5 阶段 1 schema / R6 编辑器 / R7 + R7a `{{var}}` 嵌入与视觉一致性 / R9 软硬上限确切数字）若议程时间不足允许分 2 次跟进拍板，未拍板项用 feature flag 兜底。

**Requirements:** R3 (sparring 必答项全部拍板 + R5 阶段 2 必经 + 38 演示值重放 + minimum viable 8 项一次拍板)

**Dependencies:** 无（最先）；业务专家档期确认（Plan §Direct Evidence Readiness 验证为 U1 启动 gating）

**Files:**
- Create: `docs/sparring/2026-07-XX-001-q-i4-section3-sparring-decisions.md`（业务专家决议表）
- Modify: `docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md`（Outstanding Questions Resolve Before Planning 项从 `[User decision]` → `[Resolved YYYY-MM-DD]`）
- Modify: `docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md`（T210 后续 task pack 标记 Q-I4 已闭环）

**Approach:**
- 议程分两轮：Round 1 = minimum viable decisions 8 项（60 分钟内必拍）；Round 2 = 7 项跟进（可 deferred）
- Round 1 顺序：R0a + R1a + R1b（容器形态 + 内部布局 4 组合路径必备）+ R11c（7 状态机 trigger 表）+ R4b（dirty 检测关键 UI）+ R10 + R11 + R15b（U4 强依赖）
- Round 2（follow-up）：R3b + R5 阶段 1 schema + R6 + R7 + R7a + R9 软硬上限
- 业务专家对每项打分 / 异议 / 重议 / 共识 / 拒绝路径全部覆盖
- sparring-decisions doc 用表格 + 每项一页「决议 / 共识 / 异议 / 后续」结构
- R5 阶段 2 必答项（在 sparring 议程一并拍板）：OQ-16 manualSubItems.size = 子项数组长度 + 38 演示值重放策略

**Patterns to follow:**
- 现有 PRD v0.32 决议表（PRD §5.2 + §4.1）的格式

**Test scenarios:**
- Happy path: Round 1 业务专家 5 人全部参会，minimum viable 8 项决策 60 分钟内全部拍板；sparring-decisions doc 完成 + origin Outstanding Questions 全部 15 项标 `[Resolved YYYY-MM-DD]`
- Edge case: 业务专家仅 3 人到场，Round 1 6 项拍板 + 2 项 deferred；Round 2 时间表另排
- Error path: Round 1 R15b 拍板为「全局命名空间」（与 plan 高层设计 hardcode 的「局部约定 `{section.sectionIndex}.{varName}`」冲突）→ U4 watch varBindings 重做（详见 U4 Fallback 路径）
- Integration: sparring-decisions 与 Plan §Open Questions 同步关联；origin doc 与 sparring-decisions doc 互为索引

**Verification:**
- sparring-decisions doc 落地（>= 15 项拍板，Round 1 minimum viable 8 项 + Round 2 7 项）
- origin Outstanding Questions 全部 15 项标状态
- Q-I4 在 plan §Open Questions / Sprint 2 task pack / Sprint 3 TP-3 T310 三处引用条目同步标记为已闭环或删除（R15b 决议保留权归 sparring）

---

### U2. 前端弹层落地（SectionCard.vue DYNAMIC 分支 + ManualSubItemModal / Drawer）

**Goal:** 替换 SectionCard.vue L58-66 单一 textarea 占位；新增 ManualSubItemModal.vue / ManualSubItemDrawer.vue 两个 Vue 组件覆盖 R0a × R1b = 4 组合路径；内置 R11c 7 状态（含 high-row-scroll 虚拟滚动 + 软上限触发）+ R-onboarding + R12a 视觉一致性继承 + R-a11y-baseline + R13 按钮组处置跟随 R1 + R14 Toast 复用既有全局组件 + R4b dirty 检测。

**Requirements:** R1, R2, R4, R7, R8

**Dependencies:** U1（sparring 已拍 R0a / R1a / R1b / R11c 等关键项）

**Files:**
- Modify: `frontend/src/components/SectionCard.vue`（替换 L58-66 textarea；新增 ManualSubItemModal / Drawer 子组件 import；接收 U3 升级后的 `manualSubItems: Array<ManualSubItem>` prop；过渡期 toMultiString 兼容）
- Create: `frontend/src/components/ManualSubItemModal.vue`（中央 Dialog 容器；R1b 二选一行内列表 / 表格列；R11c 7 状态实现 + 高亮路由；R-onboarding empty 态）
- Create: `frontend/src/components/ManualSubItemDrawer.vue`（右侧 push-out 抽屉 Drawer；≥1440 px 适配）
- Modify: `frontend/src/views/prompts/ComposerView.vue`（响应式 watch 接 ManualSubItems 新 prop；renderedHtml 数据形态契约转发）
- Create: `frontend/src/utils/manual-sub-item-parser.ts`（仅 R3b 批量粘贴启用时引入；否则从 Files 列表中按条件依赖跳过）
- Create: `frontend/src/components/ManualSubItemModal.test.ts`（vitest 单元测试）
- Create: `frontend/src/components/ManualSubItemDrawer.test.ts`（vitest 单元测试）
- Modify: `frontend/src/test/composer.integration.test.ts`（端到端 SectionCard 弹层交互）
- Modify: `frontend/src/views/prompts/ComposerView.test.ts`（如存在）

**Approach:**
- ManualSubItemModal.vue / Drawer.vue 用 Element Plus `el-dialog` / `el-drawer` + VariableBindingModal 共享 design token（R12a）
- R1b 二选一：行内列表（`el-form-item` × N）+ 表格列（`el-table` 列）分别实现为 ManualSubItemModal 的内部 mode prop
- R11c 7 状态（empty / loading / error / validation-fail / paste-fail / high-row-scroll / partial-import）：每态 enter-trigger / exit-trigger / 阻塞交互 / dirty 关系按 origin R11c-trigger-condition 实施矩阵（plan 不重复，链接 `docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md` 的 R11c + R11c-trigger-condition 段）。状态机合法转移：empty ↔ loading ↔ empty / validation-fail；empty → paste-fail / partial-import（R3b 启用时）；partial-import → paste-fail / validation-fail；high-row-scroll 是叠加态与 empty / validation-fail 可同时存在。R3b 启用时序排在 R11c 拍板之前。
- high-row-scroll：≥R9 软上限触发虚拟滚动（Element Plus `el-table-v2`）+ pageSize=50
- R13 按钮组跟随 R1：列表式保留 ⊕ + ✎；中央 Dialog 全部隐藏；抽屉式 ≥1440 px 保留 ⊕
- R14 Toast 复用项目既有全局 Toast 组件（如不存在则引入最小骨架）
- R4b dirty 三按钮互斥：Save Draft & Close / Discard & Close / Cancel
- R-onboarding 在 empty 态显示引导文案 + 「+ 新增第一条子项」CTA + 批量粘贴入口显眼可见
- a11y baseline：≥1024 px (中央 Dialog) / ≥1440 px (抽屉)；Tab / Enter / ESC 全键盘可达；aria-live=polite Toast；aria-required=true 必填；aria-describedby 关联错误信息
- 过渡期：SectionCard.vue 接收 `Array<ManualSubItem>` prop + toMultiString 内部兼容旧 string；U3 完成后去掉 toMultiString

**Patterns to follow:**
- VariableBindingModal.vue 4 按钮组（⚡/⊕/✎/×）作为 R13 按钮组处置参考
- ComposerView.vue 顶部 PRM 选择 + 中栏 Section 编排 + 右栏实时预览三栏布局（PRD §3.1.3 已锁）

**Test scenarios:**
- Happy path: §3 卡片标题旁点击「手动子项」打开 ManualSubItemModal，行内列表模式，第一条已有子项进入 inline 编辑态（光标定位 title），38 条 PRD §10.5.1 演示值全显
- Happy path: ≥1440 px 浏览器宽度下打开 ManualSubItemDrawer，行内列表模式，桌面宽屏下右栏实时预览保留
- Edge case: §3 首次唤起且无任何子项：进入 R-onboarding empty 态，显示引导文案 + 字段填写示例 + 「+ 新增第一条子项」CTA 自动聚焦
- Edge case: 录入 200 条手动子项，触发 R9 软上限 high-row-scroll 态；强制启用虚拟滚动 + pageSize=50
- Edge case: 录入 500 条手动子项，触发 R9 硬上限；前端拒绝新增 + 黄色提示
- Error path: 批量粘贴启用 R3b 后粘贴格式错位字符串 → R11c paste-fail 态显示兜底文案 + 「改为逐条录入」回退入口
- Error path: 后端 POST /api/composer/manual-subitems 返回 5xx → R17a Toast「操作成功但同步失败，请刷新」显示
- Integration: §3 卡片子项计数增删改 → ComposerView 顶部 PRM 选择栏装配进度小计实时刷新
- Integration: §4-§7 DYNAMIC KO 模式已选 PAR KO-PAR-0042 时，§3 第 3 条手动子项 content 嵌入 `{{min_inventory}}` → 右栏实时预览高亮
- Integration: §7 PAR 解除时 §3 已写入引用断引用视觉信号（灰显 OR 黄色提示由 sparring R7a 拍板）；子项计数保持不变

**Verification:**
- `bash scripts/check-v3-style.sh` 通过
- V3 视觉对比 + 响应式 1024 px + 768 px 双断点截图（PRD §3.1.3 一致性）
- F-54 verify handlebars evaluateCondition 通过
- mvn verify 不被破坏（Sprint 2 41/41 backend + 33/33 vitest 累计）
- AE1 / AE1b / AE2 / AE3 / AE4 / AE5 / AE6 / AE7 / AE-empty / AE-dialog-grid 共 10 条 AE 全部覆盖
- R11c 7 状态 enter-trigger / exit-trigger / 阻塞交互 / dirty 关系矩阵全部命中（空态 / loading 态 / 错误态 / 校验失败态 / 粘贴失败态 / 高行滚动态 / 部分导入态）
- toMultiString 过渡层拆除 PR 跟踪点记录在 U3 Phase 4 → 5 衔接
- aria-describedby + aria-live=polite 完整 a11y 树 deferred 到 F-53 后续专项（与 Scope Boundaries 一致）

---

### U3. ManualSubItems prop 类型迁移（前端 2 端 + 后端 2 端同步）

**Goal:** `ManualSubItems` 从 `Map<Integer, string>` 升级为 `Map<Integer, List<ManualSubItem>>`（含 `title` / `content` / `value?` / `unit?` / `lower_bound?` / `upper_bound?` / `range_type?`）。后端 `ComposerController.parseContext` 反序列化逻辑改写 + `ComposerRenderService.mergeSectionContext` each-block 替换语义从整段 `String.replace` 改写为对 manualSubItem list 逐条拼接。完成 R5 阶段 2 OQ-16 公式 `manualSubItems.size` 在 §3 段重新推导（= 子项数组长度）+ 38 演示值重放测试通过。

**Requirements:** R2, R3 (R5 阶段 2 必经), R5

**Dependencies:** U2（前端弹层落地后类型升级才有前向兼容基础）；U1（R5 阶段 1 schema 拍板后才进入 U3）

**Files:**
- Modify: `frontend/src/api/composer.ts`（L43-46 ManualSubItems 类型升级；新增 ManualSubItem interface / type；与 schema 仓内共享）
- Modify: `frontend/src/views/prompts/ComposerView.vue`（L143-156 manualSubItems.size 实现改为数组 length）
- Modify: `frontend/src/views/prompts/ComposerView.vue`（renderedHtml 数据形态契约同步升级；context.manualSubItems 从 string → array）
- Modify: `backend/src/main/java/com/liaogang/famou/km/prompt/dto/ManualSubItem.java`（**Confirm path at U3 启动时**：仓内实际可能是 logic 类 ManualSubItemParser / ManualSubItemBuilder 而非 DTO；U3 启动前 swe grep `ManualSubItem` + 看包结构；如 DTO 不在 `dto/` 目录则调整 Files 路径。含 title/content/value?/unit?/lower_bound?/upper_bound?/range_type? 字段）
- Modify: `backend/src/main/java/com/liaogang/famou/km/prompt/controller/ComposerController.java`（L70-76 String.valueOf 改为 List<ManualSubItem> 反序列化 + **dual-write 期间双 schema 反序列化 fallback 路径**：先识别字段类型 = string → 走旧路径；= List<ManualSubItem> → 走新路径；混用 → Spring Boot 反序列化错误日志 + 走 safer 路径（旧 string）避免静默数据丢失）
- Modify: `backend/src/main/java/com/liaogang/famou/km/prompt/service/ComposerRenderService.java`（L102-109 content.replace each-block 改写为逐条拼接 + R5 阶段 2 业务字段联动 + R7 `{{var}}` 解析）
- **Conditional:** V9xxx migration 仅在 §3 需要新 DB 列 / 新表 / 列类型变更时新增。如仅依赖 prm_section.content 字段（已存在 TEXT）通过 JSON 序列化承载 ManualSubItems array，**V9xxx migration 不需要落地**。U3 启动时 swe 确认 prm_section.content 是否够承载后再决定 migration 是否新增。如新增 V9xxx，需含：(a) ADD COLUMN manualSubItems JSON NULL DEFAULT NULL; (b) backfill 旧 string → JSON list 一次性 SQL（如 `JSON_ARRAY(...)` or 后端 migration runner）；(c) `V9002 V9003 V9004 schema migration 幂等`（已存在）；(d) feature flag 控制 array schema 启用比例（部署期 1 sprint 双 schema 并存）。**当前 plan 把 V9xxx 列为 Conditional，U3 启动前的 gating verify 是 schema 决策而非工作量决策。**
- Create: `backend/src/main/resources/db/migration/V9xxx__add_manual_sub_item_schema.sql`（仅在 (a) 真的需要新增列时；否则跳过）
- Create: `backend/src/test/java/com/liaogang/famou/km/prompt/ManualSubItemsMigrationTest.java`（4 端工作量清单 + 存量数据迁移脚本）
- Modify: `backend/src/test/java/com/liaogang/famou/km/prompt/ComposerRenderTest.java`（OQ-16 重新推导 + 38 演示值重放）
- Modify: `frontend/src/api/composer.test.ts`（手动子项数组类型契约）
- Modify: `backend/src/test/java/com/liaogang/famou/km/prompt/PromptControllerIntegrationTest.java`（如存在）

**Approach:**
- **Phase 1**: 后端 DTO `ManualSubItem.java` 增加新 schema；旧 string 字段标记 deprecated 但保留 1 sprint 兼容（**dual-write 路径**：前后端均支持双 schema；feature flag `manual_sub_items.array_schema` 控制 array schema 启用比例；Phase 1 默认 0%，Phase 6 升至 100%）
- **Phase 2**: 解析逻辑按 record list 接收；存量 string 字段通过 dual-read 反序列化 fallback（旧 string → 走 Array fallback 转换器，拆 `\n` + `{{var}}` 转为单元素 Array；新 array → 直读）；混用 → Spring Boot 反序列化错误日志 + 走 safer 路径（旧 string）避免静默数据丢失
- **Phase 3**: `mergeSectionContext` each-block 替换语义：从 `content.replace` 整段改为对 manualSubItem list 逐条拼接；每条拼接规则走 handlebars 渲染 + R5 阶段 2 业务字段（如启用）+ R7 `{{var}}` 解析
- **Phase 4**: 前端 `composer.ts` ManualSubItems 类型切换；emit 类型升级；**toMultiString 过渡层拆除 PR 跟踪点**：依赖 Phase 5 的 38 演示值重放测试通过后才允许发版；toMultiString 在 U2 期间位于 `frontend/src/components/SectionCard.vue` 内部 setup() computed 转换（不是新工具类，便于 Phase 4 一次性删完）
- **Phase 5**: ComposerView.vue OQ-16 公式 `manualSubItems.size` 重新推导为子项数组长度；38 演示值重放测试通过（Phase 4 必须在此之前完成）
- **Phase 6**: 移除 string 兼容路径（**前置 gating**：① 客户端流量扫描显示 7 天内新 array schema 写入占比 ≥ 99%；② 后端 dual-read 反序列化错误率 < 0.1%/天；③ 无 active client 仍写旧 string 字段）；标记 deprecated 字段 1 sprint 后清理

**Patterns to follow:**
- ComposerController.java L70-76 当前反序列化模式 → 升级为反序列化 ManualSubItem list
- ComposerRenderService.java L102-109 当前 replace 模式 → 升级为逐条拼接 handlebars 渲染
- VariableBindingModal.vue onUnbind emit `update:bindings` 模式 → SectionCard.vue emit `update:manualSubItems` 同构

**Test scenarios:**
- Happy path: 38 条 manualSubItems 渲染后 OQ-16 `manualSubItems.size` = 38，演示值重放通过
- Happy path: §3 单段子项 `{title:"料场库存上下限", content:"当前 X 吨"}` 渲染正常
- Edge case: 业务字段叠加 `{title, content, value: 850, unit: "t", lower_bound: 0, upper_bound: 1000, range_type: "single"}` 渲染正常（如 R5 拍板启用业务字段）
- Edge case: §3 第 N 条 content 嵌入 `{{min_inventory}}` 且 §7 已选 KO-PAR-0042 → 渲染期 varBindings 字典命中并替换（如 R7 拍允许嵌入）
- Error path: 存量 string 字段遇 array schema 时一次性迁移脚本执行；后端落库返回 5xx 时前端 R17a 兜底显示
- Integration: ComposerController → ComposerRenderService → 前端 ComposerView 三端 handbook 流程；现存 §4-§7 DYNAMIC KO 模式不受影响
- Integration: V9002 V9003 V9004 schema migration 幂等；新建 V9xxx migration 不破坏 Sprint 2 已沉淀数据

**Verification:**
- mvn verify 通过
- 38 演示值重放测试（pt 回归 + 单元测试）
- 每条手动子项可独立编辑不破后端渲染
- 跨段 varBindings 字典命中正确（与 U4 联动）
- V9002 V9003 V9004 + 新 V9xxx migration 幂等执行

---

### U4. 跨段变量索引运行时解析链路（R7a + R15a + R15b）

**Goal:** §3 嵌入 `{{var}}` 与 §4-§7 varBindings 字典打通；§7 PAR 解除后 §3 已写入引用断引用视觉信号（灰显 OR 黄色提示由 R7a sparring 拍板）；R15b 命名空间归属（= `{section.sectionIndex}.{varName}` 局部约定） + §4-§7 varBindings 在 §3 渲染阶段的可见性 + §7 解除兜底策略。

**Requirements:** R6

**Dependencies:** U1 (R7 / R7a / R15b sparring 决议) + U2 (ManualSubItemModal.vue / ManualSubItemDrawer.vue 已创建 — U4.Modify 这两个组件依赖其存在) + U3 (ManualSubItems 数组结构后 U4 watch varBindings 才能贯通 `{{var}}` 路由)

**Files:**
- Modify: `frontend/src/components/SectionCard.vue`（**不修改 markdown-renderer.ts 共享组件**，改为 §3 子树内部 watch varBindings；通过 LocalEventBus / Pinia store / props drilling 三选一向 ManualSubItemModal 传递 —— 默认 props drilling 避免引入新 store 依赖；如 ManualSubItemModal 嵌套深度超 2 层则改用 provide/inject）
- Modify: `frontend/src/components/ManualSubItemModal.vue`（内部 {{var}} 高亮 + 断引用色由 sparring R7a 决议：复用 §4-§7 / 差异化 / 局部约定；自包含 renderer 函数 — 不修改共享 markdown-renderer.ts）
- Modify: `frontend/src/components/ManualSubItemDrawer.vue`（同 ManualSubItemModal）
- Modify: `frontend/src/views/prompts/ComposerView.vue`（varBindings 字典单源传递；在 SectionCard 与 ManualSubItemModal 之间）
- Create: `frontend/src/utils/markdown-renderer.test.ts`（新增断引用视觉信号用例）
- Modify: `frontend/src/utils/markdown-renderer.test.ts`（如已存在）

**Approach:**
- 在 ManualSubItemModal.vue 上 watch varBindings prop；变化时重算每个手动子项的 `{{var}}` 路由
- markdown-renderer.ts 高亮层按 sparring R7a 决议复用 / 差异化：
  - 复用：直接用 §4-§7 DYNAMIC KO 高亮色 token
  - 差异化：定义新 CSS 变量（如 `--md-var-broken-ref-color`）
  - 局部约定：R15b 命名空间 = `{section.sectionIndex}.{varName}`，不反向约束 §4-§7
- VariableBindingModal.vue `onUnbind`（emit 名 `update:bindings`，不是 `update:varBindings`）触发 varBindings 字典变化；通过 ComposerView 单源传递；SectionCard 通过 props drilling 接收（不 watch，避免 props 嵌套循环）
- 断引用视觉信号实现：ManualSubItemModal 内部 renderer 检测 parent varBindings 字典中找不到匹配 varKey 时，按 R7a 决议显示灰色 OR 黄色高亮；不修改共享 `markdown-renderer.ts`
- **R15b Fallback 路径**：若 sparring 拍 R15b = 全局命名空间（与 plan 高层设计 hardcode 的「局部约定 `{section.sectionIndex}.{varName}`」冲突），U4 watch 拓扑需重做：
  - Plan A（默认局部约定）：props drilling 父 → SectionCard → ManualSubItemModal watch varBindings
  - Plan B（spar 翻转为全局）：ManualSubItemModal 内部存储全局 varBindings reverse-index（从 ComposerView 单源 flatten），入参为「§3 sectionIndex + 全局 varBindings」二项；返回 `{{var}}` 命中结果
  - Plan B 工作量 ≈ Plan A × 1.5；切换时机 = U1 sparring 拍板后 24 小时内决策
- AE6 + AE7 + AE3 覆盖：§7 解除 + §3 引用断引用 + 子项计数保持不变

**Patterns to follow:**
- VariableBindingModal.vue `onUnbind` emit `update:bindings` 模式（`L131-138`）
- watch + emit 同 prop 双向同步模式（Vue 标准）

**Test scenarios:**
- §7 DYNAMIC KO 模式已选 1 个变量绑定 PAR `KO-PAR-0042`（变量名 `min_inventory`），§3 第 3 条子项 content 嵌入 `{{min_inventory}}` → 右栏实时预览高亮（与 §4-§7 完全复用 OR 差异化按 R7a 决议）
- §7 已选 `min_inventory` 后被解除，§3 渲染时仍引用 `{{min_inventory}}` → 右栏预览按 R7a 决议显示视觉信号（断引用色 = 灰显 OR 黄色提示），不抛错
- §4-§7 切换 PAR 后 §3 已写入引用的断引用视觉信号（与 AE6 step3 一致）
- §3 嵌入未定义变量名 `{{xxx}}` 容错：保留原样 / 渲染为空 / 红波浪线（R7a 决议）
- §3 第 N 条子项 active / 断引用两态间切换 → §3 卡片计数保持不变（R12 联动）

**Verification:**
- mvn verify 不被破坏
- vitest 通过
- §7 解除时 §3 引用断引用视觉信号正确（AE6 / AE7 覆盖）
- §3 卡片计数在断引用态切换时不变（与 R12 联动）

---

### U5. 校验规则 + 重复检测 + 批量粘贴（仅 R3b 启用时）

**Goal:** R10 数值字段上下界校验 + R11 重复检测（onBlur + 去抖 300ms 触发；批量粘贴提交时逐条检测）+ R3b 批量粘贴（仅启用时引入 manual-sub-item-parser.ts）+ R4b dirty 检测 + R17a 后端落库 5xx 兜底。

**Requirements:** R10 (校验规则), R11 (重复检测), R3b (批量粘贴启用时), R4b (dirty 检测), R17a (后端 5xx 兜底)

**Dependencies:** U1 (R10 / R11 / R3b sparring 决议) + U2 (弹层落地基础)

**Files:**
- Modify: `frontend/src/components/ManualSubItemModal.vue`（含校验规则 + 重复检测 + dirty 状态 + R4b 三按钮 + R17a 兜底）
- Create: `frontend/src/utils/manual-sub-item-parser.ts`（仅 R3b 启用时引入；每行一条 / Markdown 列表 / CSV 三格式 → 推荐 1 格式收敛）
- Modify: `frontend/src/utils/element-plus-rules.ts`（公用校验规则工具）
- Modify: `frontend/src/components/ManualSubItemModal.test.ts`（R11 AE5 弹窗二选一）
- Modify: `frontend/src/utils/manual-sub-item-parser.test.ts`（如 R3b 启用，添加批量粘贴解析测试）

**Approach:**
- Element Plus `el-form` + `async-validator`
- R10 校验规则按 sparring 拍板：
  - 必填字段：`title` + `content` 必填
  - 数值字段（如启用）：`[0, 1000]` 范围
  - 重复检测：onBlur + 去抖 300ms；批量粘贴触发 = 全部粘贴完成提交时逐条
- R11 触发时机 + 命中处置按 sparring 拍板（覆盖 / 取消 / 并列 / 弹窗二选一）
- R3b 批量粘贴启用时引入 manual-sub-item-parser.ts，否则跳过
- dirty 状态：last-snapshot 与 current-snapshot 对比；检测到 dirty 时弹层顶栏三按钮互斥
- R17a 兜底：后端落库 5xx 时 Toast「操作成功但同步失败，请刷新」

**Patterns to follow:**
- VariableBindingModal 4 按钮组（⚡/⊕/✎/×）作为 R11 命中处置参考
- Element Plus `el-form` + `async-validator` 校验模式（项目内可能已有公用 rules 工具）

**Test scenarios:**
- AE5 重复 title 弹窗二选一：业务专家在弹层中新增一条 title = "料场库存上下限"（与已有第 1 条 title 完全相同）→ 弹出确认弹窗「已存在同名子项，是否替换？」，选择替换后只保留最新一条
- 数值字段超出 [0, 1000] 范围：value = "1500" + unit = "t" → 输入框失焦后即时提示数值超过上限，不进 §3 卡片计数；修改为 value = "850" 后才进入计数
- dirty 后切换 tab 5s 自动撤销：管理员首次变更第 1 条 title + 切到 §4 卡片 → 5 秒后回切 → dirty 仍存在
- Toast「3 秒撤销」期间切到 §4-§7 KO 模式 → §3 Toast 倒计时冻结 + 切回自动续秒
- §3 后端落库返回 5xx → R17a Toast「操作成功但同步失败，请刷新」显示
- R3b 批量粘贴启用：业务专家粘贴 100 行 → R11c paste-fail / partial-import 态按 sparring 决议覆盖

**Verification:**
- Element Plus `el-form` + `async-validator` 校验场景覆盖完毕
- AE5 / AE-Coverage-Notice 1/4 路径覆盖
- 不破现有 33/33 vitest 累计

---

## System-Wide Impact

- **Interaction graph:** SectionCard.vue DYNAMIC 分支 → ManualSubItemModal/Drawer 子组件（U2）→ emit `update:manualSubItems` → ComposerView 响应式 watch（U3 数据形态契约）→ renderHandlebars → markdown-renderer（U4 高亮）→ 右栏实时预览。同时 VariableBindingModal.vue `onUnbind` emit → ComposerView → SectionCard watch 传播 → ManualSubItemModal 视觉信号更新（U4 断引用）。
- **Error propagation:** R17a 后端 5xx → 前端 Toast「操作成功但同步失败，请刷新」不撤销 UI 状态；R11 paste-fail → R11c 态文案 + 「改为逐条录入」回退入口。
- **State lifecycle risks:** ManualSubItems 类型迁移期间（U3 部署期 1 sprint）存量 string 字段需向后兼容；存量 string 字段遇 array schema 由一次性 migration 脚本兜底；U2 期间 toMultiString 过渡层兜底类型升级。
- **API surface parity:** 后端 `ComposerController.parseContext` (POST /api/composer/render 等效路径) 接收数组 schema；同时 `GET /api/composer/manual-sub-items/{sectionIndex}` 返回 List<ManualSubItem>（如 spr 拍板新增）。
- **Surface coverage:**
  - **App (frontend)** — in-scope: SectionCard.vue DYNAMIC 分支 + ManualSubItemModal / Drawer 子组件 + ViewerView 响应式 watch + markdown-renderer
  - **Admin** — out-of-scope: 本 plan 焦点在 §3 用户侧弹层，admin 端不在范围
  - **Backend** — in-scope: ComposerController.parseContext 反序列化 + ComposerRenderService.mergeSectionContext 拼接语义 + ManualSubItem.java 新 DTO + 迁移 SQL
  - **Audit / Jobs** — out-of-scope: U5 Toast 仅前端 UI 回滚，不写 audit_log（PRD OQ-5 已锁）
  - **Observability** — deferred: V3 视觉对比 + check-v3-style.sh 通过即可，metrics 不在范围
  - **Testing** — in-scope: 手测 + mvn verify + vitest；不引入新测试框架
- **Integration coverage:** ComposerController → ComposerRenderService → ComposerView → SectionCard → ManualSubItemModal → markdown-renderer 端到端；VariableBindingModal.onUnbind → ComposerView → SectionCard prop 同步。
- **Unchanged invariants:** VariableBindingModal 4 按钮组（⚡/⊕/✎/×）+ OQ-5 3 秒撤销仅前端回滚 + PRD §3.1.3 三栏布局 + OQ-16 公式结构（`selectedKOs + varBindings + manualSubItems`）+ OQ-5 / OQ-12 / OQ-16 / OQ-18 / OQ-5 等 PRD §10.5 已锁决议不变。

---

## Risks & Dependencies

| Risk | Mitigation |
|------|-----------|
| U1 sparring 议程延期导致 U2-U5 顺延 | 召集业务专家前移到 Sprint 2 末期；如确实延期，U2 先停在弹层组件骨架 + R0a 默认决议（中央 Dialog + 行内列表），后续 sparring 再调 |
| U3 类型迁移跨 4 端工作量爆炸（尤其叠加 5 业务字段后） | R5 阶段 1 sparring 必答 schema 复杂度先评估；R1-implementation-bridge 承诺 textarea 整体替换 + ManualSubItems 类型升级同步；存量 string 字段 1 sprint 兼容 |
| SectionCard.vue L58-66 整体替换可能破现有手动子项流 | 过渡期 toMultiString 兼容层；U3 完成后去掉；migration 脚本兜底 |
| 后端 `String.replace` 改逐条拼接可能引入 NPE | ComposerRenderTest.java 覆盖每条空 / null / 边界 + mvn verify |
| U4 跨段 `{{var}}` 高亮色若与 §4-§7 差异化引入新 CSS 变量 | R7a sparring 必答复用 / 差异化；差异化情况下 design token 名按 frontend-standards 命名 |
| 高行数（≥R9 软上限）虚拟滚动性能 | pageSize=50 默认；Element Plus `el-table-v2`；性能压测在 U2 验证 |
| R11 重复检测时机 onBlur + 去抖 300ms 在 R3b 批量粘贴时误判 | 批量触发独立走「全部粘贴完成提交时逐条检测」路径，不与单条 onBlur 互踩 |
| 后端 API /api/composer/manual-sub-items/{sectionIndex} 尚未存在 | U5 实施期间假设 Spring Boot 路由可达；如不存在，U5 拆为 U5a（前端 mock fallback）+ U5b（后端 API 单独开发） |
| F-53 视觉对比不通过引发 §3 弹层视觉迭代 | V3 视觉对比 + check-v3-style.sh 强制跑 + 1024 px + 768 px 双断点截图；如未通过走 F-53.3 流程迭代 |

---

## Documentation / Operational Notes

- `docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md`（origin doc）：35 项 P0+P1 fix 已落地，是本 plan 的 source-of-truth。
- `docs/sparring/2026-07-XX-001-q-i4-section3-sparring-decisions.md`（U1 产出）：sparring 议程决议表，U2-U5 实施输入。
- `CHANGELOG.md`：U2-U5 完成后追加 `v1.17.X §3 手动子项弹层 + ManualSubItems 类型迁移`，user-visible 标记。
- §3 弹层 F-53 视觉对比 + `check-v3-style.sh` 强制跑（origin doc 35 项 fix F-53 教训引用）。
- README 用户指南更新：「§3 DYNAMIC 手动子项模式」使用指引（如果项目有用户文档）。

---

## Sources & References

- **Origin document:** [`docs/brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md`](../../brainstorms/2026-07-16-001-q-i4-section3-manual-subitem-modal-requirements.md)
- Related code:
  - `frontend/src/components/SectionCard.vue` (T210 commit 651c334)
  - `frontend/src/components/VariableBindingModal.vue` (T210 OQ-16 4 按钮组)
  - `frontend/src/views/prompts/ComposerView.vue` (OQ-16 实现)
  - `frontend/src/api/composer.ts` (L43-46 ManualSubItems string)
  - `backend/src/main/java/com/liaogang/famou/km/prompt/controller/ComposerController.java` (L70-76)
  - `backend/src/main/java/com/liaogang/famou/km/prompt/service/ComposerRenderService.java` (L102-109)
- Related PRs/issues:
  - `9980417` Merge feat/sprint-2-ko-and-permissions: Sprint 1+2 + F-53 + F-54
  - `c0d5aec` feat(ko): T201 实施 - U4 数据模型 + 状态机（Wave 1）
  - `651c334` feat(prompt-ui): T210 实施 - U6 三栏组装器 UI（Wave 4）
- External docs:
  - `frontend-standards.md` §7.1-7.3 CSS 变量与响应式双断点
  - PRD `docs/brainstorms/liaogang-famou-km-platform-requirements.md` §3.1.3 三栏布局 + §10.5.1 PRP 实际装配数 + §10.5.3 渲染层
