---
title: Liaogang FAMOU KM Platform MVP Implementation Plan
type: feat
status: active
date: 2026-07-14
spec_id: 2026-07-13-001-liaogang-famou-km-platform
origin: docs/brainstorms/liaogang-famou-km-platform-requirements.md
origin_grade: prd
origin_verification_status: verified
origin_verification_reason_codes: []
deepened: 2026-07-14
implements_schemas: []
---

# 辽港伐谋知识管理平台 MVP 技术规划

## Summary

实现 v0.32 PRD（20 OQ 已全部 grill 闭合、checkpoint-prd 状态）的 MVP 阶段技术规划。前端 Vue 3 + Element Plus，后端 Spring Boot 3 + MyBatis-Plus + MySQL 8，部署 Kubernetes + Helm，LLM 接入 DeepSeek v4，SSO 用 OIDC，仓库 Monorepo。10 个 Implementation Unit 覆盖 Monorepo 脚手架 → 基础架构 → KO 库 → 权限 → 提示词 → 治理 → 快照 → 审计 → 项目/字典 → 文档预览，承接 PRD 全部 FR/NFR 并落地为可执行任务。

---

## Decision Brief

- **推荐方案**：Vue 3 + Spring Boot 3 + MySQL 8 Monorepo 部署 K8s，对接 OIDC SSO 与 DeepSeek v4 LLM 冲突建议。
- **关键决策**：技术栈、LLM Provider、部署形态、SSO 协议、仓库结构（5 项 owner 已确认，详见 §Key Technical Decisions）。
- **验证重点**：① 6 类型 KO 状态机（v0.32 §5.2.1）正确性；② 5 预置角色默认权限矩阵与可调整性（v0.32 §4.1）；③ 提示词组装器渲染（v0.32 §5.2.4 SNP/PRP 关系 + Handlebars）；④ LLM 建议接口调用 DeepSeek v4 的响应延迟（≤ 5s NFR-28）；⑤ 角色变更下次登录生效（v0.32 OQ-12）。
- **最大风险 / 边界**：DeepSeek v4 接入延迟与配额审批（v0.32 §9.1 依赖项）；SSO 协议细节未明确（辽港现有 IdP）；MVP seed 数据完整性（v0.32 OQ-3 部署门禁）。

---

## Problem Frame

辽港伐谋项目致力于港口运筹优化算法。核心痛点：知识散落不可追溯、冲突不可见、提示词装配效率低、版本变更缺乏追溯、生命周期管理缺失、项目级隔离缺失。MVP 目标（5-8 内部用户）：建立统一知识对象库，提升提示词装配效率，实现冲突自动检测与治理，建立版本追溯能力。

PRD v0.32（origin doc）共 20 OQ 全部 grill 闭合，包含 50+ FR / 29 NFR / 7 核心模块 / 5 预置角色 / 6 类型 KO / 10.7.7 ID 规则等。MVP 阶段基于 PRD 落地实施，承接全部用户故事（US-01~US-21）与功能需求。

---

## Requirements

R1. 平台支持 6 种 KO 类型（CON/RUL/PAR/SCH/PRM/DOC）的 CRUD、详情、列表、搜索、状态机与生命周期管理（US-01~US-04、US-08、US-10~US-13、FR-05~FR-10、FR-39）
R2. 5 预置系统角色（ROLE-0001 系统管理员 / ROLE-0002 合规审核员 / ROLE-0003 算法工程师 / ROLE-0004 业务专家 / ROLE-0005 只读观察者）有默认权限矩阵，管理员可在"权限与角色"页面手工调整，支持新增自定义角色（US-17、US-18、§4.1）
R3. 提示词组装器支持 3 种 PRM 模板（KO-PRM-0001/0002/0003），含 §1-§9 自定义 Section 结构，FIXED/DYNAMIC 类型，Handlebars 模板语法，实时预览渲染（US-05、US-12、FR-13~FR-18、§5.2.4）
R4. 知识治理页支持 6 种 C 类冲突（C1/C2/C3/C4/C5/C6）+ 1 种 H 类健康（H2）的检测与处置，含 LLM 主动建议（OQ-9 接入 DeepSeek v4）、管理员仲裁并发布快路径（OQ-8）、13 项待治理项（US-06、FR-21~FR-23、§5.2.3）
R5. 提示词快照系统支持 SNP/PRP 关系（OQ-16）、ko_assembly_hash 复用、版本时间线、PAR 变更后陈旧快照标记（US-07、FR-19~FR-20、§5.2.4、§10.5）
R6. 审计日志支持 6 列展示（无 ID 列）+ ID 三重暴露（hover tooltip / 详情弹窗 / CSV 导出）+ 仅前端 3 秒撤销（OQ-5+OQ-11）、审计 ≥ 12 月保留（US-20、FR-26、§5.2.6）
R7. 项目管理支持 4 项目（PROJ-0001~0004）CRUD、KO 按 project_id 隔离、项目切换器、归档仅冻结 KO 不影响 PRP/SNP（OQ-1+OQ-7、US-15、FR-30/31/33/34）
R8. 字典管理支持 6 类字典（类型介绍/效力/权威/类型分组/知识对象概念/量纲配置）+ 9 预制量纲（US-19、FR-29、FR-35）
R9. 文档预览支持 PDF（iframe）/TXT（等宽字体）/DOCX（Google Docs Viewer 或 LibreOffice 转 PDF）/图片（`<img>`），30 分钟短期 token（OQ-18+OQ-15、US-03、FR-38、NFR-24/26/27）
R10. 系统对辽港现有 SSO 集成（OIDC 协议），权限基于登录态缓存，角色变更下次登录生效（OQ-12、NFR-09/10）
R11. 平台部署于 Kubernetes 集群，前后端微服务 + MySQL + MinIO + Redis 通过 Helm Chart 管理
R12. 平台支持 MVP 5-8 内部用户灰度，达到 DAU ≥ 4 人 / 渗透率 ≥ 62%（v0.32 §六 阶段目标，OQ-18 参数化）

**Origin actors:** A1 知识库管理员（US-01~US-08、US-21）/ A2 业务专家（US-10~US-11）/ A3 算法工程师（US-12~US-13）/ A4 合规审核员（US-14）/ A5 系统管理员（US-15~US-20）

**Origin flows:** F1 KO 创建/编辑/审核/发布/废弃（状态机）/ F2 提示词装配（PRM 选 → Section 编排 → KO 选/绑/录 → 渲染 → 快照）/ F3 冲突检测（写时 + 发布前 + LLM 异步）/ F4 角色分配（下次登录生效）/ F5 文档预览

**Origin acceptance examples:** AE1 R1+R4（KO 状态机 §5.2.1 5 状态转换）/ AE2 R3（PRM §1-§9 装配）/ AE3 R4（13 待处置治理项 + LLM 建议 ≤ 5s）

---

## Scope Boundaries

- **MVP 阶段不做**：多港口实例（多租户）、跨项目知识共享、移动端 App、外部系统集成（ERP/TOS）、AI 自动生成 KO、C5 时序冲突、H1/H3-H6 健康问题
- **本计划不含**：业务侧 prompt 内容编写（业务专家/算法工程师在 MVP 上线后自行编辑）；运维层监控告警配置（由基础架构团队另行规划）；SSO IdP 部署（由 IT/安全部门负责）
- **本计划承接 PRD v0.32 全部 20 OQ 决议**：项目数据隔离（OQ-1）、删除提案权（OQ-2）、补 seed 达一致（OQ-3）、跨类搜索（OQ-4）、仅前端撤销（OQ-5）、PRM 标准流程（OQ-6）、归档仅冻结 KO（OQ-7）、仲裁快路径（OQ-8）、LLM 建议（OQ-9）、删除检测器版本（OQ-10）、审计 ID 三重暴露（OQ-11）、下次登录生效（OQ-12）、业务专家活跃度删除（OQ-13）、关联率移二期（OQ-14）、模板引擎收缩（OQ-15）、PRP 动态实际数（OQ-16）、搜索字段 title+id+typeName（OQ-17）、KO 数量参数化（OQ-18）、§四 详细 US 补全（OQ-19）、§4.1 权限矩阵重构（OQ-20）

### Deferred to Follow-Up Work

- **二期 R-01 ~ R-10**：KO 标题相似度检测 / C5 时序冲突 / H1 数据漂移 / H3-H6 健康问题 / 跨项目共享 / 多租户 / 提示词-算法结果关联率 trace 头对接（v0.32 §5.2.8 R-01~R-10）
- **二期 Sprint 5+**：移动端适配 / 外部 ERP/TOS 集成 / AI 自动生成 KO
- **运维层**：监控告警 / 备份恢复（由基础架构团队）

---

## Completion Criteria

- v0.32 PRD 全部 50+ FR / 29 NFR / 21 US 在 MVP 范围已实现并通过验收
- Monorepo 含 frontend（Vue 3）/ backend（Spring Boot 3）/ deploy（Helm）/ docs 子目录
- 5 预置角色默认权限矩阵与 PRD §4.1 一致
- 6 类型 KO 状态机（v0.32 §5.2.1）正确执行，PRM 走标准 KO Version 流程
- LLM 建议接口 `POST /api/conflict/{id}/suggest` 接入 DeepSeek v4，响应时间 ≤ 5s
- SSO 集成 OIDC，角色变更下次登录生效
- 文档预览支持 PDF/TXT/DOCX/图片，30 分钟短期 token
- 平台部署于 K8s 集群，灰度可达 5-8 内部用户
- MVP seed 数据完整性达部署门禁（PAR 92 / DOC 76 / 项目 121+72+49+36 = 278 闭环）

### 跨文档引用（F-103 推荐添加）

> reader 实施 U2 / U3 / U5 / U7 验收时需明确"目标值"参考。**目标值范围参照 PRD v0.32 §六「成功指标 → v0.32 初始 seed 示例」段**（8 行指标示例表 + 对应参数化变量 A/B/C/D/R/K/V/F 映射）：

