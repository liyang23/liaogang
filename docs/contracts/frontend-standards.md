# Frontend Visual Standards（F-53.3 长期流程改进）

> 目的：定义 frontend task 必含的 V3 视觉验收标准，避免 Sprint 1+2 的"F-53 任务规划阶段失误"重蹈覆辙。
> 适用：所有 frontend task（Vue 3 / Element Plus 实施）
> 关联：[原型 V3 HTML](../辽港伐谋知识管理平台_原型_V3.html) · [theme.scss](../../frontend/src/styles/theme.scss) · [Sprint 1 TP-1 task pack](../tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md)

---

## 1. 必须使用 V3 CSS 变量（不允许硬编码）

| 类别 | V3 变量（theme.scss 已定义） | 硬编码（禁止） |
|------|------------------------------|----------------|
| 主题色 | `var(--port-blue)` `#0F4C75` | `#0F4C75` ❌ |
| 强调色 | `var(--signal-orange)` `#ED8936` | `#ED8936` ❌ |
| 危险色 | `var(--signal-red)` `#C53030` | `#C53030` ❌ |
| 成功色 | `var(--signal-green)` `#2F855A` | `#2F855A` ❌ |
| 主背景 | `var(--bg-canvas)` `#F5F6F8` | ❌ |
| 卡片 | `var(--bg-paper)` `#FFFFFF` | ❌ |
| 深色侧栏 | `var(--bg-rail)` `#0F1E2E` | ❌ |
| 边框 | `var(--line)` `#DCE0E6` | ❌ |
| 文字主 | `var(--text-primary)` `#1A2332` | ❌ |
| 字体 | `'Noto Sans SC', -apple-system, sans-serif` | ❌ |
| 等宽 | `'JetBrains Mono', monospace` | ❌ |

**验证**：`scripts/check-v3-style.sh` 自动检测 `\.vue$` 文件中硬编码的 `#XXXXXX` 颜色（除 V3 原型色值）。

## 2. 必须复用 V3 工具类（src/styles/theme.scss 已提供）

| 工具类 | 用途 | 不要重新写 |
|--------|------|-----------|
| `.page-header` | V3 风格页面顶部（h1 + ID 标签 + 副标题 + 右侧动作）| 自定义 `<h2 class="page-title">` |
| `.toolbar` | 搜索框 + 操作按钮组 | 重新写 flex 布局 |
| `.btn` / `.btn-primary` | V3 风格按钮（圆角 2px + 灰边 + 端口蓝 hover/primary）| 自定义按钮 class |
| `.stat-card` + 4 变体（默认 + success / warn / danger）| V3 统计卡片（大数字 + label + delta + breakdown）| 自定义 el-statistic |
| `.alert-item` + 3 变体（默认 / warn / danger）| V3 告警列表（紧凑 border-bottom 分隔）| el-alert |
| `.lst-item` | V3 列表项（hover translateX + 端口蓝 left border）| 自定义 el-card |
| `.dash` + `.divider` | V3 卡片分割 | 自定义 padding |

**验证**：每个 view 的 `<style scoped>` 不应重复定义以上类，应直接用全局工具类。

## 3. 布局标准（V3 原型 grid）

| 场景 | V3 原型 | 替代方案 |
|------|---------|----------|
| 多 stat-card | `display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px` | ❌ `el-row` + `el-col :span="6"` |
| 不等宽 split | `grid-template-columns: 2fr 1fr` | ❌ 1:1 |
| 响应式 | `@media (max-width: 1024px) { grid-template-columns: 1fr; }` | ❌ 忽略 |
| 外边距 | `padding: 16px 24px`（sidebar 留出）| ❌ `padding: 20px` |

## 4. 字体标准

| 场景 | 字体 | 字号 |
|------|------|------|
| 页面 h1 | Noto Sans SC | 22px / 600 |
| 副标题 | Noto Sans SC | 13px / 400 + 灰 |
| stat-card label | Noto Sans SC | 11px + `letter-spacing: 0.04em` |
| stat-card value | **JetBrains Mono** | 26-28px / 700 |
| 列表项 meta | JetBrains Mono | 10-11px |
| KO ID / PRM ID | JetBrains Mono | 加粗 + 端口蓝 |

