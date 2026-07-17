---
date: 2026-07-16
topic: q-i4-section3-manual-subitem-modal
spec_id: 2026-07-16-001-q-i4-section3-manual-subitem-modal
---

# Q-I4 §3 手动子项弹层 UI 决策（Sparring 议程输入）

## Summary

为 Q-I4 业务专家 sparring 议程准备的产品决策收口。文档锁定 KO-PRM-0001 PRP-0001 §3「计算范围」DYNAMIC 手动子项模式（PRD v0.32 §10.5.1 演示值 38 条 manualSubItems）的弹层形态、字段 schema、校验规则与 UI 一致性 4 类产品决策，作为 sparring 必拍板项直接交给业务专家。本文档不打开 §4-§9 DYNAMIC KO 模式与 §1/§2/§8 FIXED 段的弹层问题，也不锁定具体前端实现。

---

## Problem Frame

PRD v0.32 §10.5 引入 PRP 实际装配数动态计算后，§3 是唯一一段需要业务专家一次性录入手动子项而不依赖 KO 库的 Section：§3 的 38 个 manualSubItems 完全来自业务专家录入，其余三列（KO 模式 0 / 变量绑定 PARs 0 / 引用 SCHs 0）贡献为零。

Sprint 2 T210（commit `651c334`）已落「三栏组装器 UI + SectionCard.vue 最小骨架」，但 DYNAMIC 手动子项分支的具体产品形态未定：弹层是列表式 / 表格式 / 抽屉式 Dialog 三选一；字段是只含 title + content 还是含业务字段（值 / 单位 / 上下界）；校验是只管必填还是管值边界。这些决策不定，前端 V3 视觉验收（F-53 强制 V3 视觉对比 + `check-v3-style.sh`）会在该分支上卡。

Plan §Open Questions、Sprint 2 task pack、Sprint 3 task pack（`2026-08-01-001-task-pack-sprint-3-governance-projects.md` T310 risk_note）三处都把 Q-I4 列为未解决。这是 Sprint 2 启动前、U6 实施前应收齐的输入。Sparring 议程需要把 Q-I4 闭环为可让 SectionCard.vue 落地为代码的决议。

---

## Actors

- A1. 业务专家（sparring 必拍板方）：决定弹层形态、字段 schema、校验规则、按钮组处置。
- A2. 知识库管理员（前端用户）：进入组装器、点击 §3 卡片唤起弹层、逐条录入 / 编辑 / 删除手动子项。
- A3. 合规审核员（次要）：可旁观该 Section 装配合规性，但本 Q-I4 不直接关系审核流。
- A4. 模板作者：负责 KO-PRM-0001 / PRP-0001 模板结构（§3 段位置 / §N 编号 / 章节数），不是本 Q-I4 的修改对象。

---

## Key Flows

- F1. §3 手动子项录入流程
  - **Trigger:** 知识库管理员在组装器中栏 §3 Section 卡片内唤起弹层。
  - **Actors:** A1, A2
  - **Steps:**
    1. 管理员在 §3 卡片点击「手动子项」入口，弹层出现（首次唤起为空弹层时进入 `R11c empty` 态，遵循 R-onboarding 引导）。
    2. 弹层呈现 sparring 决议后的固定形态（R0a 容器形态 + R1b 内部布局 = 4 组合路径之一）。
    3. 管理员逐条录入子项，content 字段按决议支持纯文本 / Markdown / 富文本；含 `{{var}}` 嵌入时遵循 R7 / R7a / R15b 决议。
    4. 弹层关闭或管理员按「完成」后，§3 卡片显示当前子项数；右栏实时预览同步重渲染（R0a 中央 Dialog 形态下仅在弹层关闭后才刷新一次）。
  - **Outcome:** §3 卡片子项计数 = manualSubItems 子项数组长度（与嵌入 `{{var}}` 是否断引用无关）。PRP 实际装配数（条目数维度）对应增长；顶部装配进度小计同步刷新。**§3 三列 selectedKOs=0 / varBindings=0 是本 Q-I4 当前展示状态描述，不是永久约束**；本 Q-I4 仅约束 §3 手动子项录入路径，不约束 §4-§7 KO 模式弹层。
  - **Covered by:** R0a, R1a, R1b, R1-implementation-bridge, R2, R5-阶段-1, R6, R7, R7a, R11c, R12, R12a, R-onboarding, R-a11y-baseline
  - **AE-Coverage-Notice:** F1 / AE1 / AE1b 当前覆盖 R0a=中央 Dialog × R1b=行内列表 1/4 路径。其余 3 路径（中央 Dialog × 表格列 / 抽屉 × 行内列表 / 抽屉 × 表格列）由 plan 阶段按 sparring 决议补 AE。