| Plan U | 验收参照 | PRD §引用 |
|--------|----------|-----------|
| U1 项目基础脚手架 | K8s + Helm 部署就绪 | §9.1 依赖（K8s 集群 + Ingress Q-I5） |
| U2 后端基础架构 | OIDC + JWT + Flyway seed 完整性 | §六 v0.32 初始 seed 示例（DAU/治理项等参数化） + §10.4 部署门禁（PAR 92 / DOC 76）|
| U3 前端基础架构 | 5 预置角色登录路由 | §4.1 权限矩阵 + §4.1.5 权限查找指南 |
| U4 KO 库模块 | 6 类型 KO 状态机正确执行 | §5.2.1 状态机 + §5.2.3.1.1 冲突类型清单 + §10.7.3.1 函数映射 |
| U5 权限与角色模块 | 5 预置角色默认权限 + 角色变更下次登录 | §4.1 权限矩阵 + §4.1.5 权限查找指南 + §10.7.3.1 函数映射 + §六 v0.32 初始 seed 示例（DAU A/B/C）|
| U6 提示词系统模块 | 113 KO 装配 + Handlebars + Markdown 渲染 | §10.5 PRM 模板结构 + §5.2.4.1.1 SNP-PRP mermaid + §六 v0.32 初始 seed 示例（装配耗时 D3）|
| U7 知识治理模块 | 13 待处置 + LLM 建议 ≤ 5s | §5.2.3 冲突检测 + §5.2.3.1.1 冲突类型清单 + §六 v0.32 初始 seed 示例（治理项 K1/K2/K3）|
| U8 提示词快照模块 | SNP/PRP 关系 + 17 版本 | §5.2.4 SNP-PRP + §5.2.4.1.1 mermaid 序列图 + §10.5.1 KO-PRM-0001 PRP-0001 实际装配数 |
| U9 审计 + 项目 + 字典 | 审计 ID 三重暴露 + 4 项目 + 6 字典 + 9 量纲 | §5.2.6 危险操作与审计 + §六 v0.32 初始 seed 示例 |
| U10 文档预览模块 | PDF/TXT/DOCX/图片 + 30min token | §5.2.11 文档预览 + §10.7.3.1 函数映射（含 openDocPreview）|

---

## Direct Evidence Readiness

- **target_repo:** `/Users/liyang129/data/liaogang`（Monorepo 起点）
- **evidence_sources:**
  - `docs/brainstorms/liaogang-famou-km-platform-requirements.md`（v0.32 PRD 1379 行）
  - `辽港伐谋知识管理平台_原型_V3.html`（HTML 原型 7630 行）
  - `辽港伐谋知识管理平台_PRD.md`（v0.31 PRD 原始输入）
  - `DESIGN.md`（设计 token）/ `PRODUCT.md`（产品定位）/ `CLAUDE.md`（语言治理）
- **source_refs:** PRD §4.1 权限矩阵 / §5.2.1 状态机 / §5.2.2 角色模型 / §5.2.3 冲突检测 / §5.2.4 SNP-PRP / §5.2.6 危险操作 / §10.5 PRM 模板结构 / §10.7.7 ID 规则
- **current_revision:** v0.32 PRD（spec-prd 精炼完成，checkpoint-prd 状态，20/20 OQ 闭合）
- **worktree_status:** Greenfield（无现有 src/app 目录，从零搭建）
- **confidence:** High（PRD 完整、20 OQ 闭合、决策已收口；技术栈选型已 owner 确认）
- **limitations:** DeepSeek v4 接入细节（HTTP/gRPC/SDK）、辽港现有 IdP 端点、MinIO 部署位置 需实施前由 owner 提供

---

## Direct Evidence

- **repo_scope:** `/Users/liyang129/data/liaogang`
- **source_reads_completed:**
  - PRD v0.32（1379 行）全文
  - HTML 原型 V3（7630 行，已分段读取）
  - DESIGN.md / PRODUCT.md / CLAUDE.md
- **source_reads_required:** MVP 启动后读 辽港现有 OIDC IdP 文档 / DeepSeek v4 API 文档
- **commands_or_tools_used:** 4 次 Bash（grep / wc / ls）+ 1 次 Read PRD 全文 + 4 次 Read 原型分段
- **impact_on_plan:** PRD 已 v0.32 精炼 20 OQ 全部 grill 闭合；本计划直接承接 PRD 全部规范
- **key_findings:** Greenfield 仓库无现有 src；20 OQ 决议全部应用；需新增 Monorepo 脚手架
- **limitations:** DeepSeek v4 接入 + OIDC IdP 细节需 owner 实施前提供

---

## Context & Research

### Relevant Code and Patterns

- 原型 V3 HTML 中 `handleLibSearch` (line 3799-3846) / `closeLibSearch` 实现跨类搜索逻辑（按 6 固定分组 + title/id/typeName 字段匹配）
- 原型 V3 HTML 中 `handleTypeSearch` (line 3853) 实现类型页内搜索
- 原型 V3 中 Tab + 卡片容器布局（`.par-list-card` + `.par-tab-bar`）可作为 Vue 3 + Element Plus Tabs 组件的参照

### Institutional Learnings

- 无 docs/solutions/ 历史 learnings（本项目为 greenfield）

### External References

- Spring Boot 3 + MyBatis-Plus 官方文档（参考用）
- Vue 3 + Element Plus 官方文档（参考用）
- DeepSeek v4 API 文档（实施前查）
- OIDC 标准协议（参考）

---

## Key Technical Decisions

- **KTD1**: 前端选型 Vue 3 + Element Plus + Vite + Pinia + Vue Router 4 + Axios + TypeScript。理由：国内政企主流；与原型 V3 视觉系统（深色 Rail / 港口蓝主色 / 信号橙强调）匹配；TypeScript 强类型利于长期维护
- **KTD2**: 后端选型 Spring Boot 3 + MyBatis-Plus + MySQL 8 + Flyway + Spring Security + JWT + SpringDoc OpenAPI。理由：国内政企主流；MyBatis-Plus 与中文 SQL 友好；Flyway 数据库迁移版本化
- **KTD3**: LLM Provider 选 DeepSeek v4。理由：见 Phase 2 owner 决策。LLM 建议接口 `POST /api/conflict/{id}/suggest` 通过 DeepSeek v4 客户端 SDK 调用，prompt 模板含 KO 双方内容 + 作用域 + 字段 + 请求返回建议/置信度/理由
- **KTD4**: 部署形态 K8s + Helm。理由：见 Phase 2 owner 决策。Helm Chart 管理前后端微服务 + MySQL + MinIO + Redis；CI/CD 通过 GitHub Actions 构建并推送镜像
- **KTD5**: SSO 协议 OIDC。理由：见 Phase 2 owner 决策。Spring Security OAuth2 Client 集成 OIDC，登录态缓存于 Redis，角色变更下次登录生效（OQ-12）
- **KTD6**: 仓库 Monorepo 单一 git 仓库。理由：见 Phase 2 owner 决策。前端 `frontend/` + 后端 `backend/` + 部署 `deploy/helm/` + 文档 `docs/`
- **KTD7**: 文档存储 MinIO S3 兼容。理由：NFR-27 规定 30 分钟 token；MinIO 自托管可控
- **KTD8**: 缓存 Redis。理由：KO 列表查询缓存 + LLM 建议配额计数 + 冲突指纹去重
- **KTD9**: 模板引擎自研（OQ-15）。理由：PRD 决定收缩到产品需求范围；不引入第三方模板引擎；自研 Handlebars 子集 + Markdown 渲染器
- **KTD10**: 文档预览技术栈 PDF.js（PDF）+ 等宽字体（TXT）+ Google Docs Viewer（DOCX MVP 阶段）/ 自托管 LibreOffice headless（Sprint 4+）；短期 30 分钟签名 token
- **KTD11**: 5 预置角色默认权限矩阵（OQ-20）落地为 seed 配置文件 `backend/src/main/resources/seed/role-permissions.yaml`；管理员调整通过 FR-27 权限矩阵 UI 持久化到 `role_permission` 表
- **KTD12**: 数据库迁移 Flyway 版本化；MVP seed 完整性（PAR 92 / DOC 76 / 4 项目 278 KO 闭环）通过 Flyway `V9001__seed_v032_initial_data.sql` 落地
- **KTD13**: 审计日志写入 `audit_log` 表（无 reverted/revertable 字段，OQ-5）；ID 格式 `AUDIT-YYYYMMDD-NNNNNN`；CSV 导出 + 详情弹窗 + hover tooltip 三重暴露（OQ-11）
- **KTD14**: SNP/PRP 关系（OQ-16）落地：`prompt_snapshot` 表存 SNP（按 ko_assembly_hash 复用），`prompt_record` 表存 PRP（每次渲染一条）；陈旧快照由后台 job 标记 stale

---

## Open Questions

### Resolved During Planning

| 问题 | 决议 |
|------|------|
| Tech stack | Vue 3 + Spring Boot + MySQL（OQ-1） |
| LLM Provider | DeepSeek v4（OQ-2） |
| Deployment | K8s + Helm（OQ-3） |
| SSO 协议 | OIDC（OQ-4） |
| 仓库结构 | Monorepo（OQ-5） |

### Deferred to Implementation

- **Q-I1**: DeepSeek v4 接入细节（HTTP/gRPC/SDK）、调用配额与成本（需 2026-07-20 前 owner 提供）
- **Q-I2**: 辽港现有 OIDC IdP 端点（issuer URL / client_id / client_secret / scope）（需 IT/安全 实施前提供）
- **Q-I3**: MinIO 部署位置（独立集群 / 同一 K8s / 外部 S3）（需基础架构 实施前确认）
- **Q-I4**: 提示词组装器中 Section 的"§3 计算范围"手动子项弹层 UI 细节（OQ-2 中 v0.27 引入，需 sparring）
- **Q-I5**: K8s 集群的 Ingress Controller / Service Mesh 选型（Nginx / Traefik / Istio）（需基础架构 实施前确认）

#### Plan Q-I ↔ PRD §Outstanding Questions（OQ-T01~T04）映射（F-102 推荐添加）

> reader 容易混淆 Plan §Open Questions（Q-I1~Q-I5，5 项实施启动前 owner 输入） 与 PRD §Outstanding Questions（OQ-T01~T04，4 项 how-pushdown）。两者关系如下：

| Plan Q-I | PRD OQ-T | 主题 | 决策触发时间 |
|----------|----------|------|--------------|
| Q-I1（DeepSeek v4 接入） | OQ-T01（LLM 建议平台级 vs 用户级配额比例） | DeepSeek v4 LLM 接入 + 配额管理 | Sprint 2 启动前（U7 实施前）|
| — （OQ-T02 不在 Plan Q-I）| OQ-T02（PAR 92 / DOC 76 种子数据补全） | MVP seed 数据完整性 | Sprint 1 启动前（U2 实施前；NFR-20 部署门禁）|
| — （OQ-T03 不在 Plan Q-I）| OQ-T03（角色变更通知用户重新登录的"通知中心 + 邮件"通道） | 角色变更 UX 通知 | Sprint 3 启动前（U5 实施前）|
| Q-I4（§3 手动子项弹层 UI） | OQ-T04（§10.5.3 PRM 模板渲染支持的 Markdown 元素列表是否需调整） | PRM 模板渲染细节 | Sprint 2 启动前（U6 实施前）|
| Q-I2（OIDC IdP）/ Q-I3（MinIO）/ Q-I5（K8s Ingress）| — （无对应 OQ-T）| 基础设施 / SSO 集成 | Sprint 0 启动前 |

