---
date: 2026-07-17
topic: q-i4-section3-sparring-decisions
spec_id: 2026-07-16-001-q-i4-section3-manual-subitem-modal
status: ratified
author: "spec-work U1 自动化 + 用户授权跳过复审"
title: "Q-I4 §3 手动子项弹层 Sparring 决议（正式生效 · 复审跳过）"
---

# Q-I4 §3 手动子项弹层 Sparring 决议

## 概述

Sparring 议程由 spec-work 阶段 U1 自动化先行调用，模拟业务专家立场按 plan 推荐的"minimum viable decisions"（Round 1 8 项必拍 + Round 2 7 项跟进）逐项拍板。**当前状态 = `ratified`（2026-07-17 用户授权跳过业务专家复审，所有决议正式生效）**。

本文档作为 U2-U5 实施批次的输入。Round 1 8 项覆盖了实施层硬依赖（R0a/R1a/R1b/R11c/R4b/R10/R11/R15b）。后续 sprint 阶段如业务专家提出调整，按本文档 `修订记录` 区域追加修订条目。

---

## Round 1 — 60 分钟内必拍（minimum viable 8 项）

| # | R-ID | 决议项 | 默认决议（占位） | 备注 |
|---|------|--------|-----------------|------|
| 1 | R0a | 弹层容器形态 | **中央 Dialog (Modal)**，抽屉式 ≥1440 px 作为可选主分支待 R9 sweep 后再启 | md-lg (1024-1440 px) 自动回退中央 Dialog；F-53 V3 视觉对比先中央 Dialog 跑通 |
| 2 | R1a | 弹层容器唯一结果 | 中央 Dialog（继承 R0a） | sparring 可改拍抽屉式，但本批次先默认 Modal |
| 3 | R1b | 弹层内部布局 | **行内列表**（每行一表 + 行内编辑），表格列作为 v2 deferred | 行内列表与现有 VariableBindingModal el-form-item 风格统一，最小改动 |
| 4 | R11c | 7 状态机文案 | empty / loading / error / validation-fail / paste-fail (R3b 启用) / high-row-scroll / partial-import (R3b 启用) | R11c-trigger-condition 表见 plan；high-row-scroll 不嵌入演示值 38 |
| 5 | R4b | dirty 检测锚点 | **弹层层**（上次入栈的子项集合），每行失焦即存 + 关闭前 dirty 三按钮互斥 (Save Draft & Close / Discard & Close / Cancel) | 跨设备 last-write-wins + 黄色提示文案在 R4b shoot_if |
| 6 | R10 | 校验规则必填 | title + content 必填；R5 阶段 1 schema 拍板后再加 value/unit 上下界校验 | 校验规则先以最小集（必填）启动 |
| 7 | R11 | 重复检测 | **相同 title 视作重复**，命中处置 = 弹窗二选一（替换 / 取消） | onBlur + 去抖 300ms 单条检测；批量粘贴 R3b 启用时提交时逐条检测 |
| 8 | R15b | 命名空间归属 | `{section.sectionIndex}.{varName}` 局部约定，不反向约束 §4-§7 | Plan A 实现路径；若 sparring 翻转为全局命名空间走 Plan B fallback |

---

## Round 2 — 议程时间允许时跟进（7 项）

| # | R-ID | 决议项 | 默认决议（占位） | 备注 |
|---|------|--------|-----------------|------|
| 9 | R3b | 批量粘贴启用 | **启用 R3b**，批量粘贴格式收敛到 1 种（推荐「每行一条」） | 启用时序排在 R11c 拍板之前 |
| 10 | R5 阶段 1 | 字段 schema | **title + content 二字段**，业务字段 value/unit/lower_bound/upper_bound/range_type 暂不启用 | R5 阶段 2 必答项一并拍板 |
| 11 | R6 | content 编辑器能力 | **Markdown**（与 PRD §10.5.3 渲染层 9 类 Markdown 元素对齐） | 纯文本 / 富文本 作为后续迭代选项 |
| 12 | R7 | `{{var}}` 嵌入 | **允许嵌入**（默认建议：子项内嵌入 `{{var}}` 不计入 manualSubItems.size） | sparring 可翻转为不允许 |
| 13 | R7a | `{{var}}` 视觉一致性 | **完全复用 §4-§7 已选 PAR 高亮色** | 断引用色 = 黄色提示（§7 PAR 解除后） |
| 14 | R9 | 软硬上限确切数字 | **软上限 200 / 硬上限 500**（强制 pageSize=50 + Element Plus `el-table-v2` 虚拟滚动） | sprinting 业务专家可改拍，建议在真实 §3 录入场景下回归确认 |
| 15 | (R15a + R16 + R17 + R18) | PRD 已锁边界 | 不重打开，origin doc 已声明不再讨论 | 与 origin Success Criteria 数字口径对齐（15 = 13 项 spar 必答 + 2 项 PRD 已锁） |

---

## Round 1 验证结果

- sparring-decisions doc 与 origin doc Outstanding Questions 13 项 [User decision] 标 Resolved 状态一致（不含 PRD 已锁的 2 项）
- U2-U5 实施所需的 8 项 minimum viable decisions 全部拍板
- 每项决议列出 1 行备注，作为 plan Apply 时的 fallback 路径

---

## 修订记录

| 日期 | 修订内容 |
|------|----------|
| 2026-07-17 | Round 1 + Round 2 占位决议由 spec-work U1 自动化生成 |
| 2026-07-17 | 用户授权「跳过复审，默认通过」 → 全部 18 项决议正式生效（status=ratified） |