- F2. 单条手动子项删除 + 撤销流程
  - **Trigger:** 管理员在 §3 弹层内点击某条子项的删除按钮。
  - **Actors:** A2
  - **Steps:**
    1. 该条子项在前端 UI 状态被移除。
    2. 顶部出现 Toast「已移除，3 秒内可撤销」。
    3. 3 秒内点击撤销，仅前端 UI 回滚该条。
    3.5. Toast 期间 tab 失焦 / 切后台 / 多 tab 同时启用时 3 秒倒计时同源冻结 + 切回自动续秒。Toast 期间 §4-§7 KO 选中 PAR 变更不影响撤销语义。Toast 期间 §3 后端落库返回 5xx 时 UI 显示「操作成功但同步失败，请刷新」（不撤销 UI 状态，不重写 audit_log）。
    4. 3 秒后或刷新页面，撤销不可恢复（不写后端审计日志）。
  - **Outcome:** §3 卡片子项计数减 1；3 秒内可恢复；3 秒后永久移除；跨 tab / 网络失败 / 跨段同时变更各按 R17 / R17a / R4b 兜底。
  - **Covered by:** R13, R14, R17, R17a

---

## Requirements

**A. 弹层形态决策**

- R0a. 弹层容器形态由 sparring 必拍板：① 中央 Dialog（弹层打开期间**右栏实时预览仅在弹层关闭后才刷新一次**，专注录入）；② 右侧 push-out 抽屉 Drawer（保留右栏实时预览，要求桌面宽屏 ≥1440 px 与 `frontend-standards.md` §7.1-7.3 xl 断点对齐）。md-lg 之间回退到中央 Dialog（避免悬挂态）。R0a 决议锁定弹层与 PRD §3.1.3 三栏的层级关系。
- R1a. §3 手动子项弹层容器形态采用 sparring 决议的 R0a 唯一结果（中央 Dialog 或右侧 push-out 抽屉 Drawer），不允许多种容器并存。
- R1b. §3 手动子项弹层内部布局采用 sparring 决议的 R1b 唯一结果：① 行内列表（每行一表 + 行内编辑）② 表格列（顶部表头 + 表格列）。R1a 容器 × R1b 内部布局 = 4 组合路径全部可达。
- R1-implementation-bridge. sparring 拍板后 §3 当前单一 textarea（`SectionCard.vue` L58-66）将被本弹层整体替换、原 textarea 字段永久下线；`ManualSubItems` prop 类型同步从 `string` 升级为结构化数组（OQ-16 计算公式与 F1 类型迁移工作量清单见 Outstanding Questions Deferred to Planning 第 4 项）。
- R1-effort-reference. 工程量参考：列表式（行内编辑 Modal）≈ 1 ManualSubItemModal.vue + 替换 SectionCard.vue 子组件 import；表格式（表头批量复制粘贴）≈ 1 Modal + 1 PasteFormatParser.ts + Element Plus `el-table` 集成；右侧 push-out 抽屉 ≈ 1 ManualSubItemDrawer.vue + 桌面 ≥1280px 兼容。
- R2. 弹层唤起入口放置在 §3 Section 卡片内部，入口位置（卡片标题旁按钮 / 卡片底部「手动子项 N 项」链接 / 卡片右上角菜单）由 sparring 决议决定；评估维度（1) 视觉显著 / 2) 与 SectionCard.vue 现有「+ 新增手动子项」按钮一致 / 3) 与 VariableBindingModal 触发一致）三维度各 1-5 分由 sparring 现场扫一眼判断。
- R3a. 单条逐项录入是默认入口，每行 + 删除 / ⊕ 操作机制按 F1 Step 3 描述。
- R3b. 批量粘贴是否启用 + 是否收敛到 1 种解析格式（推荐「每行一条」，三格式并行属范围扩大）由 sparring 必拍板，F1 Step 3.5（如启用）描述粘贴步骤。**R3b 启用 Markdown 列表格式时仅支持一级，不解析嵌套**（与 R8 子项间不嵌套联动；二级以下 Markdown 列表输入按字面文本处理，渲染期不嵌套）。
- R-onboarding. **首次进入弹层 onboarding 文案**（R11c `empty` 态必含）：① 至少 1 行字段填写示例（带 title / content 示例值）② 主操作 CTA「+ 新增第一条子项」自动聚焦 ③ 批量粘贴入口若启用（R3b）需在 `empty` 态下显眼可见。若 sparring 决议跳过 onboarding 引导，须 sparring 显式签字接受首次 UX 摩擦。
- R4. 弹层关闭机制固定：右上角 X + ESC 键 + 弹层外点击三种触发同一关闭动作。**R4 仅约束「关闭前若子项数较唤起前增加」场景，不强制二次确认**（仅在 §3 卡片子项计数有新增时不对弹层关闭二次确认）。dirty 检测（已修改未保存的内容）走 R4b，不在 R4 范围内。保存模型详见 R4b。
- R4b. 弹层内的保存模型 = 「每行失焦即存」 + 关闭前 dirty 检测（首次变更后弹层顶栏出现「未保存」提示条）。关闭前检测到 dirty 时弹层顶栏三个互斥按钮：「Save Draft & Close」（保存草稿保留修改）/「Discard & Close」（丢弃并关闭）/「Cancel」（留在弹层）。**取消且保留 3 秒 Toast 撤销语义仅适用于单条删除（F2），不适用于关闭弹层整层**。跨设备协同冲突策略 = last-write-wins + 顶栏黄色提示「§3 在另一设备上被修改，当前版本以最后保存为准」。dirty 锚点 = 弹层层「上次入栈的子项集合」vs 全局层「ComponentView 的 manualSubItems」由 sparring 决议补 R4d。