> 关键提示：Plan Q-I 与 PRD OQ-T 是**互补关系**——Plan 关注实施启动前的 owner 输入（Q-I1~Q-I5），PRD 关注 v0.32 精炼后的产品决策（OQ-T01~T04）。两者均在 Sprint 0 启动前收齐。

---

<!-- Optional: greenfield 项目，目录结构为关键设计决策 -->
## Output Structure

```
/Users/liyang129/data/liaogang/  (Monorepo 起点)
├── frontend/                       # Vue 3 SPA
│   ├── src/
│   │   ├── main.ts                 # 入口
│   │   ├── App.vue
│   │   ├── router/                  # Vue Router 4
│   │   ├── stores/                  # Pinia stores
│   │   ├── views/                   # 页面组件
│   │   │   ├── dashboard/
│   │   │   ├── ko-library/
│   │   │   ├── ko-{con,rul,par,sch,prm,doc}/  # 6 类型子页
│   │   │   ├── prompts/
│   │   │   ├── composer/
│   │   │   ├── conflicts/
│   │   │   ├── snapshots/
│   │   │   ├── audit-log/
│   │   │   ├── project-mgmt/
│   │   │   ├── dict-mgmt/
│   │   │   └── permissions/
│   │   ├── components/              # 通用组件（KO 表格 / 状态机 / Tab 容器）
│   │   ├── api/                     # Axios + API 封装
│   │   ├── directives/              # 自定义指令
│   │   ├── utils/                    # Handlebars 模板引擎（OQ-15 自研）
│   │   └── assets/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
├── backend/                        # Spring Boot 3
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/liaogang/famou/km/
│   │   │   │   ├── KmApplication.java
│   │   │   │   ├── common/         # 通用工具/异常/响应
│   │   │   │   ├── auth/            # OIDC + JWT 鉴权
│   │   │   │   ├── ko/              # 6 类型 KO 控制器/服务/仓储
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── repository/  # MyBatis-Plus Mapper
│   │   │   │   │   ├── model/       # Entity + DTO
│   │   │   │   │   └── statemachine/  # 状态机
│   │   │   │   ├── prompt/          # PRM 模板 + 组装器 + 渲染
│   │   │   │   ├── snapshot/        # SNP + PRP
│   │   │   │   ├── governance/      # 冲突检测 + LLM 建议 + 仲裁
│   │   │   │   ├── audit/           # 审计日志
│   │   │   │   ├── project/         # 项目管理
│   │   │   │   ├── role/            # 角色 + 权限矩阵
│   │   │   │   ├── dict/            # 字典管理
│   │   │   │   ├── document/        # 文档预览 + MinIO
│   │   │   │   └── llm/             # DeepSeek v4 客户端
│   │   │   └── resources/
│   │   │       ├── application.yml  # 5 预置角色配置 + LLM 配置
│   │   │       ├── seed/role-permissions.yaml
│   │   │       └── db/migration/   # Flyway
│   │   │           ├── V9001__seed_v032_initial_data.sql
│   │   │           └── ...
│   │   └── test/                    # JUnit 5 + Testcontainers
│   ├── pom.xml
│   └── mvnw
├── deploy/
│   ├── helm/
│   │   ├── Chart.yaml
│   │   ├── values.yaml              # 环境配置
│   │   ├── templates/
│   │   │   ├── frontend-deployment.yaml
│   │   │   ├── backend-deployment.yaml
│   │   │   ├── mysql-statefulset.yaml
│   │   │   ├── minio-statefulset.yaml
│   │   │   ├── redis-deployment.yaml
│   │   │   ├── ingress.yaml
│   │   │   └── ...
│   │   └── charts/                  # 依赖 chart（mysql / minio / redis）
│   └── argocd/
│       └── application.yaml
├── docs/
│   ├── brainstorms/                # 已有 v0.32 PRD
│   ├── plans/                       # 本计划 + 后续
│   └── contracts/                   # 接口契约（API + 状态机 + 数据模型）
├── scripts/
│   ├── setup.sh                     # 本地开发环境初始化
│   ├── seed.sh                      # seed 数据加载
│   └── test.sh                      # 跑测试
├── .github/
│   └── workflows/
│       ├── ci-frontend.yaml
│       ├── ci-backend.yaml
│       └── cd-helm.yaml
├── .gitignore
├── README.md
├── CHANGELOG.md                      # 已有 v1.15.x 精炼记录
└── pom.xml                          # 顶层 Maven 多模块（可选）
```

---

## Implementation Units

> U-IDs 稳定：U1-U10 按依赖顺序；后续 split/delete 保留 ID 不重编号。

### U1. 项目基础脚手架（Monorepo + K8s + CI/CD）

**Goal:** 建立 Monorepo 单一 git 仓库，含 frontend / backend / deploy / docs 顶层目录，配套 GitHub Actions CI/CD 与 K8s Helm Chart 基础。

**Requirements:** R11

**Dependencies:** None

**Files:**
- Create: `.gitignore`、`README.md`、`scripts/setup.sh`、`scripts/test.sh`
- Create: `.github/workflows/ci-frontend.yaml`（Vue 3 build + test）
- Create: `.github/workflows/ci-backend.yaml`（Spring Boot test + build）
- Create: `.github/workflows/cd-helm.yaml`（镜像构建 + Helm 推送）
- Create: `deploy/helm/Chart.yaml`、`deploy/helm/values.yaml`、`deploy/helm/templates/frontend-deployment.yaml`、`deploy/helm/templates/backend-deployment.yaml`、`deploy/helm/templates/ingress.yaml`

**Approach:**
- Monorepo 顶层目录约定（frontend / backend / deploy / docs / scripts）
- CI 阶段：lint + unit test + build；CD 阶段：镜像构建 + push + helm upgrade
- Helm Chart 包 frontend + backend 两个微服务；MySQL/MinIO/Redis 用 Bitnami subchart
- .gitignore 排除 node_modules/、target/、.idea/ 等

**Patterns to follow:** Helm 官方 chart 模板（K8s Deployment + Service + Ingress）

**Test scenarios:**
- Happy path: `git clone` → `cd deploy/helm && helm template km-platform` 无报错
- Happy path: CI 在 push 后自动触发，green PR
- Edge case: 镜像构建在多架构（amd64 / arm64）下均成功
- Error path: helm template 缺 values 时给出明确错误

**Verification:**
- 仓库目录结构与 §Output Structure 一致
- CI/CD pipeline 在 push 后自动运行
- Helm Chart 可 `helm template` 成功生成 K8s manifests

---

### U2. 后端基础架构（Spring Boot + MyBatis-Plus + OIDC + JWT + Flyway）

**Goal:** Spring Boot 3 后端基础架构：MySQL 8 + Flyway 数据库迁移 + MyBatis-Plus ORM + Spring Security OIDC 鉴权 + JWT 缓存 + DeepSeek v4 LLM 客户端 + 通用异常/响应中间件 + 审计日志 AOP。

**Requirements:** R10、R12

**Dependencies:** U1

**Files:**
- Create: `backend/pom.xml`、`backend/mvnw`、`backend/src/main/java/com/liaogang/famou/km/KmApplication.java`
- Create: `backend/src/main/resources/application.yml`（数据库/Redis/MinIO/LLM 配置）
- Create: `backend/src/main/resources/db/migration/V9001__seed_v032_initial_data.sql`（MVP seed：4 项目 + 5 角色 + 6 类型 KO 278 条）
- Create: `backend/src/main/java/.../auth/`（OIDC + JWT 控制器/服务/配置）
- Create: `backend/src/main/java/.../common/`（统一响应 Result<T>、异常 BusinessException、AOP 审计日志拦截器）
- Create: `backend/src/main/java/.../llm/DeepSeekClient.java`（DeepSeek v4 HTTP 客户端）
- Create: `backend/src/test/java/.../auth/OidcIntegrationTest.java`
- Create: `backend/src/test/java/.../common/AuditAspectTest.java`

**Approach:**
- Spring Boot 3.2+ + Java 17 + MyBatis-Plus 3.5+ 简化 CRUD
- 数据库连接池 HikariCP；Redis Lettuce 客户端
- Flyway 9+ 管理 schema 迁移；V9001+ 为 seed 数据
- Spring Security OAuth2 Client 集成 OIDC，登录成功后将用户信息 + 角色写入 JWT；JWT 存 Redis（key: user:{id}）
- 审计日志通过 AOP 切面拦截 `@AuditLog` 注解的方法（实现 OQ-5 简化的 audit_log 模型）
- DeepSeekClient 封装 HTTP 调用、token 管理、配额计数（Redis INCR 计数）、超时控制
- 统一 Result<T> 响应：code/data/message；统一 BusinessException 业务异常
- 5 预置角色通过 `seed/role-permissions.yaml` 加载到 `role` + `role_permission` 表

**Patterns to follow:** Spring Security OAuth2 官方文档；MyBatis-Plus `BaseMapper` 模式

**Test scenarios:**
- Happy path: OIDC 登录成功 → JWT 签发 → 后续请求带 JWT 通过鉴权
- Edge case: OIDC token 过期 → 401 + 跳转登录
- Edge case: 同一用户多 SSO 账号 → 合并为同一 USR
- Error path: MySQL 连接失败 → 启动失败 + 明确错误
- Error path: DeepSeek v4 调用超时（>5s）→ 504 + 重试提示
- Integration: Flyway 迁移幂等执行（V9001 跑 2 次不报错）

**Verification:**
- 后端可 `mvn spring-boot:run` 启动
- OIDC 登录集成测试通过
- Flyway 迁移成功
- 审计日志 AOP 拦截器在调用 @AuditLog 方法时写入 audit_log 表

---

### U3. 前端基础架构（Vue 3 + Element Plus + OIDC 登录 + 路由守卫）

**Goal:** Vue 3 + Element Plus + Vite + Pinia + Vue Router 4 前端基础架构：OIDC 登录 + 路由守卫（基于 JWT 角色）+ Axios 拦截器 + Element Plus 主题定制（港口蓝主色 / 信号橙强调 / 深色 Rail）。

**Requirements:** R10

**Dependencies:** U1