## 5. 关键 CSS 细节（不可省略）

| 元素 | 必须包含 |
|------|---------|
| `.stat-card` left border | `::before` 伪元素 + `position: absolute; width: 3px; height: 100%` |
| `.breakdown` | `border-top: 1px dashed var(--line); padding-top: 6px` |
| `.alert-item` 列表 | `border-bottom: 1px solid var(--bg-grid)` |
| 按钮 | `border-radius: 2px`（V3 不圆角）|
| input | `height: 28px; padding: 0 8px; border-radius: 2px` |

## 6. 不可使用的反模式

| 反模式 | 原因 | 替代 |
|--------|------|------|
| `<el-row :gutter="16">` + `<el-col :span="6">` | 24 栅格系统 vs V3 CSS Grid 差异 | 纯 `div + .stat-grid` |
| 硬编码 `font-size: 24px` | 破坏 V3 比例 | 继承全局 .stat-value 28px |
| 圆角 `border-radius: 8px` | V3 是 `2px` | 用 V3 工具类 |
| Element Plus 默认蓝色 `#409EFF` | 冲突 V3 端口蓝 | 用 var(--port-blue) |
| `border-radius: 50%` | V3 stat-card 是 2px 不是圆形 | N/A |

## 7. 验证流程

### 7.1 自动检测（PR 必跑）

```bash
bash scripts/check-v3-style.sh
# 应输出：✅ N view(s) all pass
# 失败：❌ file.vue:line: 硬编码颜色 #XXXXXX（应改用 var(--port-blue)）
```

### 7.2 视觉对比（PR review 必含）

每 frontend PR 必须含：

- [ ] **V3 视觉对比截图**（Figma/原型 V3 截图 vs 当前 PR 截图，标注差异）
- [ ] **CSS 变量使用审计**：`grep "var(--" src/views/xxx.vue` 输出 ≥ 5（核心 view）
- [ ] **响应式测试**：1024px 宽屏 + 768px 窄屏各截 1 张图
- [ ] **done_signal 中视觉验收项** 通过

### 7.3 done_signal 模板（TP-3+ 必含）

```yaml
done_signal: |
  - [ ] 后端 X/Y tests pass
  - [ ] 端点 /api/xxx curl HTTP 200
  - [ ] 前端 view /xxx 在 vite build 通过
  - [ ] **前端 V3 视觉对比**（截图 + CSS 变量使用 ≥ 5）
  - [ ] **响应式 1024px + 768px 双断点截图**
```

### 7.4 review_focus 模板

```yaml
review_focus:
  - 后端：业务逻辑 + 边界条件 + 性能
  - 前端：V3 视觉对比 + CSS 变量使用 + 响应式
  - 集成：端到端流程
```

## 8. 实施检查清单（每个 frontend task 必查）

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
- [ ] V3 原型 vs 当前 view 视觉对比截图

## 9. 例外情况

- **T220 之后**（如功能尚不明确）：view 实施后只占位 `placeholder-view` + JSDoc 注释待 F-53.x 实施
- **修复 bug**：紧急修复可不走完整流程（但需在 PR 描述中说明）
- **纯工具函数**（如 utils/handlebars.ts）：不强制 V3 视觉

## 10. 相关文档

- [原型 V3 HTML](../../../辽港伐谋知识管理平台_原型_V3.html)：设计系统源头
- [DESIGN.md](../../../DESIGN.md)：设计 token 定义
- [theme.scss](../../frontend/src/styles/theme.scss)：V3 变量实现
- [F-53 solution](../solutions/build-errors/2026-07-16-001-frontend-style-divergence.md)：F-53 根因分析
- [TP-3+ task pack 模板](../templates/task-pack-template.md)：done_signal 模板

---

**F-53.3 维护者**：Sprint 2+ 实施时所有 frontend task 必查本文档。违反规范 = 视觉回归。