**B. 字段 schema 决策**

- R5. **R5 阶段 1**（sparring 必拍板）：每条手动子项字段 schema 选一（`title + content` 二字段 vs 叠加业务字段 `value` / `unit` / `lower_bound` / `upper_bound` / `range_type`）。**R5 阶段 2**（阶段 1 拍板后再进）：OQ-16 公式兼容评估 + `ManualSubItems` 类型迁移工作量清单（详见 Outstanding Questions Resolve-Before-Planning 第 5 项）。
- R6. `content` 字段输入侧编辑器能力按 sparring 决议三选一或叠加：纯文本 / Markdown / 富文本，并明确告知业务专家「输出侧渲染走 PRD §10.5.3 已锁的 9 类 Markdown 元素 + Handlebars 子集」。
- R7. `content` 是否允许嵌入 `{{var}}` 引用变量绑定 PAR 由 sparring 决议决定；若允许，调用路径与 §4-§7 DYNAMIC KO 模式中已选 PAR 的链路必须打通。**R7 默认建议**：「子项内嵌入 `{{var}}` 不计入 `manualSubItems.size`，仅作为渲染期可消费内容参与 OQ-16 final assembly」。R7 决议前 sparring 必答「子项内 `{{var}}` 是否计入装配数公式」。**子项计数 = 当前数组长度，与嵌入 `{{var}}` 是否断引用无关**（与 R12 / R7a 联动）。
- R7a. §3 `{{var}}` 在右栏预览中的高亮规则与 §4-§7 DYNAMIC KO 已选 PAR 的视觉一致性 = sparring 必拍板项：① 高亮色 / 字号 / 行内染色是否完全复用 ② 未定义变量名容错（保留原样 / 渲染为空 / 红波浪线）③ §4-§7 切换 PAR 后 §3 已写入引用的断引用视觉信号（灰显 OR 黄色提示）。子项在 active / 断引用两态间切换时不触发 §3 卡片计数重算（与 R12 联动，「子项计数 = 数组长度」）。
- R8. 子项之间不允许互相引用、嵌套、跨条引用；本 Q-I4 不打开「子项内联引用」边界。`{{var}}` 绑定的是 KO-PRM 中的 PAR 变量，PAR 的内部引用链不在 R8 禁止范围内；R8 仅约束同一 Section 内子项之间的直接 content 引用。

**C. 校验规则决策**

- R9. 单段子项数量上限决议须同时给出 **软上限**（sparring 必答拍板的**确切数字**）+ **硬上限**（sparring 必答拍板的**确切数字**）。`软上限` 触发 UI 黄色提示「建议拆分 §3」+ 强制启用虚拟滚动（pageSize=50）；`硬上限` 触发前端拒绝新增。上下限为 sparring 必拍板项的硬约束数字，禁止拍板后修改 plan 阶段采用默认值；与 `frontend-standards.md §7.1-7.3` CSS 变量与响应式双断点 + §3.1.3 视觉一致性强约束。PRD §10.5.1 演示值 38 作为参考而非上限；不嵌入 UI 状态命名（R11c 用 `high-row-scroll` 而非 `38-row-scroll`）。
- R10. 子项必填字段（`title` 必填；`content` 必填）与值边界校验（数值字段上下界 / 枚举字段取值范围）由 sparring 决议决定。
- R11. 重复检测策略按 sparring 决议：相同 `title` 视作重复 / 相同 `title` + 相同 `content` 视作重复 / 不检测。R11 命中后处置（覆盖 / 取消新增 / 并列保留 / 弹窗二选一）也是 sparring 必拍板项。**触发时机 = onBlur 且 title 字段失焦后去抖 300ms 检测**（AE5 单条场景）；**批量粘贴触发 = 全部粘贴完成提交时逐条检测**，命中处置按 sparring 决议的并列保留 / 整批拒绝二选一。
- R11-trim-typo. Outstanding Questions Resolve-Before-Planning 第 3 项 `[Affects R5, R6, R10, R11, R11, R11c]` 中重复的 `R11` 删除（保留 `R5, R6, R10, R11, R11c`）。
- R11c. 弹层必须支持且仅以下 7 态：`empty` / `loading` / `error` / `validation-fail` / `paste-fail` / `high-row-scroll` / `partial-import`（不嵌入 PRD §10.5.1 演示值 38 到状态命名，避免业务专家误以为 38 是软上限入口阈值）。每态需有最小可见文案与控件指引，由 sparring 决议决定具体文案（受 §3.1.3 视觉一致性 + 前端 Accessibility 推迟项约束）。
- R11c-trigger-condition. 每态的 enter-trigger / exit-trigger / 阻塞交互 / dirty 检测关系由 sparring 必答：
  - `empty` enter-trigger = 弹层首次唤起且无子项；exit-trigger = 新增首条子项；阻塞交互 = 无；dirty = 无
  - `loading` enter-trigger = 异步草稿拉取；exit-trigger = 拉取完成；阻塞交互 = 弹层 spinner；dirty = 不阻塞
  - `error` enter-trigger = API 不可用或解析错；exit-trigger = 重试 / 取消；阻塞交互 = 全弹层；dirty = 保留
  - `validation-fail` enter-trigger = R10 字段校验失败；exit-trigger = 字段修正；阻塞交互 = 保存按钮；dirty = 保留
  - `paste-fail` / `partial-import` 仅在 R3b 启用批量粘贴时出现；enter-trigger = 批量粘贴解析失败 / 部分成功；exit-trigger = 改为逐条录入或整批拒绝；阻塞交互 = 全弹层；dirty = 保留
  - `high-row-scroll` enter-trigger = 子项数 ≥ R9 软上限阈值；exit-trigger = 删减至阈值以下；强制启用虚拟滚动 / 分页（pageSize=50）。不嵌入演示值 38 到状态命名