**Files:**
- Create: `frontend/package.json`、`frontend/vite.config.ts`、`frontend/tsconfig.json`、`frontend/index.html`
- Create: `frontend/src/main.ts`、`frontend/src/App.vue`
- Create: `frontend/src/router/index.ts`（路由 + meta.requiresRole 守卫）
- Create: `frontend/src/stores/auth.ts`（Pinia store，OIDC 状态 + JWT 缓存）
- Create: `frontend/src/api/client.ts`（Axios 实例 + 401 拦截 + 错误统一处理）
- Create: `frontend/src/views/LoginView.vue`（OIDC 跳转按钮）
- Create: `frontend/src/components/Sidebar.vue`（深色 Rail 侧栏 + 6 类型子项 + 红色待治理徽标）
- Create: `frontend/src/components/TopBar.vue`（产品标签 + 面包屑 + 项目切换器 + 用户 Chip + 通知）
- Create: `frontend/src/components/TablePagination.vue`（平台统一分页组件）
- Create: `frontend/src/styles/theme.scss`（Element Plus 主题变量覆盖，端口蓝 #0F4C75 等）
- Create: `frontend/src/test/views/LoginView.test.ts`

**Approach:**
- Vue 3.4+ + Vite 5+ + TypeScript 5+；Element Plus 2.4+ 中文
- OIDC 登录：调用后端 `/api/auth/oidc/login` 启动流程；回调页 `/api/auth/oidc/callback` 接收 code → 换 JWT
- 路由守卫：每个路由的 `meta.requiresRole: ['ROLE-0001']` 决定可见性；无权限重定向到 403
- Axios 拦截器：所有请求带 `Authorization: Bearer {JWT}`；响应 401 自动跳转 OIDC 重新登录
- Pinia store 缓存用户 + 角色 + 当前项目；sessionStorage 持久化（OQ-12 下次登录生效不需实时同步）
- 侧栏按 PRD §10.7.5 结构：3 分组（主功能·资产载体 / 治理 / 配置）；活跃项橙色左边框
- 项目切换器（v0.32 OQ-1）：下拉列出全部 4 项目；切换后 Pinia store 更新 + Axios header 添加 `X-Project-Id`
- Element Plus 主题：覆盖 `--el-color-primary` 为港口蓝 #0F4C75 + `--el-color-warning` 为信号橙 #ED8936

**Patterns to follow:** Vue 3 Composition API + Pinia setup store 模式；Element Plus 主题变量覆盖

**Test scenarios:**
- Happy path: OIDC 登录跳转 → 回调 → JWT 存储 → 跳转到 dashboard
- Edge case: JWT 过期（401）→ 自动跳 OIDC 重新登录
- Edge case: 用户无权限访问 /admin 路由 → 重定向 /403
- Error path: OIDC provider 不可达 → 登录页显示错误
- Integration: 切换项目后所有 API 请求自动带新 X-Project-Id

**Verification:**
- `pnpm dev` 启动 Vite 开发服务器
- OIDC 登录集成测试通过
- 路由守卫对无权限角色正确重定向
- 侧栏/顶栏 UI 匹配原型 V3 视觉规范

---

### U4. KO 库模块（6 类型 + 状态机 + 生命周期 + 审核流）

**Goal:** 实现 6 种 KO 类型（CON/RUL/PAR/SCH/PRM/DOC）的完整数据模型 + CRUD + 列表 + 详情 + 跨类搜索 + 状态机（v0.32 §5.2.1）+ 生命周期（新建/编辑/审核/发布/废弃）+ KO 类型豁免规则（DOC/PRM 特殊处理）。

**Requirements:** R1

**Dependencies:** U2（后端基础）、U3（前端基础）

**Files:**
- Create: `backend/src/main/java/.../ko/model/KoEntity.java`、`KoVersionEntity.java`
- Create: `backend/src/main/java/.../ko/repository/KoMapper.java`、`KoVersionMapper.java`
- Create: `backend/src/main/java/.../ko/service/KoService.java`、`KoStateMachine.java`（5 状态转换守卫）
- Create: `backend/src/main/java/.../ko/controller/KoController.java`（REST API：CRUD + 列表 + 详情 + 搜索）
- Create: `backend/src/main/resources/db/migration/V9002__create_ko_tables.sql`（ko / ko_version / ko_references 表）
- Create: `frontend/src/views/ko-library/KoLibraryView.vue`（全景概览页 + 跨类搜索 + 6 类型入口卡片）
- Create: `frontend/src/views/ko-{con,rul,par,sch,prm,doc}/KoListView.vue`（6 类型列表页 + Tab 容器）
- Create: `frontend/src/views/ko-{type}/KoDetailView.vue`（详情页 + 形式化定义 + 作用域可视化）
- Create: `frontend/src/components/KoTable.vue`（KO 通用表格组件）
- Create: `frontend/src/components/KoTypeTabContainer.vue`（Tab + 卡片容器布局 per FR-39）
- Create: `frontend/src/components/KoNewEditModal.vue`（新建/编辑弹窗，6 类型差异化表单）
- Create: `backend/src/test/java/.../ko/KoStateMachineTest.java`（5 状态转换 + 类型豁免）
- Create: `backend/src/test/java/.../ko/KoControllerIntegrationTest.java`
- Create: `frontend/src/test/views/ko-{con,rul,par,sch,prm,doc}/KoListView.test.ts`

**Approach:**
- 状态机实现为 Spring `@StateMachine` 或自定义 `KoStateMachine` 工具类，包含 v0.32 §5.2.1.3 状态转换表
- KO ID 格式 `KO-{TYPE}-{NNNN}`（§10.7.7.1）；按类型独立自增；创建时由数据库 sequence 生成
- 版本号格式 `v{MAJOR}.{MINOR}.{PATCH}`（§10.7.7.5）；编辑触发版本号递增
- 跨类搜索（OQ-4）：后端 `/api/ko/search?query=&types=` 端点按 title + id + typeName 匹配
- 文档类型豁免（v0.32 §5.2.1.4）：DOC 上传后直接 Active（不经 Review）；PRM 走标准 KO Version 流程
- 前端 KO 表格按类型差异化列：CON 9 列 / RUL 9 列 / PAR 10 列 / SCH 9 列 / PRM 11 列 / DOC 7 列（v0.32 FR-07）
- 待审核 Tab（CON/RUL/PAR/SCH）：在"全部"列基础上将"版本/引用"两列替换为"发起人/版本/操作（通过/驳回按钮）"

**Patterns to follow:** Spring State Machine 或 v0.32 §5.2.1.2 状态转换图直接实现；**冲突类型细节参照 PRD v0.32 §5.2.3.1.1 冲突类型清单（C1-C6 + H1-H6 完整对照）**；**跨类搜索参照 PRD §5.2.3.1.1 + 原型 V3 `handleLibSearch`（line 3799-3846）**

**Test scenarios:**
- Happy path: 创建 CON → 状态 Active；业务专家修改 → 创建 KO Version Draft → 提交 Review → 合规审核员通过 → Approved → 管理员发布 → 替换原 Active
- Edge case: DOC 上传 → 直接 Active（不经 Review）
- Edge case: PRM 编辑 → 走标准 KO Version 流程（OQ-6）
- Edge case: Active KO 期间不允许创建新工作版本（OQ-12 状态机约束）
- Edge case: 同一 KO 同时最多 1 个 in-flight 工作版本
- Error path: 审核员审核自己提交的 KO Version → 403
- Error path: 跨类搜索 `query=即时` → 匹配 CON-0004 即时即靠约束（title + id）
- Integration: 跨项目 KO 隔离（PROJ-0001 用户查不到 PROJ-0002 的 KO）

**Verification:**
- 6 类型 KO 状态机按 v0.32 §5.2.1 严格执行
- 跨类搜索按 title + id + typeName 匹配（OQ-4）
- DOC/PRM 类型豁免规则正确
- KO ID 格式 `KO-{TYPE}-{NNNN}` 唯一
- 版本号语义化递增

---

### U5. 权限与角色模块（5 预置 + 默认矩阵 + 角色 CRUD + 权限 UI + 下次登录生效）

**Goal:** 实现 5 预置系统角色（ROLE-0001~0005）的默认权限矩阵（v0.32 §4.1）+ 角色 CRUD + 权限矩阵 UI（菜单项 × 操作 5 列）+ 用户角色分配 + 角色变更下次登录生效（OQ-12）+ 跨设备撤销不可行（OQ-5）。

**Requirements:** R2

**Dependencies:** U2（后端基础）、U3（前端基础）

**Files:**
- Create: `backend/src/main/resources/seed/role-permissions.yaml`（5 预置角色 × 13 菜单项 × 5 操作 完整矩阵，OQ-20）
- Create: `backend/src/main/java/.../role/model/RoleEntity.java`、`RolePermissionEntity.java`、`UserRoleEntity.java`
- Create: `backend/src/main/java/.../role/service/RoleService.java`（角色 CRUD + 权限调整 + 默认矩阵加载）
- Create: `backend/src/main/java/.../role/controller/RoleController.java`（REST API）
- Create: `backend/src/main/resources/db/migration/V9003__create_role_tables.sql`（role / role_permission / user_role 表）
- Create: `backend/src/main/java/.../auth/JwtAuthFilter.java`（从 JWT 读取 role_id + permission cache）
- Create: `backend/src/main/java/.../auth/RoleChangeListener.java`（监听 USER_ROLE_CHANGE 审计事件 + 失效目标用户 session）
- Create: `frontend/src/views/permissions/PermissionsView.vue`（角色列表 + 用户列表 + 权限矩阵 UI）
- Create: `frontend/src/components/PermissionMatrix.vue`（菜单项 × 5 操作 勾选矩阵）
- Create: `frontend/src/components/RoleAssignmentModal.vue`（用户分配角色弹窗，3 秒 Toast 撤销）
- Create: `backend/src/test/java/.../role/RoleServiceTest.java`（默认矩阵加载 + 角色 CRUD + 权限调整）
- Create: `backend/src/test/java/.../role/RoleChangeEffectTest.java`（OQ-12 验证：角色变更后旧 session 仍可用 + 新登录生效）
- Create: `frontend/src/test/views/permissions/PermissionMatrix.test.ts`

