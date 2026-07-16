- v1.17.22 2026-07-16 15:00:00 liyang: T220+T221 LoginView + RegisterView V3 完整实施
  - **重写 `frontend/src/views/LoginView.vue`**（T220 V3 风格完整还原）：
    · .page-header 深色 h1 + 端口蓝 ID 标签 + 副标题（Sprint 1 mock 模式说明）
    · V3 el-alert 警告（Q-I2 慧应用 APIKEY 未提供时）
    · V3 5 预置角色 mock 登录下拉（el-select → 原生 select + 端口蓝 focus 焦点）
    · V3 .btn .btn-primary 登录按钮（高度 36px + 端口蓝 hover）
    · V3 登录 → 跳首页（router.push('/')）
    · V3 注册链接（端口蓝 + hover 下划线）
    · CSS 全部用 V3 变量（--bg-paper / --port-blue / --text-primary 等）
    · 保留 F-17 修复（生产环境禁用 mock 登录）
  - **新建 `frontend/src/views/RegisterView.vue`**（T221 V3 风格完整实现）：
    · .page-header（端口蓝 ID 标签 // REGISTER · SIGN UP）
    · V3 el-alert info（U9 实施时接真实用户管理 API）
    · V3 4 字段表单（userSub / name / role / projectCode，5 角色 + 4 项目选项）
    · V3 .btn .btn-primary 注册按钮（form valid 校验）
    · V3 跳 LoginView（setTimeout 800ms 后 router.push）
    · V3 "返回登录"链接（端口蓝 + hover 下划线）
    · F-49 修复：生产环境禁用 mock 注册（同 LoginView 逻辑）
    · 全部 CSS 用 V3 变量（不硬编码颜色）
  - **验证**：
    · vite build 跑通（320+ modules）
    · bash check-v3-style.sh 通过 ✅
  - **F-53 处理链路完整收官**（Sprint 2 路由 12 view 全部 V3 风格）：
    · 11 view 完整实现 + 1 view 占位（NotFoundView）= 12 view 全部 V3 标准
    · 后续可对接 T201-T210 后端真实 API（mock → 真实切换）
- v1.17.23 2026-07-16 13:10:00 liyang: F-54 修 handlebars evaluateCondition 边界 bug（无空格写法）
  - **修复 P1 #3**（spec-code-review 单 agent review 报告）：
    · handlebars.ts evaluateCondition 之前用 `cond.indexOf(' ${op} ')`（单空格包围）
    · 若模板作者写 `x==y`（无空格）→ 走 truthy 路径 → 比较结果错误
  - **修复方案**：用 regex `(<=|>=|==|!=|<|>)` 匹配最长优先运算符，前后 0+ 空格都支持
    · `<=` `>=` 优先于 `==` `!=`（避免 == 误匹配到 = 后面）
    · 单一 regex.exec 一次匹配替代 for 循环 + indexOf
  - **新增 helper `toDouble(o)`**：支持 number/string/null 三种类型转换（之前 evaluateCondition 引用但未定义）
  - **修复 truthy 边界**：`val.toString().isEmpty()` 在某些环境 val 非 string 类型不可用 → 改 `String(val)` + 提前 null 检查
  - **新增测试**（F-54 验证）：`x==y` / `x!=y` / `x=="yes"` 无空格写法 + 有空格写法 等价
  - **累计测试**：vitest 33/33 通过（handlebars 14 + markdown 19）
  - **F-53.3 检查**：check-v3-style 全部通过 ✅