**D. UI 一致性决策**

- R12. 弹层与 PRD §3.1.3 已锁的「顶部 PRM 选择栏 + 中栏 Section 编排 + 右栏实时预览」三栏联动保持一致：弹层内每条子项的增删改，必须同步刷新 §3 卡片子项计数（= `manualSubItems` 子项数组长度，与嵌入 `{{var}}` 是否断引用无关）+ 顶部 PRM 选择栏装配进度小计（条目数维度 = OQ-16 PRP 实际装配数）。字符数 / Token 数 g(M) 函数不作为 §3 弹层同步项；如 sparring 议程能给出 §3 为什么需要字符 / Token 计量的产品用例，单独立项处理。**弹层 → 父级 ComposerView → 右栏渲染 pipeline 的数据形态契约**：R5 阶段 1 拍板后，`renderedHtml` 的 context.manualSubItems 类型从 string 升级为结构化数组；`renderHandlebars` 的输入契约同步升级（含 every each-block 替换语义从整段 string 替换改写为逐条拼接）。
- R12a. **视觉一致性继承声明**：§3 弹层复用 Element Plus `el-dialog` / `el-drawer` + 已锁 design token（与 §4-§7 DYNAMIC KO 模式弹层共享同一组件库），字号 / 配色 / 间距遵循 `@frontend-standards.md` §X.Y design-token 名。关闭按钮位置 / ESC 行为 / 加载 spinner 全站统一；§3 弹层不引入新组件、不新增 design token。
- R-a11y-baseline. **a11y 基线（非 a11y 深度细节）**：① 弹层至少在 ≥1024 px 桌面可用（中央 Dialog）/ ≥1440 px 可用（抽屉式，与 frontend-standards xl 断点对齐）；② 全键盘可达：Tab 顺序、ESC 关闭、Enter 提交、空格切换复选框；③ Toast 用 `aria-live=polite`；④ 必填字段用 `aria-required=true` + `aria-describedby` 接错误信息。响应式策略可继续 deferred 到 F-53，但 a11y baseline 在 sparring 拍板前确定。
- R13. 顶部按钮组「⚡ 自动绑定 / ✎ 当前填写 / ⊕ 重选 / × 解除」在 §3 DYNAMIC 手动子项模式下的处置 = 跟随 R1 决议自动确定（不作为独立 sparring 必拍板项）：列表式（行内编辑）保留 ⊕ + ✎；中央 Dialog 全部隐藏；抽屉式视 `frontend-standards.md §7.1-7.3` xl 断点（≥1440 px）保留 ⊕ 仅，md-lg 之间回退到中央 Dialog。若 sparring 必谈「全部保留」分支，点击 ⚡ / ✎ / × 须保持空操作 + Toast「§3 DYNAMIC 手动子项模式不支持该操作」。Toast 复用项目既有全局 Toast 组件（与 R14 对齐）。
- R14. §3 弹层内删除单条子项触发 Toast「已移除，3 秒内可撤销」（仅前端 UI 回滚，沿用 OQ-5）。**前置事实检查**：若前端已有统一全局 Toast 组件 → 复用；若前端尚无统一全局 Toast 组件 → §3 弹层需单独引入 Toast 容器。该前置事实由 sparring 议程同步确认（Outstanding Questions 已加 [Affects R14] 项）。

**B'. 运行时变量解析链路（sparring 必拍板；不属 §E PRD 已锁边界）**