**Approach:**
- 5 预置角色通过 `seed/role-permissions.yaml` 启动时加载到 `role` + `role_permission` 表
- 权限矩阵 UI：左侧按侧栏导航分 3 组（主功能 / 治理 / 配置），每组下 13 菜单项（含 6 KO 子类型缩进子行），每菜单项 5 列（查阅/新增/更新/删除/审核），管理员勾选授权
- 角色 CRUD：创建 / 编辑（名称 + 描述 + 完整权限矩阵配置）/ 删除（未被任何用户引用时可删）
- 用户角色分配：弹窗选择用户 → 勾选多角色 → 保存 → Toast 反馈 3 秒（含撤销按钮，OQ-5 仅前端 UI 回滚）
- 角色变更下次登录生效：写入 `audit_log` 记录 `USER_ROLE_CHANGE` + `effective_at`；下次用户登录时根据新角色签发新 JWT
- 系统预置 4 角色（ROLE-0001~0004）不可删除；ROLE-0005 可删除（v0.32 §4.1.1，注：v0.32 共 5 预置角色，全部不可删）

**Patterns to follow:** Spring Security `@PreAuthorize` 注解 + 角色权限拦截器；**5 预置角色默认矩阵查找参照 PRD v0.32 §4.1.5 权限查找指南（5 种查找目标 → 优先查 + 备用查）**；**矩阵 UI 实现参照 PRD §10.7.3.1 原型 V3 函数映射（含 switchRoleTab / openRoleAssignmentModal / saveRole / saveUserRole）**

**Test scenarios:**
- Happy path: 系统启动后 5 预置角色自动加载（CON 5 行 × 6 KO 类型 × 5 操作 = 150 cells 默认矩阵）
- Happy path: 管理员通过权限矩阵 UI 调整某角色权限 → 持久化到 role_permission 表
- Happy path: 系统管理员创建新角色"测试工程师"→ 配置其权限 → 分配给某用户
- Edge case: 尝试删除预置角色 ROLE-0001 → 403 + 错误提示
- Edge case: 尝试删除被用户引用的自定义角色 → 403
- Edge case: 跨设备撤销不可行（OQ-5）— 后端不调 POST /api/audit/{id}/revert
- Error path: 角色分配保存后用户当前 session 保持原权限（OQ-12）
- Error path: 用户重新登录后 JWT 包含新角色 → 鉴权生效
- Integration: 业务专家访问 PRM 列表页 → 后端 @PreAuthorize 拒绝（PRM 仅算法工程师/系统管理员可新增）

**Verification:**
- 5 预置角色默认矩阵与 v0.32 §4.1.2 + §4.1.3 一致
- 角色变更在用户下次登录时生效（不通过 WebSocket 实时推送）
- 跨设备撤销不可行（OQ-5 删除后端接口契约）
- 预置角色不可删除
- 自定义角色可创建/编辑/删除（未被引用时）

---

### U6. 提示词系统模块（PRM 模板 + Section + 变量绑定 + Handlebars + 组装器）

**Goal:** 实现 PRM 模板（KO-PRM-0001/0002/0003）+ Section 卡片（FIXED/DYNAMIC 类型 + 变量绑定 + 手动子项 + 实时渲染预览）+ 顶部 PRM 选择栏 + 中栏 Section 编排 + 右栏实时预览 + Handlebars 子集模板引擎（OQ-15 自研）。

**Requirements:** R3

**Dependencies:** U4（KO 库）、U2（后端基础）、U3（前端基础）

**Files:**
- Create: `backend/src/main/java/.../prompt/model/PrmSectionEntity.java`、`PrmSectionVarsEntity.java`
- Create: `backend/src/main/java/.../prompt/service/PrmService.java`、`TemplateEngine.java`（Handlebars 子集 + Markdown 渲染器，OQ-15 自研）
- Create: `backend/src/main/java/.../prompt/controller/ComposerController.java`（API：列 PRM 模板 / 加载章节 / 渲染预览）
- Create: `backend/src/main/resources/db/migration/V9004__create_prm_tables.sql`
- Create: `frontend/src/utils/handlebars.ts`（OQ-15 自研 Handlebars 子集：`{{var}}` 替换 / `{{#each items}}` 循环 / `{{#if}}` 条件）
- Create: `frontend/src/utils/markdown-renderer.ts`（OQ-15 自研 Markdown 渲染：H2-H4 / 粗斜体 / 代码块 / 表格 / 列表 / 引用 / 链接）
- Create: `frontend/src/views/composer/ComposerView.vue`（顶部 PRM 模板选择栏 + 中栏 Section 列表 + 右栏实时预览）
- Create: `frontend/src/components/SectionCard.vue`（FIXED 变量赋值区 / DYNAMIC KO 选择 / 手动子项编辑器）
- Create: `frontend/src/components/VariableBindingModal.vue`（PAR 单选弹层 + ⚡符号匹配 / ✎当前绑定）
- Create: `backend/src/test/java/.../prompt/TemplateEngineTest.java`（OQ-15 Handlebars 子集 + Markdown 渲染）
- Create: `backend/src/test/java/.../prompt/ComposerRenderTest.java`（PRP 实际装配数 = selectedKOs + varBindings + manualSubItems，OQ-16）

**Approach:**
- PRM 模板存 `prm_template` 表 + `prm_section` 表（每模板 9 段示例 KO-PRM-0001 / 3 段 KO-PRM-0002 / 5 段 KO-PRM-0003）
- Section 类型 FIXED/DYNAMIC 字段化；DYNAMIC 段含 selectedKOs（KO 模式）/ manualSubItems（手动子项模式）/ varBindings（变量绑定 PAR）
- 自研 Handlebars 子集（OQ-15）：`{{var}}` 替换 / `{{#each items}}` 循环 / `{{#if}}` 条件
- 自研 Markdown 渲染器（OQ-15）：H2-H4 / 粗斜体 / 行内代码 / 代码块 / 表格 / 列表 / 引用 / 链接 / 分隔线 / `{{var}}` 高亮
- 实时预览：用户每次修改 Section → 重新调用 `/api/composer/render` → 后端组装 + 渲染 → 前端展示
- PRP 实际装配数（OQ-16）= selectedKOs + varBindings + manualSubItems（动态计算）
- 字符数 / Token 数（g(M) 函数）实时显示
- 变量绑定 PAR 单选弹层（OQ-16）：⚡自动匹配 `koLibrary.PAR.symbol === varKey` / ✎当前绑定 / ⊕重选 / ×解除

**Patterns to follow:** 原型 V3 中 `handleTypeSearch` / `renderComposerSections` / `onPRMTemplateChange` 函数的 Vue 3 重构（完整函数清单见 PRD §10.7.3.1）；**SNP/PRP 完整生命周期 + 陈旧快照流程参照 PRD v0.32 §5.2.4.1.1 mermaid 序列图**

**Test scenarios:**
- Happy path: 选择 KO-PRM-0001 → 加载 9 段 → 用户绑定各段 KO → 渲染显示装配 113 KO
- Edge case: §3 DYNAMIC 手动子项模式 → 用户录入手动子项 → 渲染
- Edge case: §4 DYNAMIC KO 模式 → 用户从 KO 库选 KOs → 渲染按 `{{#each items}}` 循环
- Edge case: FIXED 段 `{{var}}` → 自动匹配 `koLibrary.PAR.symbol === varKey`（⚡自动）
- Error path: Handlebars 语法错误（未闭合 `{{#if`） → 渲染失败 + 错误提示
- Error path: 变量未绑定（值为空字符串）→ 渲染保留 `{{var}}` 占位
- Integration: 渲染后字符数 / Token 数实时计算（g(M) 函数）
- Integration: PRP 装配数动态（OQ-16 — 不依赖模板作者登记）

**Verification:**
- Handlebars 子集 + Markdown 渲染按 OQ-15 收缩到产品需求范围
- 实时预览响应 ≤ g(M) 毫秒（NFR-03）
- PRP 实际装配数动态（OQ-16）
- KO-PRM-0001 9 段 / KO-PRM-0002 3 段 / KO-PRM-0003 5 段加载正确
- 变量绑定 PAR 单选弹层支持 ⚡自动 / ✎当前 / ⊕重选 / ×解除

---

### U7. 知识治理模块（冲突检测 + LLM 建议 + DeepSeek v4 + 仲裁快路径）

**Goal:** 实现 6 种 C 类冲突（C1/C2/C3/C4/C5/C6）+ 1 种 H 类健康（H2）的检测 + 13 项待处置治理项 + LLM 主动建议（OQ-9 接入 DeepSeek v4 + 配额管理 + ≤ 5s 响应）+ 管理员仲裁并发布快路径（OQ-8）+ 批量处置 + 治理报告导出。

**Requirements:** R4

**Dependencies:** U4（KO 库）、U2（DeepSeek v4 客户端）、U3（前端基础）

**Files:**
- Create: `backend/src/main/java/.../governance/model/ConflictEntity.java`、`LlmQuotaEntity.java`
- Create: `backend/src/main/java/.../governance/service/ConflictDetector.java`（C1-C6 + H2 指纹检测算法，§5.2.3.1）
- Create: `backend/src/main/java/.../governance/service/LlmSuggestionService.java`（调用 DeepSeek v4 + 配额计数 + ≤ 5s 超时）
- Create: `backend/src/main/java/.../governance/service/ConflictArbitrator.java`（OQ-8 仲裁快路径：Draft→Review→Approved→Published 自动流程）
- Create: `backend/src/main/java/.../governance/controller/GovernanceController.java`（API：检测 / 建议 / 仲裁 / 批量处置 / 报告导出）
- Create: `backend/src/main/resources/db/migration/V9005__create_governance_tables.sql`
- Create: `backend/src/main/resources/prompts/conflict-suggestion.txt`（DeepSeek v4 prompt 模板：KO 双方内容 + 作用域 + 字段 → 建议 / 置信度 / 理由）
- Create: `frontend/src/views/conflicts/ConflictsView.vue`（治理页 + 6 检测器卡 + 13 治理项 + 仲裁面板）
- Create: `frontend/src/components/ConflictRow.vue`（冲突详情行 + LLM 建议 + 置信度条 + 处置下拉 + 仲裁按钮）
- Create: `frontend/src/components/LlmQuotaTag.vue`（"余 1/2 次" 配额 chip）
- Create: `frontend/src/components/BatchActionBar.vue`（批量处置工具栏）
- Create: `backend/src/test/java/.../governance/ConflictDetectorTest.java`（C1-C6 + H2 检测 + 指纹算法）
- Create: `backend/src/test/java/.../governance/LlmSuggestionServiceTest.java`（DeepSeek v4 mock + ≤ 5s 响应 + 配额管理）
- Create: `backend/src/test/java/.../governance/ConflictArbitratorTest.java`（OQ-8 快路径：单次自动状态转换）
- Create: `frontend/src/test/views/conflicts/ConflictsView.test.ts`