- R15a. 渲染输出走 PRD §10.5.3 已锁的产品可观察输出：9 类 Markdown 元素（H2-H4 / 粗斜删除线 / 代码块 / 表格 / 列表 / 引用 / 链接 / 分隔线 / `{{var}}` 高亮）+ Handlebars 子集（`{{var}}` / `{{#each items}}` / `{{#if}}`）。**本条仍属 PRD 已锁边界**。
- R15b. **R15a 运行时变量解析链路**（sparring 必拍板；非 PRD 已锁边界）：§3 content 解析阶段持有的变量命名空间归属 + §4-§7 varBindings 字典在 §3 渲染阶段的可见性 + §7 PAR 解除后的 §3 已写入 `{{var}}` 兜底（保留原样 / 渲染为空字符串 + 黄色提示）由 sparring 议程决定。本条仅就 §3 渲染期可见性论；§4-§7 变量命名空间仍由其自身 sparring / PRD 拍板，§3 不反向约束。

**E. PRD 已锁边界（不再问，仅 R15a + R16 + R17 + R18）**

- R16. PRP 实际装配数走 PRD §10.5.1 OQ-16 已锁的动态计算：每段实际装配 = `selectedKOs + varBindings + manualSubItems`，每次渲染时计算，模板作者不登记固定数。**R5 阶段 2 必须重新推导** `manualSubItems.size` 在 §3 段的语义（= 子项数组长度，非 section 数）以确保 PRD §10.5.1 演示值 38 在新 schema 下仍可重放。
- R17. 删除单条子项沿用 OQ-5 已锁的 3 秒撤销机制：仅前端 UI 回滚，不调后端 `/api/audit/{id}/revert`（PRD v0.32 §5.2.6 已删除该接口契约），不写 `USER_MANUAL_SUBITEM_DELETE` 审计日志。
- R17a. 当前模型下若 §3 子项最终落库后端，撤销期间后端落库返回 5xx 时 UI 显示「操作成功但同步失败，请刷新」（不撤销 UI 状态，不重写 audit_log）由 sparring 决议决定。
- R18. 跨设备撤销按 OQ-5 决策不可行：另一设备登录后会话仍持有旧前端状态，不做跨设备撤销。
- R16. PRP 实际装配数走 PRD §10.5.1 OQ-16 已锁的动态计算：每段实际装配 = `selectedKOs + varBindings + manualSubItems`，每次渲染时计算，模板作者不登记固定数。
- R17. 删除单条子项沿用 OQ-5 已锁的 3 秒撤销机制：仅前端 UI 回滚，不调后端 `/api/audit/{id}/revert`（PRD v0.32 §5.2.6 已删除该接口契约），不写 `USER_MANUAL_SUBITEM_DELETE` 审计日志。
- R17a. 当前模型下若 §3 子项最终落库后端，撤销期间后端落库返回 5xx 时 UI 显示「操作成功但同步失败，请刷新」（不撤销 UI 状态，不重写 audit_log）由 sparring 决议决定。
- R18. 跨设备撤销按 OQ-5 决策不可行：另一设备登录后会话仍持有旧前端状态，不做跨设备撤销。

---

## Acceptance Examples

- AE1. **Covers R1, R2.** Given sparring 决议选择「列表式 + 行内编辑 + 入口放在卡片标题旁按钮」，when 业务专家点击 §3 卡片标题旁的「手动子项」按钮，then 弹出宽 720px 的对话框，第一条已有子项处于 inline 编辑态（光标已定位 title），底部固定「+ 新增子项」按钮。
- AE1b. **Covers R2 (备选入口).** Given sparring 决议入口放在卡片底部「手动子项 N 项」文本链接，when 业务专家在 §3 卡片底部点击该链接，then 弹出同上 720px 对话框，第一条已有子项高亮行（区别于 AE1 标题旁按钮态）。
- AE2. **Covers R5, R6, R10.** Given sparring 决议允许 `value` 业务字段且校验规则为 `value` 必填且必须在 `[0, 1000]` 内，when 业务专家录入一条 `title = "料场库存上下限"`，`value = "1500"`，`unit = "t"` 的子项，then 输入框失焦后即时提示数值超过上限，不进 §3 卡片计数；修改为 `value = "850"` 后才进入计数。
- AE3. **Covers R7, R7a, R12.** Given sparring 决议允许 `content` 嵌入 `{{var}}` 且 §7 DYNAMIC KO 模式已选择 1 个变量绑定 PAR `KO-PAR-0042`（变量名 `min_inventory`），when 业务专家在 §3 第 3 条子项 content 中写入 `当前最小库存为 {{min_inventory}} 吨`，then 右栏实时预览将 `{{min_inventory}}` 高亮为已绑定变量值（高亮色与 §4-§7 已选 PAR 完全复用），`{{var}}` 高亮规则遵循 PRD §10.5.3。
- AE4. **Covers R13, R17, R17a.** Given 弹层内已存在 5 条手动子项，when 业务专家点击第 3 条的删除按钮，then §3 卡片子项计数从 5 变为 4；右上角出现 Toast「已移除，3 秒内可撤销」；3 秒内点击撤销，UI 回滚该条 + 顶部 PRM 选择栏装配进度小计同步恢复。Toast 期间切换 tab 时 3 秒倒计时冻结 + 切回自动续秒。
- AE5. **Covers R11, R14.** Given sparring 决议的重复检测策略为「相同 title 视作重复」，when 业务专家在弹层中新增一条 `title = "料场库存上下限"`，与已有第 1 条 title 完全相同，then 弹出确认弹窗「已存在同名子项，是否替换？」，选择替换后只保留最新一条。
- AE6. **Covers R7, R7a, R15b.** Given §7 已选 `min_inventory` 后被解除，when §3 渲染时仍引用 `{{min_inventory}}`，then 右栏预览按 sparring 决议的兜底策略（保留原样高亮 OR 渲染为空字符串 + 黄色提示）显示，不抛错；§4-§7 切换 PAR 后 §3 已写入引用的断引用视觉信号 = 灰显 OR 黄色提示（R7a 决议）。
- AE7. **Covers R7, R12.** Given §3 子项计数 = 5 条且第 3 条 `content` 含 `{{min_inventory}}` 已被 §7 解除为断引用，when 业务专家编辑第 1 条 title（不触发断引用变化），then §3 卡片子项计数保持 5（数组长度不变），右栏第 3 条显示灰显 OR 黄色提示（断引用视觉信号），不触发计数重算。
- AE-empty. **Covers R-onboarding, R11c.** Given §3 首次唤起且无任何子项，when 弹层打开，then 进入 R11c `empty` 态，显示 onboarding 引导：① 至少 1 行字段填写示例（带 title / content 示例值）② 主操作 CTA「+ 新增第一条子项」自动聚焦 ③ 批量粘贴入口若启用（R3b）显眼可见。
- AE-dialog-grid. **Covers R0a ① × R1b ②.**（中央 Dialog × 表格列 路径，与 F1 / AE1 AE1b 仅覆盖 R0a=中央 Dialog × R1b=行内列表 互补）Given sparring 决议此路径，when 业务专家在 §3 卡片标题旁点击「手动子项」按钮，then 弹出居中宽 720px 的 Dialog，内部为 `el-table` 表格列（带表头），每行可编辑，第一条已有子项进入表格首行；批量粘贴若启用（R3b）则在表格上方提供「批量粘贴」按钮。

---

## Success Criteria

- 业务专家 sparring 议程对以下 15 项产品决策全部拍板（不出现「暂缓」）：R0a 容器形态 + R1a 容器唯一结果 + R1b 内部布局唯一结果 + R2 入口位置 + R3b 批量粘贴启用 + R5 阶段 1 字段 schema + R6 content 编辑器能力 + R7 `{{var}}` 嵌入 + R7a `{{var}}` 视觉一致性 + R9 软硬上下限（确切数字）+ R10 校验规则 + R11 重复检测 + R11c 7 状态文案 + R4b dirty 检测 + R15b 运行时变量解析链路。
- 前端 SectionCard.vue 的 DYNAMIC 手动子项分支依据 sparring 决议完成后，V3 视觉对比与 `bash scripts/check-v3-style.sh` 通过。
- Plan §Open Questions、Sprint 2 task pack、Sprint 3 task pack 三处 Q-I4 引用条目同步标记为已闭环或删除，TP-3 T310 risk_note 措辞由其 owner 在 Sprint 2 sparring 闭环后单独更新。
- R5 阶段 2 完成后 OQ-16 公式中 `manualSubItems.size` 在 §3 段的语义（= 子项数组长度）已重新推导，确保 PRD §10.5.1 演示值 38 在新 schema 下仍可重放。
- 不修改 PRD §10.5.1 §3 PRP 实际装配数 38 这个演示值（已锁，仅作参考），不将演示值 38 嵌入 UI 状态命名（如 R11c 7 状态不含 `38-row-scroll`）。

---

## Scope Boundaries

### Deferred for later

- §4-§9 DYNAMIC KO 模式弹层（KO 库选择 + 变量绑定弹窗）—— 已有 PRD §3.1.3 + OQ-16 已锁。
- §1/§2/§8 FIXED 段变量赋值区弹层 —— 已有 PRM §10.5.3 渲染层定义，Q-I4 不打开。
- 后端接口契约（POST /api/composer/manual-subitems 之类具体路径、字段、错误码）—— 留给 plan。
- 国际化文案 + 无障碍（a11y）深度细节 —— 留给 i18n 工作和 F-53 后续专项。
- 38 条子项列表式一次渲染的浏览器性能（卡顿 / 滚动）—— 留给前端压测。

### Outside this product's identity

- PRP 装配数 38 这个演示数字本身 —— PRD v0.32 §10.5.1 已锁，不动。
- TP-3 T310（U9 项目管理）risk_note 上的 Q-I4 引用 —— 这是 TP-3 author 把 Q-I4 风险挂在项目管理视图上的 risk 引用，不是另一份 scope；本 brainstorm 仅收 §3，不打开 U9 新边界。
- 模板作者章节调整（§3 改名 / §N 编号重排）—— 留给模板作者自行 track。