**Approach:**
- 冲突指纹算法（§5.2.3.1）：MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12]；命中指纹 → 沿用旧 ID
- LLM 建议接口（OQ-9）：`POST /api/conflict/{id}/suggest` 调用 DeepSeek v4，prompt 含冲突双方 KO 内容 + 作用域 + 字段，请求返回 `suggestion/confidence/rationale`；配额每日 2 次
- 仲裁快路径（OQ-8）：管理员点击"仲裁并发布" → 创建 KO Version → 系统自动 Review → 自动 Approved → 自动 Published；审计日志写 `USER_CONFLICT_ARBITRATE`；C6 置信度 <0.8 弹 Modal 二次确认
- 检测触发：写时（KO 创建/编辑时自动检测）+ 发布前（用户点击"渲染提示词"时扫描已选 KO）+ LLM 异步（用户点击"LLM 冲突检测"）
- 配额管理：Redis INCR 计数（每日 0 点重置）；平台级 + 用户级双重限制
- 治理报告导出：CSV 格式，含冲突类型、KO、状态、处置时间、操作人

**Patterns to follow:** §5.2.3.1 指纹算法 + §5.2.3.2 LLM 建议接口；**v0.32 重构要点（OQ-9 新增 LLM 主动建议 + OQ-10 删除检测器版本号 + OQ-8 仲裁快路径）见 PRD §5.2.3 顶部 v0.32 重构要点段**；**冲突类型清单参照 PRD §5.2.3.1.1（9 行 × 4 列）**

**Test scenarios:**
- Happy path: 用户点击"LLM 冲突检测" → 调用 DeepSeek v4 → 返回建议 + 置信度 + 理由
- Edge case: DeepSeek v4 配额耗尽（42901）→ UI 配额 chip 变红 + 禁用按钮
- Edge case: DeepSeek v4 超时（>5s）→ 40801 + 重试提示
- Edge case: 同一 KO 冲突指纹命中 → 沿用旧 ID（不创建新记录）
- Edge case: 冲突 KO 内容变更后冲突不再成立 → 标记 `auto_resolved: true`
- Error path: 管理员点击"仲裁并发布" → 一次操作完成 Draft→Review→Approved→Published（C6 <0.8 需二次确认）
- Error path: 批量处置时同类型策略应用 → 一次操作处置多条
- Integration: 仲裁发布后原 Active KO 被新版本替换 + 旧版本归档

**Verification:**
- 6 检测器（C1-C6 + H2）按 §5.2.3.1 检测
- LLM 建议接口 ≤ 5s 响应（NFR-28）
- 管理员仲裁快路径（OQ-8）一次操作完成 4 状态转换
- 配额管理（平台 + 用户级）正确
- 指纹算法去重正确

---

### U8. 提示词快照模块（SNP/PRP + ko_assembly_hash + 时间线 + 陈旧快照）

**Goal:** 实现 SNP（装配方案版本）+ PRP（渲染动作）的双层关系（v0.32 §5.2.4）+ ko_assembly_hash 复用逻辑 + 版本时间线（V1.0~V3.0）+ PAR 变更后陈旧快照标记 + 13 KO 装配示例复现。

**Requirements:** R5

**Dependencies:** U6（提示词系统）

**Files:**
- Create: `backend/src/main/java/.../snapshot/model/PromptSnapshotEntity.java`、`PromptRecordEntity.java`
- Create: `backend/src/main/java/.../snapshot/service/SnapshotService.java`（SNP/PRP 关系 + ko_assembly_hash 计算 + 陈旧快照标记）
- Create: `backend/src/main/java/.../snapshot/service/StaleSnapshotJob.java`（PAR 变更后扫描受影响 SNP + 写 SNP_STALE_DETECTED 审计）
- Create: `backend/src/main/java/.../snapshot/controller/SnapshotController.java`（API：列表 / 详情 / 对比 / 导出）
- Create: `backend/src/main/resources/db/migration/V9006__create_snapshot_tables.sql`
- Create: `frontend/src/views/snapshots/SnapshotsView.vue`（版本时间线 + MAJOR/MINOR/PATCH 颜色节点）
- Create: `frontend/src/components/SnapshotSelector.vue`（PRP 选择下拉）
- Create: `frontend/src/components/SnapshotTimelineNode.vue`（V1.0~V3.0 节点 + 变更清单 [+]/[~]/[-]）
- Create: `backend/src/test/java/.../snapshot/SnapshotServiceTest.java`（OQ-16 113 KO 装配 + ko_assembly_hash 复用）
- Create: `backend/src/test/java/.../snapshot/StaleSnapshotJobTest.java`（PAR 变更触发 stale 标记）
- Create: `frontend/src/test/views/snapshots/SnapshotsView.test.ts`

**Approach:**
- `prompt_snapshot` 表存 SNP：`hash`（ko_assembly_hash 16 位截断）+ `rendered_text_canonical` + `prm_id` + `prm_version` + `ko_ids[]` + `ko_versions[]` + `ko_field_values[]` + `manual_subitems_hash` + `var_bindings[]`
- `prompt_record` 表存 PRP：每次渲染一条；`rendered_text` + `render_time` + `token_count` + `user_id` + `reason` + `force_rendered`
- ko_assembly_hash（v0.32 §5.2.4）：SHA256(prm_id + prm_version + sorted(ko_ids) + sorted(ko_versions) + sorted(ko_field_values) + manual_subitems_hash + sorted(var_bindings))[:16]
- 命中 hash → 复用 SNP，仅创建 PRP；不命中 → 创建 SNP + PRP
- 装配 KO 数（v0.32 OQ-16）= PRP 渲染时动态计算 `selectedKOs + varBindings + manualSubItems`，不依赖模板作者登记
- 陈旧快照：PAR KO 的 `current_value` 变更后，扫描所有引用该 PAR 的 SNP → 标记 `stale: true` + 写 `SNP_STALE_DETECTED` 审计
- 时间线 UI：MAJOR（橙色） / MINOR（绿色） / PATCH（灰色）圆点；展开显示变更清单 [+]/[~]/[-]
- 13 KO 装配示例（v0.32 OQ-16 §10.5.1）：PRP-0001 V3.0 = 113 KO（45 KO 模式 + 15 变量绑定 PARs + 38 手动子项 + 15 引用 SCHs）

**Patterns to follow:** v0.32 §5.2.4 hash 计算 + 复用规则；**SNP/PRP 完整生命周期 + 陈旧快照流程参照 PRD v0.32 §5.2.4.1.1 mermaid 序列图（hash 命中 / 未命中 / PAR 变更 stale 三场景）**

**Test scenarios:**
- Happy path: 渲染 KO-PRM-0001 → ko_assembly_hash 计算 → SNP 创建 → PRP 创建
- Edge case: 同一 ko_assembly_hash 再次渲染 → SNP 复用 + 仅创建新 PRP
- Edge case: PAR KO 当前值变更 → 所有引用该 PAR 的 SNP 标记 `stale: true` + 写 SNP_STALE_DETECTED 审计
- Edge case: PRP 实际装配数 = 45 + 15 + 38 + 15 = 113（v0.32 OQ-16）
- Error path: 同分钟内多次快照 → SNP 格式加 `-X` 序号（SNP-20260521-1430-2）
- Integration: 13 段结构（§1-§9）正确按 §10.5.1 装配
- Integration: 陈旧快照在治理页"陈旧快照"区块显示

**Verification:**
- SNP/PRP 关系按 v0.32 §5.2.4 执行
- 113 KO 装配（OQ-16）正确
- PAR 变更触发 stale 标记 + 审计
- 时间线 UI 正确显示 17 个版本（V1.0~V3.0）

---

### U9. 审计日志 + 项目管理 + 字典管理模块

**Goal:** 实现 3 个交叉模块（合并 U）：① 审计日志（5 类操作 + ID 三重暴露 + 仅前端撤销 + CSV 导出 + 12 月保留）；② 项目管理（4 项目 CRUD + 切换器 + 归档仅冻结 KO）；③ 字典管理（6 字典 + 9 量纲）。

**Requirements:** R6、R7、R8

**Dependencies:** U2（后端基础）、U3（前端基础）、U5（角色）

**Files (审计日志):**
- Create: `backend/src/main/java/.../audit/model/AuditLogEntity.java`
- Create: `backend/src/main/java/.../audit/service/AuditService.java`（写入 + 查询 + CSV 导出 + 12 月保留）
- Create: `backend/src/main/java/.../audit/controller/AuditController.java`
- Create: `backend/src/main/resources/db/migration/V9007__create_audit_tables.sql`
- Create: `frontend/src/views/audit-log/AuditLogView.vue`（6 列展示 + ID hover tooltip + 详情弹窗 + 筛选 + 导出 CSV）
- Create: `frontend/src/components/AuditLogDetailModal.vue`（完整 audit_log 字段弹窗）
- Create: `backend/src/test/java/.../audit/AuditServiceTest.java`（OQ-5 简化模型验证 + OQ-11 ID 三重暴露）

**Files (项目管理):**
- Create: `backend/src/main/java/.../project/model/ProjectEntity.java`
- Create: `backend/src/main/java/.../project/service/ProjectService.java`（OQ-1 维持 v0.37：仅数据隔离，无成员表；OQ-7 归档仅冻结 KO 不影响 PRP/SNP）
- Create: `backend/src/main/java/.../project/controller/ProjectController.java`
- Create: `backend/src/main/resources/db/migration/V9008__create_project_tables.sql`
- Create: `frontend/src/views/project-mgmt/ProjectMgmtView.vue`（4 项目列表 + 创建/编辑弹窗 + 归档 + 切换器）
- Create: `frontend/src/components/ProjectSwitcher.vue`（顶部栏下拉）
- Create: `backend/src/test/java/.../project/ProjectServiceTest.java`（OQ-7 归档不冻结 PRP/SNP）

**Files (字典管理):**
- Create: `backend/src/main/java/.../dict/model/DictEntity.java`
- Create: `backend/src/main/java/.../dict/service/DictService.java`（6 字典 + 9 量纲 + 软/硬删除 v0.32 §5.2.5）
- Create: `backend/src/main/java/.../dict/controller/DictController.java`
- Create: `backend/src/main/resources/db/migration/V9009__create_dict_tables.sql`
- Create: `frontend/src/views/dict-mgmt/DictMgmtView.vue`（6 Tab 分类 + 编辑弹窗）
- Create: `backend/src/test/java/.../dict/DictServiceTest.java`（软/硬删除 + 引用完整性校验）

**Approach (审计日志):**
- `audit_log` 表按 v0.32 §5.2.6 OQ-5 简化模型：id (AUDIT-YYYYMMDD-NNNNNN) + action + user_id + target_ko + detail + reason + created_at；**无** reverted_at / reverted_by / status(reverted) 字段
- ID 三重暴露（OQ-11）：① 表格行 mouseover tooltip（≤100ms NFR-29）显示完整 AUDIT ID ② 点击行打开详情 Modal 展示完整记录 ③ CSV 导出首列含 AUDIT ID
- 仅前端撤销（OQ-5）：所有"删除"类操作 Toast 3 秒可撤销 UI，**不调后端接口**，**不写 USER_ROLE_REVERT 审计**
- CSV 导出：内存流式生成，UTF-8 BOM 头，分页分批导出

**Approach (项目管理):**
- `project` 表存 4 项目（PROJ-0001~0004）+ status（active/archived）+ domain + created_at
- KO 按 `project_id` 过滤（OQ-1 数据隔离）
- 归档（OQ-7）：仅冻结 KO 变更（新增/编辑/发布/废弃/恢复）；PRP/SNP 不动；提示词列表/快照页对归档项目条目显示"项目已归档"标签
- 项目切换器（OQ-1 维持 v0.37）：列出全部 4 项目（含归档通过"显示已归档"勾选后可见）；切换后 X-Project-Id 头更新
- 不实现项目内成员/角色表（OQ-1 维持 v0.37）

**Approach (字典管理):**
- `dict` 表 6 字典：类型介绍 / 效力分级 / 权威分级 / 类型分组名称 / 知识对象概念 / 量纲配置
- 量纲表存 9 预制量纲：% / 元/TEU / h / 条 / 辆/岸桥 / 栏 / 人/班 / 台/班 / 箱/h
- 软删除：标记 `disabled: true`；存量 KO 仍显示该字典项但带 `ⓘ 已停用` 灰色标签
- 硬删除：仅未被任何 KO 引用的字典项可删；删除前校验 `SELECT COUNT(*) FROM ko WHERE ... LIKE '%"code":"XXX"%'`

**Test scenarios (审计日志):**
- Happy path: 5 类操作（KO 创建/修改/删除/版本发布/审批/冲突处置）写入 audit_log
- Edge case: hover 行 → 100ms 内显示完整 AUDIT ID（NFR-29）
- Edge case: 点击行 → 详情 Modal 显示完整记录（OQ-11）
- Edge case: CSV 导出首列为 AUDIT ID
- Error path: 删除 KO → Toast 3 秒可撤销 → 撤销仅 UI 回滚（OQ-5）

**Test scenarios (项目管理):**
- Happy path: 创建 PROJ-0001 → KO 列表过滤显示 PROJ-0001 下 KO
- Happy path: 归档 PROJ-0004 → KO 冻结 + PRP/SNP 仍可访问（OQ-7）
- Edge case: 跨项目 KO 不可见（PROJ-0001 用户查不到 PROJ-0002 KO）
- Edge case: 项目切换器显示全部 4 项目；"显示已归档"勾选可见归档项目
- Error path: 归档项目下尝试编辑 KO → 403

**Test scenarios (字典管理):**
- Happy path: 管理员编辑类型介绍 → 记录 audit_log
- Edge case: 软删除字典项 → KO 创建/编辑表单中不出现该字典项
- Edge case: 硬删除被引用的字典项 → 403 + 错误提示
- Error path: 9 预制量纲缺失 → 启动失败（部署门禁）

**Verification:**
- 审计日志按 v0.32 §5.2.6 OQ-5 简化（无 reverted_* 字段）
- ID 三重暴露按 OQ-11（hover / 详情 / CSV）
- 项目管理按 v0.32 §5.2.2 关键规则 4（仅 KO 冻结，OQ-7）
- 字典管理按 v0.32 §5.2.5（软/硬删除 + 引用完整性）

---

### U10. 文档预览模块（MinIO + PDF.js + LibreOffice headless + 30 分钟 token）

**Goal:** 实现 6 种文档格式的预览（PDF iframe / TXT 等宽字体 / DOCX Google Docs Viewer / 图片 `<img>` / 不支持格式提示）+ 30 分钟短期签名 token + 缓存转换结果 + 离线降级。

**Requirements:** R9

**Dependencies:** U2（后端基础）、U3（前端基础）、U9（审计）

**Files:**
- Create: `backend/src/main/java/.../document/model/DocEntity.java`（关联 KO-DOC 表）
- Create: `backend/src/main/java/.../document/service/MinIOService.java`（S3 兼容上传/下载/签名 URL 生成）
- Create: `backend/src/main/java/.../document/service/DocPreviewService.java`（按格式返回 preview_strategy + URL）
- Create: `backend/src/main/java/.../document/service/LibreOfficeConverter.java`（DOCX/XLSX/PPTX → PDF，30 天缓存）
- Create: `backend/src/main/java/.../document/controller/DocPreviewController.java`（API：`/api/doc/{ko_id}/preview` + `/api/doc/{ko_id}/file?token=...`）
- Create: `backend/src/main/resources/db/migration/V9010__create_doc_tables.sql`
- Create: `backend/pom.xml` 新增 minio + libreoffice (jodconverter-core) 依赖
- Create: `frontend/src/views/ko-doc/components/DocPreviewModal.vue`（900px 宽弹窗 + 格式分发）
- Create: `frontend/src/components/DocPreviewPDF.vue`（PDF.js iframe）
- Create: `frontend/src/components/DocPreviewText.vue`（等宽字体文本区域）
- Create: `frontend/src/components/DocPreviewImage.vue`（`<img>` 标签）
- Create: `backend/src/test/java/.../document/MinIOServiceTest.java`（签名 token 生成 + 30 分钟过期校验）
- Create: `backend/src/test/java/.../document/LibreOfficeConverterTest.java`（DOCX→PDF 转换 + 缓存命中）
- Create: `frontend/src/test/components/DocPreviewModal.test.ts`

**Approach:**
- 6 种预览策略：PDFJS_DIRECT（PDF iframe）/ PDFJS_CONVERTED（DOCX 转 PDF 后 iframe）/ TEXT_DIRECT（TXT 等宽）/ IMAGE_DIRECT（`<img>`）/ UNSUPPORTED（其他）
- 签名 token（30 分钟）：HMAC-SHA256(secret, ko_id + expiry) base64；过期 401
- LibreOffice headless：Spring `@Async` + Spring `TaskExecutor` 异步转换；转换结果存 MinIO（路径 `converted/{ko_id}.pdf`）+ Redis 缓存转换状态
- 客户端预校验：≤ 10MB / .xlsx/.csv（per NFR-25）
- 离线降级：PDF.js 自托管（如本地 LibreOffice 不可用，PDF 仍可读）
- 进度推送：WebSocket 推送转换进度（如 0-100% + ETA）

**Test scenarios:**
- Happy path: 点击 PDF 文档名 → 弹窗 → PDF.js iframe 渲染
- Happy path: 点击 DOCX 文档名 → 弹窗 → 转换 PDF → iframe 渲染
- Happy path: 点击 TXT 文档名 → 弹窗 → 等宽字体文本展示
- Happy path: 点击 PNG 文档名 → 弹窗 → `<img>` 展示
- Edge case: 签名 token 过期（>30 分钟）→ 401 + 重新打开
- Edge case: 同一 DOCX 第二次预览 → 缓存命中，秒级返回
- Error path: 转换失败（LibreOffice 不可达）→ 重试 2 次 → 降级为下载入口
- Error path: 客户端预校验文件 > 10MB → 413 + 错误提示
- Error path: 离线场景 PDF.js 仍可读（自托管）
- Integration: 审计日志记录 DOC_PREVIEW_ACCESSED

**Verification:**
- 6 种预览策略按 NFR-24/26/27 实现
- 30 分钟签名 token（NFR-27）
- LibreOffice 转换结果 30 天缓存（NFR-26）
- 客户端预校验 ≤ 10MB（NFR-25）

---

## System-Wide Impact

- **Interaction graph:**
  - OIDC 回调 → JWT → 后端 @PreAuthorize → 鉴权
  - KO 创建/编辑 → 写时检测（ConflictDetector）→ 冲突记录 + 审计
  - 提示词渲染 → TemplateEngine → PRP 写入 + 审计
  - 角色变更 → USER_ROLE_CHANGE 审计 → 用户下次登录时新 JWT 签发
  - LLM 建议 → DeepSeek v4 HTTP → 配额计数 → 建议/置信度/理由
  - **SNP/PRP 完整生命周期**（hash 命中复用 / hash 未命中创建 SNP+PRP / PAR 变更触发陈旧快照）→ **可视化见 PRD v0.32 §5.2.4.1.1 mermaid 序列图**（F-202 推荐添加）
- **Error propagation:**
  - 业务异常：BusinessException → GlobalExceptionHandler → 统一 Result<T> 响应
  - 鉴权失败：Spring Security 401/403 → 前端 axios 拦截器处理
  - LLM 超时：≤ 5s → 504 + 重试提示（NFR-28）
  - 数据库连接失败：HikariCP 重试 + 启动失败日志
- **State lifecycle risks:**
  - KO 状态机：Active → Pending 期间不允许创建新工作版本
  - PRP 装配数：动态计算，模板作者不登记（OQ-16）
  - 陈旧快照：PAR 变更后由后台 job 标记，不自动重建
  - 角色变更：旧 session 保持原权限，新登录时新角色生效（OQ-12）
- **API surface parity:**
  - RESTful API 统一前缀 `/api/`
  - 响应统一 Result<T>：code/data/message
  - Swagger 文档自动生成（SpringDoc）
  - 错误码规范：40101（鉴权失败）/ 40301（无权限）/ 40801（超时）/ 41301（文件过大）/ 42901（LLM 配额）
- **Surface coverage:**
  - App (Vue 3 SPA): in-scope
  - H5/PC web: out-of-scope（移动端列入二期）
  - Admin: in-scope（权限与角色 / 字典管理 / 项目管理）
  - backend (Spring Boot): in-scope
  - data/API (RESTful + MyBatis-Plus): in-scope
  - events/jobs (Flyway 迁移 + Stale Snapshot Job + LibreOffice 转换 Job): in-scope
  - observability: deferred（运维层另行规划）
  - testing (JUnit 5 + Testcontainers + Cypress): in-scope
- **Integration coverage:**
  - 跨设备撤销不可行（OQ-5）— 仅前端 UI 回滚，跨设备需重新登录
  - 跨项目 KO 隔离（OQ-1）— 通过 X-Project-Id 头过滤
  - LLM 调用限流（平台 + 用户级）— Redis INCR 计数