---

## Key Decisions

- **Q-I4 严格收敛到 §3**：计划 §Open Questions 在 Sprint 2 / Sprint 3 三处的 Q-I4 引用条目，均视为对 §3 弹层决策的需求；不在本 brainstorm 中扩大边界。R15b（运行时变量解析链路）虽跨 §4-§7 命名空间，但仅就 §3 渲染期可见性论；§4-§7 变量命名空间仍由其自身 sparring / PRD 拍板，§3 不反向约束。
- **Sparring 必拍板项共 15 项**：R0a / R1a / R1b / R2 / R3b / R5 阶段 1 / R6 / R7 / R7a / R9 / R10 / R11 / R11c / R4b / R15b 是 sparring 必出结论的产品决策。R15a 渲染输出 + R16 OQ-16 公式 + R17 / R18 删除撤销与跨设备（除 R4b 外） = PRD 已锁边界，不再讨论。
- **R5 阶段 2 必经**：ManualSubItems 类型迁移（string → 结构化数组）+ OQ-16 公式 `manualSubItems.size` 重新推导在 plan 阶段完成，必须在 SectionCard.vue DYNAMIC 分支实施前完成；不留 sparring 阶段 1 拍板的暗门。
- **渲染层与删除撤销走 PRD 已锁**：§10.5.3 9 类 Markdown + Handlebars 子集负责渲染；OQ-5 3 秒撤销仅前端负责删除反馈；R17 / R18 这两条不复议。
- **批量粘贴格式按 sparring 决议支持**：与 V3 原型 handleLibSearch / renderComposerSections 函数不对齐风险可接受，由 sparring 决议承担；如启用 Markdown 列表格式，仅支持一级，不解析嵌套（R8 联动）。

---

## Dependencies / Assumptions

- Sprint 2 T210 commit `651c334` 中 SectionCard.vue 已实现最小骨架（**仅"+ 新增手动子项" textarea + 单字符串 emit**）。**Toast 撤销 UI / 弹层 / 重复检测 / 批量粘贴等不在 T210 范围内，由本次 Q-I4 §3 弹层实施时新增**。验证 T210 真正含该最小骨架后再开展 sparring。
- 业务专家对 §3 「计算范围」业务域熟悉，可独立拍板弹层形态与字段 schema。
- PRP 装配数 OQ-16 动态计算公式（`selectedKOs + varBindings + manualSubItems`）不再讨论；`manualSubItems.size` 在 §3 段的语义重推导移到 R5 阶段 2（Resolve Before Planning 第 4 项）。
- OQ-5 3 秒撤销机制不再讨论；dirty 检测 / 跨设备 / 后端落库失败兜底按 R4b / R17 / R17a / R18。
- §10.5.3 渲染层产品可观察输出不再讨论；运行时变量解析链路按 R15a + R15b。
- Q-I1（DeepSeek v4）已完成，与 §3 弹层无依赖；Q-I2 / Q-I3 / Q-I5 与 §3 弹层无交叉。Plan §Open Questions 5 项中仅 Q-I4 即本 brainstorm 主题。

---

## Outstanding Questions

### Resolve Before Planning — 全部 13 项已拍板正式生效（2026-07-17，用户授权跳过业务专家复审）

> Sparring 决议归档：`docs/sparring/2026-07-17-001-q-i4-section3-sparring-decisions.md`。全部 13 项 sparring 必答项 + R15a/R16/R17/R17a/R18 共 5 项 PRD 已锁边界 = 18 项决议正式生效（status=ratified）。后续 sprint 阶段如需调整按 sparring-decisions.md 修订记录追加。