- **Unchanged invariants:**
  - 6 类型 KO ID 格式 `KO-{TYPE}-{NNNN}`（§10.7.7.1）— 不变
  - 版本号格式 `v{MAJOR}.{MINOR}.{PATCH}`（§10.7.7.5）— 不变
  - 5 预置角色 ID `ROLE-0001~0005` — 不变
  - 11 类对象 ID 规则（§10.7.7）— 不变

#### 未明确归属项的责任分配（F-205 推荐添加）

> Plan §System-Wide Impact 中 observability / 备份恢复 / SSO IdP 维护等项标记为 "deferred（运维层另行规划）"，但未明确归属。Sprint 0 启动前需与各团队协商：

| 责任项 | 建议归属 | 触发时间 | 备注 |
|--------|----------|----------|------|
| observability（Prometheus 指标 / Grafana 仪表盘 / 告警规则）| 基础架构团队 | Sprint 2 启动前 | U7 实施时埋点（`llm_quota_used_total` / `audit_log_insert_latency` 等 6 项），运维接入告警 |
| 备份恢复（MySQL 全量+增量 / MinIO 跨区复制 / 审计日志归档）| 基础架构团队 | Sprint 1 启动前 | NFR-15 12 月保留 + OQ-3 部署门禁依赖 |
| SSO IdP 维护（issuer URL / client_id/secret / token 证书轮换）| IT/安全 | Sprint 0 启动前 | 辽港现有 OIDC IdP 端点 + 定期轮换密钥 |
| 数据迁移（V9001 seed + 后续 schema 变更）| 后端 + DBA | Sprint 0 启动前 | Flyway V9001__seed_v032_initial_data.sql 落地 4 项目 + 5 角色 + 6 类型 KO 278 条 |
| LLM 配额审批（DeepSeek v4 调用配额 + 成本预算）| 产品 + 算法 + 财务 | Sprint 2 启动前 | OQ-T01 平台级 vs 用户级比例 |
| 角色变更通知（"通知中心 + 邮件"通道）| 系统管理员 + IT | Sprint 3 启动前 | OQ-T03 通知用户重新登录的 UX 通道 |
| 文档预览（PDF.js 自托管 / LibreOffice headless 容器镜像）| 基础架构 | Sprint 4 启动前 | U10 实施时 K8s Job 资源（CPU/内存）|
| 监控/日志/告警值班（on-call rotation / SLO 定义）| SRE / 运维 | 全量上线前 | 与 observability 配套 |

---

## Risks & Dependencies

| 风险 | 缓解 | PRD 引用（F-203 推荐添加） |
|------|------|---------------------------|
| DeepSeek v4 接入延迟与配额审批不及时 | U2 留 mock 客户端；U7 留 mock 服务；Sprint 1 后切换真实 LLM | PRD v0.32 §9.1 依赖（算法团队 2026-07-20 前）+ §9.2 风险表第 5 行 + OQ-T01 配额比例 |
| 辽港现有 OIDC IdP 端点未提供 | U3 留 OIDC mock；实施前由 IT/安全提供 | PRD v0.32 §9.1 依赖（IT/安全 2026-07-01）+ OQ-T03 通知通道 |
| MinIO 部署位置未确认 | U2/U10 留 application.yml 配置占位 | PRD v0.32 §9.1 依赖（基础架构 2026-07-12）+ §5.2.11 文档预览技术架构 |
| 模板引擎自研（OQ-15 KTD9）复杂度高 | U6 先实现 Handlebars 子集 + Markdown 子集；如遇复杂度升级到第三方库（marked + handlebars） | PRD v0.32 §10.5.3 PRM 模板渲染产品可观察输出（v0.15 F-015 收缩）|
| LLM 建议成本 | U7 配额管理（平台 80% / 用户 20%）；用户点击触发（不自动） | PRD v0.32 §9.2 风险表第 5 行 + NFR-28 ≤ 5s |
| MVP seed 数据完整性（v0.32 OQ-3 部署门禁） | U2 Flyway V9001 seed 脚本必须达 PAR 92 / DOC 76 / 4 项目 278 KO 闭环 | PRD v0.32 §9.1 依赖（业务专家 2026-07-17）+ §10.4 部署门禁 + OQ-T02 |
| K8s 集群配置 + Helm Chart 调试 | U1 先用本地 docker-compose 验证；CI/CD 跑通后再上 K8s | PRD v0.32 §9.1 依赖（数据/基础设施 2026-07-12）+ Sprint 1 交付物 |
| 审计日志 12 月保留的存储成本 | U2 按月分区表（`audit_log_YYYYMM`），老数据归档到冷存储 | PRD v0.32 NFR-15 12 月保留 + FR-26 验收 |
| 文档预览 LibreOffice headless 部署复杂度 | U10 用 jodconverter-core 封装；如不稳定 MVP 阶段可仅支持 PDF + TXT + DOCX Google Docs Viewer | PRD v0.32 §5.2.11 文档预览技术架构 + NFR-24/26/27 |
| 5 预置角色权限矩阵与 PRD §4.1 不一致 | U5 启动时加载 `seed/role-permissions.yaml`；U2 启动测试验证 | PRD v0.32 §4.1.1 5 预置角色清单 + §4.1.2/4.1.3 默认权限矩阵 + §4.1.5 权限查找指南 |
| 角色变更下次登录生效影响用户体验 | U5 角色变更时给用户发送通知（站内信 + 邮件）；U3 顶栏显示"您的权限已变更"提示 | PRD v0.32 US-18 验收 #4 + §5.2.2 关键规则 1（OQ-12 修订）|
| 跨类搜索性能（PAR 92 + 全部 278 KO） | U4 在 `ko` 表加 `(title, id, type)` 复合索引 | PRD v0.32 §5.2.10 列表分页口径 + FR-05 跨类搜索验收（OQ-4 source-resolved）|

---

## Documentation / Operational Notes

- 部署文档：`docs/deploy/k8s-setup.md`（K8s 集群配置 + Helm Chart 使用）
- 运维文档：`docs/operate/backup-recovery.md`（MySQL 备份 + MinIO 备份 + 审计日志归档）
- 监控告警（运维层）：MySQL 慢查询 / MinIO 存储 / DeepSeek v4 配额 / 角色变更频率（由基础架构团队）
- SSO 集成文档：`docs/integrate/oidc-sso.md`（辽港 IdP 对接步骤）
- LLM 集成文档：`docs/integrate/deepseek-v4.md`（API 调用 + 配额管理 + 错误处理）
- API 文档：自动生成（SpringDoc OpenAPI + Vue TypeScript 类型生成）

---

## Sources & References

- **Origin document:** [docs/brainstorms/liaogang-famou-km-platform-requirements.md](../brainstorms/liaogang-famou-km-platform-requirements.md)（v0.32 PRD 1379 行，20 OQ 全部 grill 闭合，spec_id: 2026-07-13-001-liaogang-famou-km-platform，status: checkpoint-prd）
- **Prototype:** [辽港伐谋知识管理平台_原型_V3.html](../../辽港伐谋知识管理平台_原型_V3.html)（HTML 原型 7630 行，含 15 个页面 + 100+ JS 函数）
- **Original PRD:** [辽港伐谋知识管理平台_PRD.md](../../辽港伐谋知识管理平台_PRD.md)（v0.31 原始输入）
- **Design tokens:** [DESIGN.md](../../DESIGN.md)（颜色 / 字体 / 间距 / 圆角）
- **Product positioning:** [PRODUCT.md](../../PRODUCT.md)（3 词定位：工业 / 精确 / 可信）
- **Language governance:** [CLAUDE.md](../../CLAUDE.md)（Chinese 硬性要求）
- **PRM example:** [prompt示例.md](../../prompt示例.md)（13 段标准主提示词骨架 + 9 硬约束 + 7 业务规则）
- **PRD OQ decisions:** v0.32 PRD §Owner Decision Trace（20 条决议）

---

## 计划元数据

- 计划创建时间：2026-07-13
- 计划深度：Deep（10 实现单元 + 关键决策 + 风险表 + 系统影响 + Phase 拆分）
- 计划来源：spec-prd 流程 v0.32 PRD
- 实施单位：U1-U10
- 预计 Sprint 划分（参考 v0.32 PRD §八）：
  - Sprint 1：U1 + U2 + U3 + U4（partial）
  - Sprint 2：U4（rest）+ U5 + U6
  - Sprint 3：U7 + U8
  - Sprint 4：U9 + U10 + 联调

#### Plan Sprint 拆分 ↔ PRD §8 时间计划映射（F-201 推荐添加）

> reader 容易混淆 Plan §计划元数据（4 个 Sprint + U 范围） 与 PRD §八时间计划（8 个里程碑 + 交付物）。两者关系如下：

| Sprint | Plan U 范围 | PRD §8 里程碑 + 交付物 |
|--------|-----------|------------------------|
| Sprint 1（计划 2026-07-10 → 实际 2026-07-13 启动）| U1 + U2 + U3 + U4（partial）| PRD §八里程碑 1-3（PRD 精炼 / 技术方案设计 / UI 设计交付）+ Sprint 1 交付物：导航框架、Dashboard、KO 类型列表页 |
| Sprint 2（计划 2026-07-31 完成）| U4（rest）+ U5 + U6 | PRD §八里程碑 4（Sprint 2：KO 详情 + 提示词）+ 交付物：KO 详情页、三栏组装器、渲染引擎 |
| Sprint 3（计划 2026-08-14 完成）| U7 + U8 | PRD §八里程碑 5（Sprint 3：知识治理 + 提示词版本快照）+ 交付物：治理页（C/H类）、LLM 建议接口、提示词快照时间线、审计日志 |
| Sprint 4（计划 2026-08-28 完成）| U9 + U10 + 联调 | PRD §八里程碑 6（Sprint 4：权限 + 配置 + 联调）+ 交付物：角色权限、字典管理、端到端联调 |
| 灰度发布（2026-09-18）| — | 内部用户可用（5-8 人）|
| 全量上线（2026-09-25）| — | 全团队可用（~15 人）|

> 关键提示：Plan Sprint 拆分是**实施单位**视角（U1-U10 落地节奏），PRD §8 是**业务里程碑**视角（用户可感知的交付节点）。两者应**并行参考**：每个 Sprint 完成后既能交付 U 实施，也能验证 PRD §8 对应里程碑的交付物。

---

**Plan written to `/Users/liyang129/data/liaogang/docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`**

接下来按 `plan-handoff.md` 执行：
- Phase 5.3.8: 跑 `spec-doc-review` 文档 review
- Phase 5.3.9: final checks
- Phase 5.4: 呈现 5 选项 handoff 菜单（start-work / write-tasks / issue / proof / done）