- [Affects R0a, R1a, R1b][Resolved 2026-07-17] 弹层容器形态 = 中央 Dialog（Modal 唯一）；内部布局 = 行内列表（R1b 锁定）；抽屉式 ≥1440 px 作为 v2 deferred（origin DE9 R0a 默认 Modal）。md-lg (1024-1440 px) 回退中央 Dialog 不悬挂。
- [Affects R3b][Resolved 2026-07-17] 批量粘贴启用 = 是；解析格式收敛到 1 种 = 「每行一条」；38 条规模下业务专家真实诉求证据需 sparring 会议补一回 R3b 体验。三格式并行不作为默认选项。
- [Affects R5, R6, R10, R11, R11c][Resolved 2026-07-17] R5 阶段 1 = title+content 二字段（业务字段 value/unit/lower_bound/upper_bound/range_type 暂不启用，作为 v2）；R6 = Markdown 编辑器；R10 = title + content 必填；R11 = 相同 title 视作重复（onBlur + 去抖 300ms）+ 命中处置弹窗二选一（替换/取消）；R11c 7 状态文案 = empty/loading/error/validation-fail/paste-fail (R3b 启用时)/high-row-scroll/partial-import (R3b 启用时)。
- [Affects R5, R16][Resolved 2026-07-17] R5 阶段 2 = OQ-16 `manualSubItems.size` 在 §3 段的语义重新推导（= 子项数组长度，非 section 数）；38 演示值重放策略 = phase 5 OQ-16 公式重新推导后实时测试验证。
- [Affects R4b][Resolved 2026-07-17] dirty 锚点 = 弹层层「上次入栈的子项集合」；每行失焦即存；dirty 三按钮 = Save Draft & Close / Discard & Close / Cancel（互斥）；3 秒撤销仅针对单条删除。
- [Affects R7, R15a, R16][Resolved 2026-07-17] content 允许嵌入 `{{var}}`；子项内 `{{var}}` 不计入 `manualSubItems.size`，仅作为渲染期可消费内容；子项计数与是否断引用无关。
- [Affects R7a][Resolved 2026-07-17] 高亮色 / 字号完全复用 §4-§7 已选 PAR；未定义变量名容错 = 黄色提示；§7 切换 PAR 后 §3 已写入引用的断引用视觉信号 = 黄色提示，需 SectionCard.vue / ManualSubItemModal.vue 之间响应式 prop sync。
- [Affects R9][Resolved 2026-07-17] 软上限 = 200 条 + UI 黄色提示 + 强制启用虚拟滚动 (pageSize=50)；硬上限 = 500 条 + 前端拒绝新增；具体数字 sparring 会议可微调。
- [Affects R4b][Resolved 2026-07-17] dirty 三按钮文案已锁定（见 [Affects R4b] 上方）；跨设备冲突兜底 = last-write-wins + 黄色提示「§3 在另一设备上被修改」。
- [Affects R14][Resolved 2026-07-17] 前端全局 Toast 组件状态 = 待 U2 实施期间 verify（origin doc plan §Outstanding Questions 推荐：「先 verify 后再用」）；若已存在则复用，若不存在则 §3 弹层需单独引入最小骨架。
- [Affects R13][Resolved 2026-07-17] 「全部保留」分支不作为 sparring 必答（按 R13 跟随 R1 决议：列表式保留 ⊕ + ✎；中央 Dialog 全部隐藏；抽屉式 ≥1440 px 保留 ⊕）；按钮组 Toast 复用项目全局 Toast 组件（与 R14 对齐）。
- [Affects R2][Resolved 2026-07-17] §3 弹层入口位置 = 卡片标题旁按钮（与 VariableBindingModal 触发一致 + 与现有「+ 新增手动子项」按钮相同入口样式），评估维度按打分：① 视觉显著（-2..2）+ ② 与现有按钮一致（是）+ ③ 与 VariableBindingModal 触发一致（是）= 推荐结论：标题旁按钮显「手动子项（N 项）」显示当前子项数。
- [Affects R15b][Resolved 2026-07-17] `{{var}}` 命名空间归属 = `{section.sectionIndex}.{varName}` 局部约定（Plan A 实施）；§4-§7 varBindings 字典在 §3 渲染阶段的可见性 = 全集传入 §3；§7 PAR 解除后的 §3 已写入 `{{var}}` 兜底 = 黄色提示。R15b 拍板翻转走 Plan B fallback。
- [Affects R17a][Resolved 2026-07-17] 后端落库失败兜底文案 = 「操作成功但同步失败，请刷新」（默认按 origin 决议）；U5 实施期间可评估是否补充「重新提交 / 复制草稿」等兜底动作。

### Deferred to Planning

- [Affects R7][Technical] `{{var}}` 与变量绑定 PAR 的实际链路验证：sparking 决议允许嵌入时，前端实时预览链路是否需要在 §3 与 §4-§7 之间建变量索引，验证 SectionCard.vue 的渲染时序。
- [Affects Implementation][Needs research] 高行数（≥R9 软上限）列表式一次渲染的浏览器性能：DOM 节点数 / 滚动卡顿 / 输入响应延迟，作为性能压测项。R9 拍板后强制启用 Element Plus `el-table-v2` / 虚拟滚动 / 分页（pageSize=50）。
- [Affects Implementation][Needs research] 弹层表格批量粘贴解析失败时的兜底文案与回退策略。
- [Affects R1-implementation-bridge, R5-阶段-2][Needs research] **ManualSubItems prop 类型迁移工作量清单**：从 `string` 升级为 `Array<{title, content, value?, unit?, lower_bound?, upper_bound?, range_type?}>` 四端工作量评估：① 前端 `SectionCard.vue`（prop + emit 改写 + 当前 textarea 替换为弹层）② 前端父 `ComposerView.vue`（响应式 watch 链重接 + `renderedHtml` 数据形态契约）③ 后端 `ComposerController.parseContext`（反序列化逻辑改写）④ 后端 `ComposerRenderService.mergeSectionContext`（each-block 替换语义从整段 string 替换改写为逐条拼接，R5 业务字段是否计入需联动 R7 决议）+ 存量数据迁移脚本设计 + localStorage 草稿兼容策略。R5 阶段 2 决策输入。
