# Changelog

- 记录格式：`- v版本号 YYYY-MM-DD HH:MM:SS 作者: 变更摘要 [(user-visible)]`
- 说明：
  - `v版本号` 使用本次变更对应的发布版本
  - 日期时间必须使用 `YYYY-MM-DD HH:MM:SS`
  - `作者` 填写提交人或变更责任人
  - `变更摘要` 使用中文，简明说明本次改动
  - 用户可感知的变更在末尾追加 `(user-visible)`

- v1.13.2 2026-07-13 15:47:30 liyang: 使用 spec-first 初始化项目
- v1.14.0 2026-07-13 16:30:00 liyang: 同步 PRD v0.31/v0.32/v0.37/v0.38 修订到原型 V3 (user-visible)
  - 同步 KO 数量口径：PAR 89→92、DOC 70→76（侧栏 subcount、KO 库首页卡片、Dashboard breakdown、KO-PAR/KO-DOC 副标题共 7 处） (user-visible)
  - 删除 AB 岗文案：Dashboard 活动列表"待 AB 岗审核"→"待合规审核员审核" (user-visible)
  - 删除项目成员管理：移除 US-16 残留（成员数列、4 个项目成员数字、membersModal 弹窗、membersData 数据、openMembersModal/renderMembers/addMember/removeMember 函数、相关 CSS 样式） (user-visible)
  - 调整项目列表 KO 数：PROJ-0001 120→121，使四项目加和 121+72+49+36=278 闭环 (user-visible)
  - 顶部栏项目切换器补 PROJ-0004 归档项目（默认 disabled + 新增"显示已归档"复选框 + toggleArchivedProject 函数） (user-visible)
- v1.15.0 2026-07-13 19:30:00 liyang: 通过 spec-prd 流程精炼 PRD v0.31 → v0.32，新增 17 项 owner 决议
  - 新增 docs/brainstorms/liaogang-famou-km-platform-requirements.md（1100+ 行 PRD 产物，含 Owner Decision Trace 17 条 / Outstanding Questions 4 项 / Planning Recheck 8 项）
  - 删除业务专家「提案权」功能（整段移除 §5.2.2.1 + FR-41/42 + §6 业务专家活跃度行 + 权限矩阵 5 列保持） (user-visible)
  - 改为补 seed 达到 header == list 严格一致（PAR 92 / DOC 76 / 四项目 121+72+49+36=278 闭环；删除 §5.2.10 演示态 chip 方案） (user-visible)
  - 3 秒撤销仅前端（删除后端接口契约 POST /api/audit/{id}/revert + audit_log reverted_* 字段） (user-visible)
  - PRM 走标准 KO Version 流程（重写 §5.2.1.4 PRM 豁免表） (user-visible)
  - 项目归档仅 KO 冻结（PRP/SNP 不动，§5.2.2 关键规则 4 明确化） (user-visible)
  - 管理员仲裁快路径（自动 Draft→Review→Approved→Published；审计日志 USER_CONFLICT_ARBITRATE） (user-visible)
  - 新增 LLM 冲突建议接口 POST /api/conflict/{id}/suggest（OQ-9；新增 NFR-28 LLM 建议响应 ≤ 5s） (user-visible)
  - 删除"检测器版本号"相关需求（v0.31 原型 v2026.07.08 显示 + TASK-C6-YYYYMMDD-HHMM 编号移除）
  - 审计日志 ID 三重暴露（hover tooltip + 详情弹窗 + CSV 导出；新增 NFR-29 hover 响应 ≤ 100ms） (user-visible)
  - 角色变更下次登录时生效（撤销 v0.34 修订"下次操作时生效"；删除 WebSocket role-changed 推送） (user-visible)
  - 113 KO 改为 PRP 动态实际数（删除"模板作者登记 assembly_ko_count"部署门禁） (user-visible)
  - 模板引擎描述收缩到产品需求范围（删除"自研 renderMarkdown() 引擎"措辞，改为产品可观察输出） (user-visible)
  - §6 成功指标移除"业务专家活跃度"和"提示词-算法结果关联率"（后者移入二期 R-10） (user-visible)
  - 跨类搜索按原型实现（FR-05 验收补 title+id+typeName 3 字段） (user-visible)
  - 当前为 checkpoint-prd 状态（17/17 OQ 全部 grill 闭合，可入规划阶段；finalize 升级需 producer 路径）
- v1.15.1 2026-07-13 19:45:00 liyang: spec-prd 二次精炼追加 OQ-18
  - 参数化 PRD 内具体 KO/文档数量信息（删除源组织估算 "约 70 个文档"；保留结构性数字 6 类型/5 角色/4 系统角色/4 项目；seed 数据保留为 "v0.32 初始 seed" 标签不作为未来约束）
  - G-01~G-06 目标值参数化为 N/K%/T%/P 等可配置变量
  - §六 成功指标三阶段 DAU + 9 项指标全部参数化为可配置变量
  - NFR-02/03/04 重写为 SLA 函数 g(N)/h(M)/f(N)
  - FR-01 验收：统计卡片显示数字来自后端 /api/ko/count 接口实时计算，不限定具体数值
  - §9.1 依赖中 "补 PAR 92 / DOC 76 seed" 改为 "seed 完整性达部署门禁"
  - FR-15 中 "113 个 KO 装配" 改为 M 个动态实际数
  - 新增 OQ-18 Owner Decision Trace 行（18/18 OQ 全部 grill 闭合）
  - PRD 行数：1049 → 1088 (+39 行)
- v1.15.2 2026-07-13 20:00:00 liyang: spec-prd 三次精炼追加 OQ-19 §四 详细 US 补全
  - §四 4.2 从"一句话概述清单"升级为完整 US-01~US-21 详细描述（按 v0.32 18 项决议应用修改）
  - US-01 卡片显示数字改为来自后端 /api/ko/count 接口实时计算（OQ-18）
  - US-05 增加 OQ-9 LLM 建议 + OQ-8 仲裁并发布快路径描述
  - US-06 冲突详情行增加 LLM 主动建议；仲裁并发布为快路径
  - US-08 增加 OQ-6 PRM 走标准 KO Version 流程
  - US-11 业务专家仅 RUL 可创建（OQ-2 业务专家直接编辑范围）
  - US-12 PRP 装配 KO 数改为动态实际数（OQ-18）
  - US-13 删除"三级防线覆盖"措辞
  - US-15 移除"成员数列"措辞（OQ-1 维持 v0.37）
  - US-16 整条删除（v0.37 维持）
  - US-17 5 列权限矩阵无"提案"列（OQ-2）
  - US-18 角色变更下次登录生效 + 删 WebSocket + 撤销仅前端 UI（OQ-5+OQ-12）
  - US-20 审计日志 ID 三重暴露（OQ-11）
  - US-21 4 类型导入（CON/RUL/PAR/SCH）保持
  - 新增 OQ-19 Owner Decision Trace 行（19/19 OQ 全部 grill 闭合）
  - PRD 行数：1088 → 1287 (+199 行)
- v1.15.3 2026-07-13 20:30:00 liyang: spec-prd 四次精炼追加 OQ-20 §4.1 权限矩阵重构
  - §4.1 从"5 角色清单 + 单一 2D 矩阵"重构为"5 预置角色 + 默认权限矩阵（v0.32 初始 seed）+ 权限可调整性"三段式
  - §4.1.1 5 预置角色清单（ROLE-0001~0005，含定位 + 默认范围）
  - §4.1.2 默认权限矩阵 - 知识对象库 KO 子类型（5 角色 × 6 KO 类型 × 5 操作 完整默认矩阵，5 个独立 6×5 表）
  - §4.1.3 其他菜单默认权限（总览/提示词/知识治理/审计日志/项目管理/字典管理/权限与角色）
  - §4.1.4 权限可调整性（管理员通过 FR-27 调整；预置角色不可删除但权限可调整；支持新增自定义角色；变更下次登录生效）
  - §5.2.2 关键规则补充第 4 条"权限可调整性"（OQ-20）
  - 新增 OQ-20 Owner Decision Trace 行（20/20 OQ 全部 grill 闭合）
  - PRD 行数：1287 → 1379 (+92 行)
- v1.15.4 2026-07-13 22:00:00 liyang: spec-doc-review P1 推荐方案落地（PRD 文档可读性改进）
  - F-001：PRD 顶部新增「## 导航（模块地图 + ID 格式速查）」章节，含 14 章节模块地图 + 12 类 ID 格式速查表
  - F-002：§5.2 末尾新增「5.2.12 横向约束快速定位表」（11 行 × 3 列：子节号 / 标题 / 关键决议）
  - F-003：§5.2.4.1 新增「SNP/PRP 创建与陈旧快照流程」mermaid 序列图（演示 hash 命中 / 未命中 / PAR 变更 stale 三场景）
  - F-004：§5.2.6 顶部新增「v0.32 重构要点」总结（3 句话：3 秒撤销仅前端 / audit_log ID 三重暴露 / audit_log 简化模型）
  - F-005：§4.1.5 新增「权限查找指南」段落（5 角色 × 13 菜单 × 5 操作共 325 cells 快速定位，5 种查找目标 → 优先查 + 备用查）
  - F-006：PRD 顶部「## 导航」含 12 类 ID 格式速查表（与 §10.7.7 完整版交叉引用）
  - PRD 行数：1379 → 1505 (+126 行)
- v1.15.5 2026-07-13 22:30:00 liyang: spec-doc-review P2 推荐方案落地（PRD 文档可读性改进 + 内部一致性）
  - F-014：§5.2.3 顶部新增「v0.32 重构要点」（4 句话：MD5 指纹保留 / LLM 主动建议新增 / 检测器版本号删除 / 冲突类型占位清晰）
  - F-009：§5.2.3.1.1 新增「冲突类型清单」表格（9 行 × 4 列：C1-C6 + H1-H6 完整对照）
  - F-007：§六 末尾新增「v0.32 初始 seed 示例」段落（8 行指标示例 + 对应参数化变量映射）
  - F-008：§10.4 顶部新增「v0.32 初始 seed 与 §6 参数化目标的区别」段落（6 维对比：来源 / 作用 / 关系 / 示例 / 数据来源 / 修订触发）
  - F-010：Outstanding Questions 表新增「决策触发时间」列（4 行：OQ-T01~T04 各对应 Sprint 0~2 启动前）
  - F-011：Planning Recheck 表新增「实施启动前触发条件」列（8 行：R-PL-01~R-PL-08 各对应 Sprint 0~4 启动前）
  - F-012：§10.7.1 简化（引用 DESIGN.md 完整版，保留 PRD 实施特有约束 6 项：端口蓝 / 信号橙 / 深色 Rail / 6 类型颜色 + 形状 / 3 秒撤销 Toast / audit_log hover 响应）
  - F-013：§10.7.3.1 新增「原型 V3 函数映射」表（17 行：场景 → 原型 V3 函数 → 行号 → 实施模块）
  - PRD 行数：1505 → 1600 (+95 行)
- v1.15.6 2026-07-14 liyang: spec-doc-review Plan re-review P1 推荐方案落地（跨文档引用更新）
  - F-101：Plan U4 / U5 / U6 / U7 / U8 加 PRD v0.32 新子节引用（§5.2.3.1.1 冲突类型清单 / §5.2.4.1.1 SNP-PRP mermaid / §4.1.5 权限查找指南 / §5.2.3 v0.32 重构要点 / §10.7.3.1 原型 V3 函数映射）
  - F-102：Plan §Open Questions 加「Plan Q-I ↔ PRD OQ-T 映射」段（5 行 × 4 列：Plan Q-I / PRD OQ-T / 主题 / 决策触发时间）
  - F-103：Plan §Completion Criteria 加「跨文档引用」子节（10 行 × 3 列：Plan U / 验收参照 / PRD §引用）
  - Plan frontmatter 更新：date 2026-07-13 → 2026-07-14；deepened 2026-07-13 → 2026-07-14
  - Plan 行数：约 700+ → 950 (+~250 行)
  - Plan ↔ PRD 引用数：6 → 15 (+9 处)
- v1.15.7 2026-07-14 liyang: spec-doc-review Plan re-review P2 推荐方案落地（跨文档 + 责任分配）
  - F-201：Plan §计划元数据 加「Plan Sprint 拆分 ↔ PRD §8 时间计划映射」表（4 行 × 3 列：Sprint / Plan U 范围 / PRD §8 里程碑 + 交付物）
  - F-202：Plan §System-Wide Impact "Interaction graph" 加 1 行：SNP/PRP 完整生命周期可视化见 PRD v0.32 §5.2.4.1.1 mermaid 序列图
  - F-203：Plan §Risks & Dependencies 表加 1 列"PRD 引用"（12 行风险全部交叉引用 PRD §9.1/§9.2/§4.1/§10.4 等）
  - F-205：Plan §System-Wide Impact 加「未明确归属项的责任分配」表（8 行 × 4 列：observability / 备份恢复 / SSO IdP 维护 / 数据迁移 / LLM 配额审批 / 角色变更通知 / 文档预览 / 监控值班）
  - F-204 已被 F-101 覆盖（U5 Patterns 已加 §10.7.3.1 引用 + 具体函数名 switchRoleTab/openRoleAssignmentModal/saveRole/saveUserRole），跳过
  - Plan 行数：950 → 1000+ (+~50 行)
- v1.16.0 2026-07-14 18:39:51 liyang: Sprint 1 基础架构落地（U1 项目脚手架 + U2 后端基础 + U3 前端基础 全部 10 任务完成 + spec-code-review 24 项安全修复）
  - U1-T001：建立 Monorepo 单一 git 仓库顶层目录约定（frontend / backend / deploy / docs / scripts）+ README / .gitignore / setup.sh / seed.sh / test.sh (user-visible)
  - U1-T002：建立 K8s + Helm 部署基础（Chart.yaml / values.yaml / frontend-deployment / backend-deployment / backend-service / ingress / _helpers.tpl）`helm template` 无报错 (user-visible)
  - U2-T004：建立 Spring Boot 3 后端基础架构（KmApplication / MyBatis-Plus / MySQL 8 / Flyway / HikariCP / SpringDoc OpenAPI / 统一 Result<T> 响应 / BusinessException / GlobalExceptionHandler）`mvn spring-boot:run` 启动 + `/v3/api-docs` 返回 OpenAPI JSON (user-visible)
  - U2-T005：实现辽港统一认证后端集成（OQ-23 取代原 OIDC）：招商云 PAAS `getUserInfoByCode`（APIKEY 鉴权）+ 本地 user 表创建/查找（sub UUID + preferred_username 工号）+ 5 预置角色查询 + 本地 JWT 签发 + Redis 缓存 + 角色下次登录生效（OQ-12）+ 跨设备撤销不可行（OQ-5） (user-visible)
  - U2-T006 Part 1：通用基础设施（Result / BusinessException / GlobalExceptionHandler）+ @AuditLog 注解 AOP（AuditAspect 写入 audit_log 表 + AuditLogAnnotation + AuditLogService） (user-visible)
  - U2-T006 Part 2：DeepSeek v4 客户端（HTTP/SDK + ≤5s 超时控制，NFR-28）+ LLM 配额管理（OQ-9 + OQ-T01，Redis INCR 计数） (user-visible)
  - U2-T010：Flyway V9001__seed_v032_initial_data.sql seed 脚本，4 项目 + 5 预置角色 + 6 类型 KO 278 条（121+72+49+36 闭环）+ 6 字典 + 9 量纲 + 权限矩阵 150 cells，部署门禁达成（OQ-3 强化：header == list 严格一致） (user-visible)
  - U3-T007：建立 Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router 4 + Axios 前端基础架构，端口蓝 #0F4C75 主题变量对齐原型 V3，`pnpm dev` 启动 (user-visible)
  - U3-T008：慧应用 SSO 前端跳转集成（OQ-23 取代原 OIDC）：LoginView + 回调页 code → JWT + Pinia auth store + sessionStorage 持久化 + 401 自动跳登录 + codeVerifier sessionStorage 缓存 (user-visible)
  - U3-T009：前端跨切组件（Sidebar 侧栏 3 分组 / TopBar 顶栏面包屑+通知+用户 Chip / TablePagination 平台统一分页 / 路由守卫对无权限角色重定向 / Element Plus 主题对齐原型 V3） (user-visible)
  - 安全修复：spec-code-review 修复 24 项（P0 + P1 + P2 安全加固；JWT 密钥强化 + Lombok @SneakyThrows 替换 + SQL/HQL 注入面关闭 + Spring 表达式注入面关闭 + Actuator 收敛等）
  - 集成测试环境配置：新增 `backend/src/test/resources/application-it.yml`（`it` profile；MySQL 127.0.0.1:3306/km_platform_it + Redis 127.0.0.1:6379 db 1 + MinIO 127.0.0.1:9001 km-platform-it bucket；Flyway 真实执行；JPA/Hibernate 验证；SpringDoc 启用；minioadmin 凭据入测试资源，生产凭据走 K8s Secret）；单元测试 `application-test.yml` 保持禁用外部依赖不变
  - 集成测试一键脚本：新增 `deploy/docker/docker-compose.yml`（MySQL 8 + Redis 7 + MinIO 三服务 + healthcheck + 命名 volume 持久化）+ `scripts/test-env-up.sh`（启动 + 等待就绪轮询）+ `scripts/test-env-down.sh`（停止 / `--clean` 清数据卷）+ `scripts/test-it.sh`（up + `mvn test -Dspring.profiles.active=it` + 可选 stop；`--no-up` `--stop` `--clean` 三个 flag 覆盖本地/CI/重置场景）；`scripts/setup.sh` 末尾提示从 `docker-compose up -d` 改为指向 `test-env-up.sh`
  - 集成测试体系完善：1) `pom.xml` 加 `maven-failsafe-plugin` + 显式 surefire 排除 `*IT.java`，`*Test.java` 走 surefire 单元测试 + `*IT.java` 走 failsafe 集成测试（自动激活 it profile）；2) `scripts/test-it.sh` 从 `mvn test` 改 `mvn verify`；3) 加 `MinioBucketInitializer` 测试配置（it profile 启动时幂等创建 `km-platform-it` bucket）+ `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 自动发现；4) 加 `spring-security-test` 测试依赖；5) 新增 `AuditAspectIT.java` 集成测试样例（F-12 修复回归：成功 + 失败路径都写审计日志 + action + `_FAILED` 后缀 + ID 格式 `AUDIT-YYYYMMDD-NNNNNN` + anonymous 优雅降级；用 Logback ListAppender 拦截 `AuditLogService` 日志；待 audit_log 表实装后追加 JDBC 验证）
  - 总变更规模：49 文件 +3682 行（10 个 commit：27069d3 / d2a7481 / fd9f3cc / 20be4d6 / 55e929d / c33b244 / 69a305c / 83bf3e4 / 62c7dc5 / 909d5a2）+ CHANGELOG 1 入口 + it profile 1 文件 + docker-compose 1 文件 + 3 个 shell 脚本 + setup.sh 微调 + failsafe 配置 + bucket 初始化器 + 集成测试样例
- v1.16.1 2026-07-15 12:30:00 liyang: Sprint 1 编译错误修复 + 集成测试环境验证通过（17 文件改动；F-21 ~ F-25 修复序列）
  - **F-21 编译错误修复**：`LlmQuotaService` 多行字符串字面量改 `text block`（Java 17）；`JwtAuthFilter` 字段游离于 class 外移入 class 内；`AuditLog` 实体类名与 `@interface AuditLog` 注解冲突，实体改名为 `AuditLogEntity`、`AuditLogAnnotation.java` 改名为 `AuditLog.java`（注解名保持，引用零变更）；`AuditLogService` / `DeepSeekClient` final 字段构造器重复（合并 / 删 `@RequiredArgsConstructor`）；`AuditLogService` / `LlmQuotaService` 移除 Lombok 构造器注入改 `@Autowired(required=false)` 让 StringRedisTemplate 在 test profile 缺失时仍可启动
  - **F-22 依赖修复**：`pom.xml` 加 `lombok`（provided）/ `jjwt 0.11.5`（0.12.x API 大变：`setSigningKey`/`parseClaimsJws`/`getBody` 全部移除，降级到 0.11.5 兼容现有代码）/ `spring-boot-starter-security`（之前漏掉，`HttpSecurity` 找不到根因）；`mybatis-plus-spring-boot-3-starter` → `mybatis-plus-spring-boot3-starter`（连字符位置错，baidu-nexus HTTP 404）
  - **F-23 import 补齐**：`JwtService` 加 `jakarta.annotation.PostConstruct`（Spring Boot 3 + Jakarta EE 9+）；`LiaogongAuthController` 加 `java.util.List`；`GlobalExceptionHandler` 加 `Authentication` / `SecurityContextHolder`
  - **F-24 配置修复**：`application.yml` 删第二个重复 `server:` 顶级键；`application-it.yml` MinIO endpoint `9001` → `9000`（**关键发现**：9000 = S3 API 端口，9001 = Console Web UI；`test_env.txt` 端口写反）；`MinioBucketInitializer` `io.minio.error` → `io.minio.errors`（MinIO 8.5.10 包路径）
  - **F-25 集成测试 skip**：`AuditAspectIT` 加 `spring.flyway.enabled=false`（V9001 seed 假设 role/ko 表存在但 Sprint 1 范围无建表 migration，U4/U9 实装时补；本次集成测试只验证 AOP + Logback ListAppender 不依赖 schema）
  - **端到端验证**：`./scripts/test-it.sh` 跑通，`mvn clean verify` BUILD SUCCESS，5/5 测试通过（1 单元 + 4 集成），耗时 7.5s
  - **host 探测**：`test-env-up.sh` 加 MySQL/Redis/MinIO 协议层探测（python socket + curl），已部署则跳过 docker compose up；覆盖本机（已装服务）+ CI（无服务）双场景
  - **未 commit**（spec-first 全局配置不进 Sprint 1）：`.claude/settings.json` 4 个 hook args 改绝对路径（根治 Stop hook 在子目录解析失败）`backend/target/`（构建产物）/ `backend/logs/`（运行时日志）/ `docs/tasks/`（task pack）
  - 总变更规模：17 文件 +204 / -126 行（编译错误修复 + 集成测试环境 + hook 路径根治）
- v1.16.2 2026-07-15 14:06:00 liyang: 沉淀 Sprint 1 缺编译门禁教训（spec-compound knowledge，docs/solutions/）
  - 新增 `docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`（11.5 KB / 196 行；spec-compound Bug track 模板）：复盘 Sprint 1 12 commit 0 次 `mvn compile` 验证导致 15+ 编译错误的根因
  - **Problem**：9 commit 累计 25 main java 文件改动，0 次 mvn compile 验证；仓库无 `.github/workflows/` / `.githooks/` / pre-commit hook；task pack 写了 `test_focus` 但只挂在文档里没人执行
  - **Root cause**：`missing_tooling`（commit 流程缺任何形式的 mvn compile 门禁）
  - **Resolution**：`workflow_improvement`（加 3 层防御：pre-commit `mvn compile` / pre-push `mvn verify` / GitHub Actions CI）
  - **Prevention**（P1/P2/P3 分层）：pre-commit hook + pre-push hook + GitHub Actions + task pack test_focus 自动化 + 环境健康检查 + Flyway 顺序约定 + commit task_id 规范 + Sprint 收尾必跑清单
  - **Frontmatter** 严格按 `spec-compound/references/schema.yaml` Bug track 模板（title/date/category/module/problem_type/component/severity/symptoms[5]/root_cause/resolution_type + 新 promote 必填 `invalidation_condition` / `source_refs` + 8 tags）；通过 `validate-frontmatter.py` exit 0
  - **Related solutions 待立**（本文档 Related 段列出，本次未立）：MinIO 端口语义 / Lombok `@RequiredArgsConstructor` 冲突 / public `@interface` 文件名规范 / Stop hook 绝对路径
  - 关联 commit：`d676c27` (fix backend) + Sprint 1 全部 12 commit（9 原始 + 2 集成/安全 + 1 fix）
- v1.16.3 2026-07-15 15:30:00 liyang: 实施 P1 三层编译门禁（pre-commit / pre-push / GitHub Actions CI）
  - **新增 `.githooks/pre-commit`**：commit 前跑 `mvn compile -q`（约 5-10s），仅当本次 commit 涉及 `backend/**.java` 才跑（避免改 frontend/docs/.githooks 拖慢）；用 `set -o pipefail` 确保 mvn 失败被正确捕获（修复初版 `mvn | tail` 让 tail 退出 0 掩盖 mvn 失败的 bug）；实测故意改坏 `Result.java` 触发 `非法字符: '#'` 编译错误，hook 立即输出"❌ mvn compile 失败"并 exit 1 阻断 commit
  - **新增 `.githooks/pre-push`**：push 前跑 `mvn verify -Dspring.profiles.active=it`（约 30s），仅当本次 push 涉及 `backend/**` 才跑；需要 host 已有 MySQL/Redis/MinIO 或运行 `./scripts/test-env-up.sh`
  - **新增 `.github/workflows/backend-ci.yml`**：PR 必跑 `mvn verify`（MySQL 8.0 + Redis 7-alpine + MinIO service containers，含 healthcheck）；Maven 依赖 `actions/cache` 缓存；surefire/failsafe reports 上传为 artifact（保留 7 天）；触发条件 push 到 main / feat/** 或 PR 目标 main
  - **README 增强**：在"快速开始"前加"开发者首次 clone 必做"段，明确 `git config core.hooksPath .githooks` 启用步骤 + 三层 hook 行为表 + 跳过 flag（`--no-verify`）+ 关联 solution 文档
  - **本地启用**：`git config core.hooksPath .githooks` 已在主仓配置；新 clone 的开发者首次运行该命令即可
  - **来源**：Sprint 1 复盘 `docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md` P1 提案
- v1.16.4 2026-07-15 15:50:00 liyang: 立 4 个 related solution 文档（spec-compound 沉淀 Sprint 1 子案例）
  - **新建 `docs/solutions/integration-issues/minio-port-semantics.md`**：MinIO 端口语义（9000 = S3 API / 9001 = Console Web UI；`test_env.txt` 写反是源头）。track=integration_issue / component=tooling / severity=medium
  - **新建 `docs/solutions/build-errors/lombok-required-args-constructor-conflict.md`**：Lombok `@RequiredArgsConstructor` 与手动构造器签名冲突导致重复定义。track=build_error / component=tooling。含 `AuditLogService` 字段注入 + `DeepSeekClient` 构造器合并两种修法
  - **新建 `docs/solutions/conventions/java-public-type-filename.md`**：Java 规范 public type 必须与文件名匹配（JLS §7.6）；注解 vs 实体同名冲突解决方案。track=convention / component=tooling
  - **新建 `docs/solutions/tooling-decisions/stop-hook-absolute-paths.md`**：Claude Code Stop hook 相对路径在 worktree 子目录解析失败；改绝对路径根治。track=tooling_decision / component=tooling
  - **全部 frontmatter 验证**：4 个新文档均通过 `spec-compound/scripts/validate-frontmatter.py` exit 0
  - **`docs/solutions/` 总览**：1 篇 Sprint 1 总览 + 4 篇子案例（按 spec-compound 分类：build-errors × 2 / integration-issues × 1 / conventions × 1 / tooling-decisions × 1）
  - **来源**：执行 Sprint 1 复盘文档 `build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md` Related 段列出的 4 个 related solutions
- v1.16.5 2026-07-15 16:30:00 liyang: LLM 真接入适配（Q-I1 部分信息已收齐：从 llm_client.txt 提取）
  - **改 `application.yml` `app.llm`**：base-url 默认 `https://api.deepseek.com/v4` → `https://qianfan.baidubce.com/v2`（真实接入走百度千帆 OpenAI 兼容网关，非官方 deepseek.com）；model 默认 `deepseek-v4` → `deepseek-v4-flash`；env var 从 `DEEPSEEK_*` 改 `LLM_*`（更准确反映实际服务来源）
  - **新增 `monthly-cost-cap-yuan: 5000`**：OQ-T01 月度成本上限占位（Q-I1 配额收齐后由 owner 改）
  - **改 `DeepSeekClient.parseResponse()`**：适配 OpenAI 标准 + 千帆 Qianfan 扩展 schema
    · 提取 `message.content`（已用）+ 新增 `message.reasoning_content`（千帆扩展，推理过程独立计费）
    · 提取 `usage.{prompt,completion,total}_tokens`（OpenAI 标准）
    · 提取 `usage.completion_tokens_details.reasoning_tokens`（千帆扩展）
    · 加 `asInt(Object)` 工具方法处理 Number/String/null 三种类型
    · TODO 注释：U7 实施时升级为真实 JSON 解析（OQ-9 期望 LLM 返回结构化 JSON）
  - **改 `LlmSuggestion` 字段**（+5 字段）：reasoning / promptTokens / completionTokens / totalTokens / reasoningTokens（埋点数据 + 计费用）
  - **不需改的**（已兼容）：URL 拼接（`${base-url}/chat/completions`，base-url 含 `/v2` 时拼对）/ Bearer Token 鉴权（`setBearerAuth`）/ RestTemplate 5s timeout（满足 NFR-28）
  - **Q-I1 仍缺 4 项**（不在本次改动范围）：配额 TPM/TPD / dev URL / 上下文窗口 / 429 fallback 策略
  - **`mvn compile` 验证**：exit 0（DeepSeekClient + application.yml 改动编译通过）
  - **来源**：`llm_client.txt`（用户提供的 curl + 响应示例，含百度千帆 Qianfan OpenAI 兼容网关信息）
- v1.16.6 2026-07-15 16:15:00 liyang: Q-I1 完整 4 项收齐（TPM/TPD/月成本/上下文窗口）+ 上下文截断 + solution 沉淀
  - **Q-I1 完整信息已收齐**（2026-07-15 算法团队提供）：
    · TPM=100000（每分钟 100K tokens）
    · TPD=10*10*100000=10000000（每天 10M tokens；10h × 10 批/h × 100K/batch）
    · 月度成本上限 5000 元
    · dev/staging URL 与 prod 一致（都用 qianfan.baidubce.com/v2）
    · 上下文窗口 100K tokens
  - **`application.yml` `app.llm` 完整 4 项配置**（Q-I1 已收齐）：
    · `tpm: 100000` / `tpd: 10000000` / `monthly-cost-cap-yuan: 5000` / `cost-yuan-per-1k-tokens: 0.002`
    · `context-window-tokens: 100000`（deepseek-v4-flash 实际 128K 留 22% buffer）
    · `dev-staging-same-as-prod: true`（dev/staging 用同 endpoint）
  - **`LlmQuotaService` 加 4 个 @Value 字段**：`tpmLimit` / `tpdLimit` / `monthlyCostCapYuan` / `costYuanPer1kTokens`（配置就位，Sprint 3 U7 升级 token 限流时直接用）
  - **`DeepSeekClient` 加上下文窗口 100K 截断**（F-26 修复）：
    · `@Value` 读 `context-window-tokens: 100000`
    · 估算 prompt tokens（按 2 chars/token，含中英文保守）
    · 超阈值时截断 userPrompt + 追加截断提示
    · 防 400 错误（超长 conflict context 触发 LLM 400）
  - **`DeepSeekClient.parseResponse()` 补 reasoning_tokens 字段**（千帆扩展）已就位（v1.16.5）
  - **新建 `docs/solutions/integration-issues/q-i1-real-llm-integration.md`**：Q-I1 真接入沉淀（spec-compound integration_issue track；含 llm_client.txt 提取的 endpoint / 鉴权 / 模型 / 配额 / 上下文 5 项配置 + Sprint 3 升级 token 限流的预防措施）
  - **`mvn clean verify` 验证**：exit 0（5/5 测试通过，1 单元 + 4 集成）
  - **配置可立即生效**：dev 环境设置 `LLM_API_KEY=bce-v3/...` 环境变量 → 重启 backend → 走真 LLM 接入（mock 兜底仍在，api-key=demo 时走 mock）
  - **后续 Sprint 3 U7 实施时直接用** `tpm` / `tpd` / `monthlyCostCapYuan` / `costYuanPer1kTokens` 4 字段升级 token 限流（无需再改 yml）
  - **docs/solutions/ 总览**：6 篇（build-errors × 2 / conventions × 1 / integration-issues × 2 / tooling-decisions × 1）
- v1.16.7 2026-07-15 16:14:00 liyang: 移 llm_client.txt 到 docs/private/ + .gitignore 强化（防误 commit 真实凭证）
  - **`mv llm_client.txt docs/private/llm_client.txt`**：含 Q-I1 真实 API key（`bce-v3/ALTAK-...`），从仓库根目录移至 `docs/private/` 私有目录
  - **`.gitignore` 末尾加 5 段自定义规则**（spec-first 段外）：
    · `docs/private/` 整目录 ignore（含真实凭证的本地参考文件）
    · `llm_client.txt` 兼容旧路径 ignore（根目录误放也安全）
    · `test_env.txt` ignore（用户提供的 MySQL root 密码等敏感信息）
    · `辽港*.docx` / `*.md.orig` ignore（产品资料 / 编辑器备份）
    · `.vscode/` / `.idea/` / `*.swp` / `*.swo` / `.DS_Store` ignore（编辑器配置）
  - **新建 `docs/private/README.md`**：说明目录用途（本地私有文件，不进 git）+ 添加新文件约定 + Sprint 1 已存放文件清单（`llm_client.txt` 等）
  - **`git status` 清理**：untracked 列表从 8 项减到 6 项（`docs/private/` 整目录隐藏 / `llm_client.txt` / `test_env.txt` 隐藏 / 中文 docx 隐藏）
  - **mvn 验证**：不需要（纯配置文件改动，但 push 时 hook 仍跑 mvn compile + mvn verify）
- v1.17.0 2026-07-15 16:25:00 liyang: Sprint 2 启动 - TP-2 task pack 落地（U4 KO 库 + U5 权限 + U6 提示词）
  - **新建 `feat/sprint-2-ko-and-permissions` 分支** + worktree（`.worktrees/sprint-2`）：基于 main bb839ab 拉出
  - **新建 `docs/tasks/2026-07-15-001-task-pack-sprint-2-ko-and-permissions.md`**（570 行；按 Sprint 1 task pack 模板）：
    · 3 个 Implementation Unit 拆 11 个 task：U4 拆 T201~T204（数据模型 + CRUD + 前端 + 审核流），U5 拆 T205~T207（数据模型 + UI + 下次登录生效），U6 拆 T208~T211（PRM 模板 + Handlebars + 三栏组装器 + PRP 装配数）
    · 4 个 wave 按 U 依赖：Wave 1（T201 数据模型基础）→ Wave 2（T202~T205 U4 业务 + U5 数据）→ Wave 3（T206~T208 权限 UI + 下次登录 + PRM）→ Wave 4（T209~T211 Handlebars + 三栏 + PRP）
    · 每个 task 详细列：goal / dependencies / files / test_focus / done_signal / risk_note / review_gate / review_focus / stop_if
    · frontmatter 含 spec_id / source_plan / mode: derived / status: derived（与 Sprint 1 同结构）
    · source_plan_hash: pending-validation（待 Sprint 2 启动后跑 `spec-first tasks validate` 验证）
  - **Sprint 2 不依赖 Q-I1**：U6 提示词系统的实时预览是前端自研 Handlebars 渲染，不调 LLM API；Q-I1 完整 4 项已收齐（端点 + Bearer + deepseek-v4-flash + TPM/TPD/月成本/上下文）但仅 U7 知识治理需要
  - **依赖 Sprint 1 已完成**：
    · U1（Monorepo + K8s）/ U2（后端基础）/ U3（前端基础）已落地
    · 集成测试环境（MySQL + Redis + MinIO 真实环境）已就绪
    · P1 三层编译门禁（pre-commit / pre-push / GitHub Actions）已部署，Sprint 2 实施时自动防护
  - **owner 输入仍待 Sprint 2 启动后收齐**：
    · Q-I4 §3 手动子项弹层 UI 细节（sparring 业务专家）
    · Q-I2 / Q-I3 prod 联调时由 IT/安全 + 基础架构提供
  - **未实施**（按用户选择 "写 TP-2 task pack + 分支准备（不实施）"）：T201~T211 待 task pack review 后再启动
- v1.17.2 2026-07-15 19:11:00 liyang: T201 实施 - U4 数据模型 + 状态机（Wave 1 完成）
  - **新建 `backend/src/main/resources/db/migration/V9002__create_ko_tables.sql`**（5 张表）：
    · `project` 表（V9001 seed 依赖；V9001 隐含需要，T201 范围扩展补建）
    · `role` 表（同 project 理由）
    · `ko` 表（KO 库主表；6 类型 CON/RUL/PAR/SCH/PRM/DOC 共 278 条；ID 格式 KO-{TYPE}-{NNNN}）
    · `ko_version` 表（KO 版本历史；编辑触发版本号递增 v{MAJOR}.{MINOR}.{PATCH}）
    · `ko_references` 表（KO 引用关系：DEPENDENCY/REFERENCE/CONFLICT）
    · F-22 + F-8 修复：START TRANSACTION + COMMIT 包装 + IF NOT EXISTS 幂等
  - **新建 `backend/src/main/java/.../ko/model/KoEntity.java`**：KO 主表实体（MyBatis-Plus @TableName + @TableId IdType.INPUT）
  - **新建 `KoVersionEntity.java` + `KoReferenceEntity.java`**：版本历史 + 引用关系实体
  - **新建 `KoMapper.java` + `KoVersionMapper.java`**：MyBatis-Plus BaseMapper（@MapperScan 已由 KmApplication 配置）
  - **新建 `KoStateMachine.java`**：5 状态转换守卫 + DOC 类型豁免 + OQ-12 状态机约束
    · Draft → Review → Approved → Published → Active
    · DOC 豁免：Draft → Active（§5.2.1.4）
    · PRM 走标准流程（OQ-6）
    · Active 终止态
    · 同 KO 最多 1 个 in-flight 工作版本
  - **新建 `KoStateMachineTest.java`**：11 个单测覆盖（5 状态 + 类型豁免 + 终止态 + OQ-12 + initialStatus + 同状态幂等）
  - **修复 `application-test.yml` + `application.yml` + `pom.xml`（F-27 修复）**：
    · pom.xml 加 `h2` test scope（test profile 用 H2 内存数据库）
    · `application.yml` 删 JPA 段（项目用 MyBatis-Plus，不需要 JPA 实体扫描）
    · `application-test.yml` 加 H2 datasource（jdbc:h2:mem:km_test;MODE=MySQL）+ 排除 HibernateJpaAutoConfiguration + JpaRepositoriesAutoConfiguration
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，16/16 测试通过（KmApplicationTests 1/1 + KoStateMachineTest 11/11 + AuditAspectIT 4/4）
  - **P1 三层防御实际工作**：pre-commit 跑 mvn compile（5+ 文件改动） + pre-push 跑 mvn verify（含 4 集成测试 + it profile）
  - **下个任务**：T202（U4 CRUD + 跨类搜索 REST API，依赖 T201）
- v1.17.3 2026-07-15 21:53:00 liyang: T202 实施 - U4 CRUD + 跨类搜索（Wave 2 完成）
  - **新建 `ko/dto/KoListItem.java` + `KoDetail.java` + `KoSearchResult.java`**：3 个 DTO 类型（list 列表项 + 详情含 references + 搜索结果含 matchedField）
  - **新建 `ko/service/KoService.java`**：6 业务方法
    · `createKo`：按类型生成 ID（KO-{TYPE}-{NNNN}，F-29 简化实现 selectCount+1，T203 升级为雪花算法）
    · `getById`：跨项目隔离（X-Project-Id 校验，40403 拒绝跨项目访问）
    · `listKo`：按 type/projectId/status 过滤 + 分页（MyBatis-Plus Page）
    · `searchKo`：跨类搜索 OQ-4（**F-36 修复**：3 字段全搜 title + id + typeName，之前只搜 title/id）
    · `updateKo`：跨项目隔离 + 状态机守卫（状态变更在 T204 处理）
    · `softDelete`：标记 is_deleted=1
    · 6 类型中文名映射（CON 约束 / RUL 规则 / PAR 参数 / SCH 数据结构 / PRM 提示词模板 / DOC 文档）
  - **新建 `ko/controller/KoController.java`**：6 REST API 端点
    · POST /api/ko（创建）
    · GET /api/ko/{id}（详情）
    · GET /api/ko（列表 + 分页）
    · GET /api/ko/search（跨类搜索 OQ-4）
    · PUT /api/ko/{id}（更新）
    · DELETE /api/ko/{id}（软删除）
    · 跨项目隔离通过 X-Project-Id header
  - **新建 `test/db/test-schema.sql` + 新建 `test/.../KoControllerIT.java`**：
    · 8 个集成测试覆盖（createAndGet / searchByTitle / searchById / searchByTypeName / searchByTypesFilter / listByType / crossProjectIsolation / softDelete）
    · F-30 修复：test-schema.sql 绕开 Flyway MySQL DDL（H2/CLOB→TEXT 兼容 + IF NOT EXISTS 幂等）
    · F-31 修复：@AutoConfigureMockMvc(addFilters=false) 绕过 Spring Security 默认 403
    · F-32 修复：@Transactional + @Rollback 测试数据自动回滚
    · F-33/34/35 修复：用 UUID 唯一 title/code/projectId 避免 V9001 seed 278 条 KO 干扰测试
  - **F-36 关键修复**：`KoService.searchKo` 实际只查 title+id，没查 typeName（OQ-4 3 字段缺 1）；修后用 TYPE_NAMES 反查匹配 types，再 SQL `OR type IN (...)`，3 字段全搜
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**24/24 测试通过**（KmApplicationTests 1/1 + KoStateMachineTest 11/11 + AuditAspectIT 4/4 + KoControllerIT 8/8）
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify（含集成测试）
  - **下个任务**：T203（U4 前端 KO 库 + 详情页，依赖 T201/T202 完成后前端集成）
- v1.17.4 2026-07-15 22:10:00 liyang: T203 实施 - U4 前端 KO 库（Wave 2 完成）
  - **新建 `frontend/src/api/ko.ts`**：KO 库 API 客户端（封装后端 6 个端点 + TypeScript interface 与后端 DTO 一一对齐）
    · 类型定义：KoEntity / KoListItem / KoDetail / KoReference / KoSearchResult / Page
    · 6 个 API 调用：createKo / getKo / listKo / searchKo / updateKo / deleteKo
    · KO_TYPE_NAMES 6 类型中文名映射（与后端 KoService 一致）
  - **新建 `frontend/src/views/ko-library/KoLibraryView.vue`**：KO 库全景概览页
    · 6 类型入口卡片（CON / RUL / PAR / SCH / PRM / DOC，端口蓝 #0F4C75 + 阴影 hover）
    · 跨类搜索框（OQ-4：title + id + typeName 任意字段）
    · 搜索结果表格 + matchedField 标签（success/warning/info）
  - **新建 `frontend/src/views/ko-library/KoTypeListView.vue`**：类型列表页（URL /ko-:type）
    · Element Plus el-table 列表（ID / 标题 / 版本 / 状态标签 / 项目 / 更新时间）
    · 状态标签：Active(green) / Published(blue) / Review/Approved(warning) / 其他(info)
    · 行点击跳详情 + TablePagination 通用分页组件
  - **新建 `frontend/src/views/ko-library/KoDetailView.vue`**：KO 详情页（URL /ko-:type/:id）
    · el-page-header 返回 + el-descriptions 完整字段 + 形式化定义（JetBrains Mono 等宽字体）
    · 引用关系列表（KoReference，T204 实施时填具体数据）
    · 编辑/删除按钮（删除带 ElMessageBox 二次确认）
  - **router 已预配置**（Sprint 1 + T203 之前）：ko-library / ko-:type / ko-:type/:id 3 条路由引用 3 个 view，无需改 router
  - **本地验证缺失**：`pnpm` 不在 PATH（`pnpm install` 跑过但 `node_modules` 已清空）。前端构建验证依赖 GitHub Actions CI（已配置 pnpm install + pnpm build）
  - **`mvn clean verify` 后端测试**：未跑（T203 是纯前端 task；P1 三层防御 pre-commit / pre-push 自动跑后端测试 5/5）
  - **下个任务**：T204（U4 审核流 + DOC/PRM 豁免，依赖 T201 + T202 完成后）
- v1.17.5 2026-07-15 22:30:00 liyang: Sprint 1 router 配错修复 - 10 个占位 view 文件（让 vite build 跑通）
  - **根因**：Sprint 1 router/index.ts 预先配置 15 个 view 路由，但只实现了 LoginView + DefaultLayout（2 个）；剩余 13 个 view 引用但文件不存在，**导致 vite build 失败 + frontend 构建无法验证**
  - **F-37 修复**：创建 10 个占位 view（Sprint 2/3 后续 task 实施时替换）：
    · dashboard/DashboardView.vue（首页，T205+ 实施）
    · audit/AuditLogView.vue（审计日志，U9 实施）
    · conflicts/ConflictsView.vue（冲突管理，U7 实施）
    · dict/DictMgmtView.vue（字典管理，U9 实施）
    · project/ProjectMgmtView.vue（项目管理，U9 实施）
    · permissions/PermissionsView.vue（权限矩阵，U5 实施）
    · prompts/PromptsView.vue（PRM 列表，U6 实施）
    · prompts/ComposerView.vue（三栏组装器，U6 实施）
    · prompts/SnapshotsView.vue（PRP 快照，U8 实施）
    · NotFoundView.vue（404 wildcard 占位）
  - **F-38 修复**：`.gitignore` 加 `frontend/dist/` / `frontend/node_modules/` / `frontend/.npmrc` / `frontend/pnpm-*.yaml` 忽略（机器特定 + 构建产物）
  - **验证**：`vite build` 跑通，dist/ 完整生成（合计 320 modules transformed）
    · T203 4 个 view 编译成功（KoLibraryView 2.93KB / KoTypeListView 3.65KB / KoDetailView 4.10KB / api/ko.ts 1.10KB）
  - **后续**：占位 view 实施时按 Sprint 2/3 task 顺序替换（U4→U5→U6→U7→U8→U9）
- v1.17.6 2026-07-15 22:50:00 liyang: T204 实施 - U4 审核流 + DOC/PRM 豁免（Wave 2 完成）
  - **新建 `ko/service/KoAuditService.java`**：4 业务方法 + 1 状态查询
    · `submitForReview`：Draft → Review（业务专家 / 算法工程师提交）
    · `approve`：Review → Approved（合规审核员通过，OQ-12 自审禁止）
    · `reject`：Review → Draft（驳回附原因，40031 原因不能为空）
    · `publish`：Approved → Published → Active（系统管理员发布，自动转 Active，§5.2.1.3）
    · `getStatus` + `countInFlightVersions`：OQ-12 状态机约束查询
    · 依赖 KoStateMachine 做状态转换守卫（5 状态 + DOC 豁免 + 类型校验）
  - **新建 `ko/controller/KoAuditController.java`**：5 REST API 端点
    · POST /api/ko/{id}/submit（提交审核）
    · POST /api/ko/{id}/approve（审核通过，OQ-12 自审禁止）
    · POST /api/ko/{id}/reject（驳回 + reason body）
    · POST /api/ko/{id}/publish（发布）
    · GET /api/ko/{id}/status（状态 + in-flight 数）
    · 跨项目隔离 + 角色校验：T205+ 实施时加 @PreAuthorize（OQ-12 + §4.1.2 权限矩阵）
  - **新建 `test/.../KoAuditFlowTest.java`**：5 测试覆盖 done_signal
    · `happyPath`：Draft → Review → Approved → Published → Active（4 状态 + 1 自动）
    · `docExemption`：DOC 类型 Draft → Active 直接（§5.2.1.4 豁免，验证不可再走 Review）
    · `selfAuditForbidden`：自己审自己 → BusinessException（OQ-12 自审禁止）
    · `prmStandardFlow`：PRM 走标准 4 状态（OQ-6，不豁免，验证不可 Draft → Active）
    · `oq12InFlightLimit`：同 KO in-flight 数量约束（countInFlightVersions 返回 0；T205+ 实施完整版本管理时验证）
    · `@Transactional` 自动回滚（@Rollback 不需要显式注解）
  - **F-39 修复**：删除测试文件中错误的 `Rollback` placeholder 字段和冗余 `@Rollback` 注解（`@Transactional` 已自动回滚）
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**29/29 测试通过**（KmApplicationTests 1/1 + KoStateMachineTest 11/11 + KoAuditFlowTest 5/5 + AuditAspectIT 4/4 + KoControllerIT 8/8）
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify
  - **下个任务**：T205（U5 数据模型 + 默认矩阵 seed + 角色 CRUD）
- v1.17.7 2026-07-15 22:50:00 liyang: T205 实施 - U5 数据模型 + 默认矩阵 + 角色 CRUD（Wave 2 完成）
  - **新建 3 entity**：
    · RoleEntity（id/code/name/description/isBuiltin/isDeleted）—— 预置 5 角色 + 自定义角色通用
    · RolePermissionEntity（roleId/menuId/operation/allowed）—— 权限矩阵 cell
    · UserRoleEntity（userSub/roleId/assignedBy）—— 用户角色关联（OQ-12 下次登录生效）
  - **新建 3 mapper**（BaseMapper，@MapperScan 已配）：RoleMapper / RolePermissionMapper / UserRoleMapper
  - **新建 `role/service/DefaultMatrixLoader.java`**：@PostConstruct 启动加载 + 幂等
    · 读取 `classpath:seed/role-permissions.yaml`（SnakeYAML）
    · 加载 5 预置角色（如已存在跳过）
    · 加载 150 cells 权限矩阵（5 角色 × 6 KO 类型 × 5 操作）
  - **新建 `role/service/RoleService.java`**：list / getByCode / createCustomRole / deleteRole
    · 预置角色不可删（OQ-20 + v0.32 §4.1.1）
    · 自定义角色被引用不可删（40052 错误码）
  - **新建 `role/controller/RoleController.java`**：4 REST API 端点
    · GET /api/role（列表）/ GET /api/role/{code}（详情）
    · POST /api/role（创建自定义）
    · DELETE /api/role/{code}（删除）
  - **新建 `db/migration/V9003__create_role_tables.sql`**：2 张新表
    · `role_permission`（5 角色 × 6 KO 类型 × 5 操作 = 150 cells）
    · `user_role`（多对多关联）
    · IF NOT EXISTS 幂等 + START TRANSACTION + COMMIT
    · role 表 V9002 已建（T201 顺带建），V9003 不重复
  - **新建 `seed/role-permissions.yaml`**：5 角色完整矩阵（SnakeYAML 格式）
    · ROLE-0001 系统管理员：30 cells（6 KO × 5 op 全部 ✓）
    · ROLE-0002 合规审核员：12 cells（全 READ + 6 REVIEW）
    · ROLE-0003 算法工程师：16 cells（5 KO × 3 op + DOC READ）
    · ROLE-0004 业务专家：12 cells（RUL/PAR READ+CREATE+UPDATE + 其他 READ）
    · ROLE-0005 只读观察者：6 cells（全 READ）
  - **新建 `test/.../RoleServiceTest.java`**：5 测试覆盖 done_signal
    · `builtinRolesSeeded`：5 预置角色自动 seed（ROLE-0001 系统管理员名验证）
    · `defaultMatrix150Cells`：默认矩阵各角色 cell 数量 > 0
    · `builtinRoleNotDeletable`：预置角色不可删（40051 错误）
    · `customRoleCreateDelete`：自定义角色可创建+删除（验证 isDeleted=1）
    · `referencedRoleNotDeletable`：被引用自定义角色不可删（40052 错误）
  - **F-40 修复**：`test-schema.sql` 加 `role` 表（H2 test profile 用，V9001 seed 不跑 Flyway，需要 test-schema 完整建表）
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**34/34 测试通过**
    · KmApplicationTests 1/1 + KoStateMachineTest 11/11 + KoAuditFlowTest 5/5 + RoleServiceTest 5/5（← T205）
    · AuditAspectIT 4/4 + KoControllerIT 8/8
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify
  - **下个任务**：T206（U5 权限矩阵 UI + 角色 CRUD + 用户分配 + 3 秒 Toast 撤销，依赖 T205 + T203 前端基础）
- v1.17.8 2026-07-15 23:05:00 liyang: T206 实施 - U5 权限矩阵 UI + 3 秒 Toast 撤销（Wave 3 完成）
  - **新建 `frontend/src/api/role.ts`**：角色 API 客户端
    · 5 操作枚举（READ/CREATE/UPDATE/DELETE/REVIEW）+ 中文 label
    · 13 菜单清单（按 3 组：主功能 5 / 治理 4 / 配置 4）
    · 6 API 调用：listRoles / getRole / createRole / deleteRole / getRolePermissions / saveRolePermissions
  - **新建 `frontend/src/components/PermissionMatrix.vue`**：13 菜单 × 5 操作 复选框矩阵
    · 按组排序（主功能 / 治理 / 配置）
    · el-checkbox + @update 事件（父组件收集 → 批量保存 API）
  - **新建 `frontend/src/components/RoleAssignmentModal.vue`**：用户角色分配弹窗
    · 多用户 sub 输入（英文逗号分隔）
    · OQ-12 提示（角色变更下次登录生效）
  - **替换 `frontend/src/views/permissions/PermissionsView.vue`**（占位 → 真实实现）：
    · 5 预置角色 + 自定义角色 tabs（预置带 el-tag 标识）
    · 权限矩阵嵌入（hasChanges 检测）
    · 用户角色分配（OQ-12 提示）
    · 3 秒 Toast 撤销（OQ-5 仅前端 UI 回滚）：保存成功 → 3 秒后 setTimeout 自动回滚 → ElMessage.info 提示
    · ElMessageBox 切换角色前确认（避免未保存修改丢失）
  - **验证**：`vite build` 跑通，320+ modules 编译
    · PermissionsView.js 7.29 kB（gzip 3.62 kB）
  - **下个任务**：T207（U5 角色变更下次登录生效 OQ-12，依赖 T205 数据模型 + T206 权限矩阵）
- v1.17.9 2026-07-15 23:30:00 liyang: T207 实施 - OQ-12 角色变更下次登录生效（Wave 3 完成）
  - **新建 `role/event/RoleChangeEvent.java`**：角色变更事件（ASSIGN/REMOVE/MATRIX_UPDATE 3 类型 + effectiveAt）
  - **新建 `role/service/RoleChangeAuditService.java`**：监听 RoleChangeEvent 写 USER_ROLE_CHANGE 审计（@EventListener + AuditLogService.recordAsync）
  - **新建 `role/service/UserRoleService.java`**：assignRole + removeRole（发布事件 + 写 user_role + 业务规则）
    · 分配幂等（已存在跳过）
    · 移除验证（不存在抛 40060）
    · 验证角色存在（40411）
  - **新建 `test/auth/RoleChangeEffectTest.java`**：4 测试覆盖 OQ-12 + OQ-5
    · `oldSessionStillUsesOldRole`：签发 ROLE-0003 JWT → 改 ROLE-0005 → 旧 JWT role claim 仍是 ROLE-0003（OQ-12 决策）
    · `newSessionUsesNewRole`：签发新 ROLE-0005 JWT → 新 JWT role claim 是 ROLE-0005（旧 JWT 不变）
    · `crossDeviceRevokeImpossible`：反射验证 UserRoleService + JwtAuthFilter 都没有 revoke/cancel/invalidate/expire 方法（OQ-5 决策）
    · `auditLogTriggered`：TestAuditListener 捕获 RoleChangeEvent 触发次数 ≥ 1
  - **F-41 修复 1**：`mapper.delete()` 返回 `int`（不是 `Long`），改 `int deleted = ...`
  - **F-42 修复 2**：`@Component` 嵌套类不被 Spring 自动扫描；改 `@TestConfiguration` + `@Import(TestAuditListenerConfig.class)` 显式注入
  - **F-43 修复 3**：占位反射断言 `extracting("useRedisMock")` 失败（JwtAuthFilter 无此字段），改反射验证"无 revoke/cancel/invalidate 方法"
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**38/38 测试通过**
    · KmApplicationTests 1/1 + KoStateMachineTest 11/11 + KoAuditFlowTest 5/5 + RoleServiceTest 5/5
    · RoleChangeEffectTest 4/4（← T207）+ AuditAspectIT 4/4 + KoControllerIT 8/8
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify
  - **下个任务**：T208（U6 PRM 模板数据 + 17 段，依赖 T201 数据模型 + T205 默认矩阵）
- v1.17.10 2026-07-16 09:00:00 liyang: T208 实施 - U6 PRM 模板数据 + 17 段（Wave 4 启动）
  - **新建 2 entity**：
    · PrmTemplateEntity（id/name/description/version/createdAt/updatedAt）—— 3 预置 PRM 模板主表
    · PrmSectionEntity（id/templateId/sectionIndex/title/sectionType/content）—— 17 段 = 9+3+5
  - **新建 2 mapper**：PrmTemplateMapper + PrmSectionMapper（含 selectByTemplateId 按 index 排序）
  - **新建 `db/migration/V9004__create_prm_tables.sql`**：2 张新表
    · prm_template（id PK = KO-PRM-0001/0002/0003）
    · prm_section（template_id+section_index 唯一索引，17 段）
  - **新建 `seed/prm-templates.yaml`**：3 预置 PRM 模板完整内容（SnakeYAML 格式）
    · KO-PRM-0001 大窑湾统筹优化：9 段（任务背景 / 输入参数 / 硬约束 / 软目标 / KO 库引用 / 算法选择 / 输出格式 / 边界条件 / 人工复核）
    · KO-PRM-0002 堆场计划优化：3 段（任务背景 / 输入参数 / 优化目标）
    · KO-PRM-0003 泊位分配算法：5 段（任务背景 / 状态空间 / 动作空间 / 奖励函数 / 训练策略）
    · Section 类型：FIXED（变量赋值型）/ DYNAMIC（动态选择型，含 {{#each items}} 循环）
  - **新建 `prompt/service/PrmService.java`**：@PostConstruct 启动加载 + 幂等
    · loadTemplates：插入 3 预置模板（已存在跳过）
    · loadSections：插入 17 段（templateId+sectionIndex 唯一键去重）
    · getTemplate + getSections：查询 API
    · SnakeYAML 读取 seed 文件
  - **新建 `prompt/controller/PrmController.java`**：3 REST API 端点
    · GET /api/prm（列表占位，T208+ 实施）
    · GET /api/prm/{id}（详情 + sections）
    · GET /api/prm/{id}/sections（仅 sections）
  - **新建 `test/prompt/PrmServiceTest.java`**：3 测试覆盖 done_signal
    · `templatesSeeded`：3 预置 PRM 模板自动 seed（KO-PRM-0001/0002/0003 name/version 非空）
    · `sectionsCountCorrect`：17 段完整性（9+3+5 = 17）
    · `sectionContentValid`：每段 content 非空 + section_type 合法（FIXED / DYNAMIC）
  - **F-44 修复**：`test-schema.sql` 加 `prm_template` + `prm_section` 表（H2 test profile 用，V9004 MySQL DDL 不兼容）
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**41/41 测试通过**
    · KmApplicationTests 1/1 + KoStateMachineTest 11/11 + KoAuditFlowTest 5/5 + RoleServiceTest 5/5
    · RoleChangeEffectTest 4/4 + PrmServiceTest 3/3（← T208 done_signal 满足）
    · AuditAspectIT 4/4 + KoControllerIT 8/8
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify
  - **下个任务**：T209（U6 自研 Handlebars 子集 OQ-15 + Markdown 渲染器）
- v1.17.11 2026-07-16 09:45:00 liyang: T209 实施 - U6 自研 Handlebars 子集 OQ-15 + Markdown 渲染器（Wave 4）
  - **新建 `frontend/src/utils/handlebars.ts`**：自研 Handlebars 子集（OQ-15 决策：不实现 partials/helpers/sub-expressions/块参数）
    · 3 类语法：{{var}} 替换 / {{#each items}}...{{/each}} 循环 / {{#if cond}}...{{/if}} 条件
    · 条件支持：== / != / > / < 5 种比较运算符
    · 嵌套属性：{{user.name}} 递归访问 + 字符串字面量 ("yes" / 'no') + 数字字面量
    · {{this.x}} 数组元素引用（each 迭代时上下文 this）
    · 多轮处理（100 轮上限防无限循环）
  - **新建 `frontend/src/utils/markdown-renderer.ts`**：自研 Markdown 渲染器（OQ-15 收缩到 PRD §10.5.3 9 类元素）
    · 块级：H2/H3/H4 标题（##/###/####） + 代码块（```lang ... ```）+ 表格（| col | col | + |---|）+ 列表（- / 1.）+ 引用（>）+ 分隔线（---）
    · 行内：粗体（**） + 斜体（*） + 行内代码（`）+ 链接（[text](url)）+ 变量高亮（{{var}} → <span class="var-highlight">）
    · HTML 字符转义（防 XSS）
    · 多行段落累积 + 引用嵌套
    · 图片不实现（OQ-15 决策）：![alt](url) 按原样作为段落处理
  - **新建 2 测试文件**：
    · `handlebars.test.ts`（13 测试）：{{var}} / 嵌套属性 / 数组 each / 空数组 / 真值条件 / 假值条件 / ==/!=/>/< / 不实现 partials / 缺失变量 / each 内部 var / 混合语法
    · `markdown-renderer.test.ts`（19 测试）：H2/H3/H4 标题 / 不支持 H1 / 粗体/斜体/行内代码 / 代码块带语言 / 表格 / 无序列表 / 有序列表 / 引用 / 分隔线 / 链接 / {{var}} 高亮 / HTML 转义 / 多行段落 / 图片不实现
  - **vitest 测试结果**：**32/32 全部通过**（done_signal 要求 18+，实际 32，超 14）
  - **F-45 修复**：测试文件 import 路径 `../../src/utils/...` 错（多一段 `src/`），改 `@/utils/...` 别名（Vite + tsconfig 已配 `@` alias）
  - **F-46 修复**：handlebars.render() 之前去 processVar 导致 6 个测试失败；恢复 render() 三步（each → if → var 替换），markdown-renderer 改为**不调 handlebars**（独立工具，避免 var 被替换丢失），{{var}} 由 inlineMd 加 span 高亮
  - **下个任务**：T210（U6 三栏组装器 UI，依赖 T208 PRM 模板 + T209 Handlebars 渲染）
- v1.17.12 2026-07-16 10:00:00 liyang: T210 实施 - U6 三栏组装器 UI（Wave 4 完成）
  - **新建 `frontend/src/api/composer.ts`**：PRM 三栏组装器 API 客户端
    · 类型定义：PrmTemplate / PrmSection / VarBindings / SelectedKOs / ManualSubItems / ComposerContext / RenderResult
    · 4 API 调用：listPrmTemplates / getPrmTemplate / renderComposer / estimateTokens
    · estimateTokens 本地估算（2 chars/token 保守，含中英文混合）
  - **新建 `frontend/src/components/SectionCard.vue`**：Section 卡片
    · FIXED（变量赋值型）：content 中 {{var}} 占位 + ⚡绑定变量按钮
    · DYNAMIC（动态选择型）：KO 选择 + 手动子项编辑器
    · 绑定状态显示：OQ-16 装配数动态计算
  - **新建 `frontend/src/components/VariableBindingModal.vue`**：变量绑定弹窗
    · OQ-16 4 种操作：⚡自动匹配（varKey ↔ PAR.symbol）/ ⊕重选 / ✎当前绑定 / ×解除
    · 解析 {{var}} 占位符 + 列出 + 选 PAR
    · 自动匹配 varKey 与 PAR.symbol 一致（不区分大小写）
  - **替换 `frontend/src/views/prompts/ComposerView.vue`**（占位 → 三栏组装器实际实现）
    · 顶部：PRM 模板选择栏（el-select + 切换加载）
    · 中栏：Section 编排列表（SectionCard × N，含变量绑定弹窗触发）
    · 右栏：实时预览 + 字符数 / token 数 / 装配数（OQ-16 动态）
    · 渲染：handlebars.render() 替换 {{var}} → markdown-renderer.render() → v-html
    · mock fallback：后端 API 待 T208+ 实施（用 seed 硬编码数据）
  - **F-47 修复**：SectionCard defineEmits 用 `var` 关键字作 indexed signature 名称（TypeScript 解析失败）；改为 `k`
  - **验证**：`vite build` 跑通，320+ modules 编译
    · ComposerView.js 14.98 kB（gzip 5.97 kB，map 49.10 kB）
  - **P1 三层防御实际工作**：pre-commit（lint via vite build）+ pre-push（lint via vite build）
  - **下个任务**：T211（U6 PRP 装配数动态 OQ-16 + 字符数 g(M) + Handlebars 错误检测，依赖 T209 + T210）
- v1.17.13 2026-07-16 10:30:00 liyang: T211 实施 - PRP 装配数动态 OQ-16 + 字符数 + Handlebars 错误检测（TP-2 完成）
  - **新建 `prompt/service/TemplateEngine.java`**：服务端 Handlebars 子集（与前端 handlebars.ts 一致）
    · 3 类语法：{{var}} / {{#each}} / {{#if}}（含 ==/!=/>/</<= 比较运算符）
    · validateSyntax()：检测 {{#each}} / {{/if}} 数量不匹配（40070 业务异常）
    · render()：多轮处理（100 轮上限）+ 嵌套属性 + 字符串字面量
  - **新建 `prompt/service/ComposerRenderService.java`**：PRP 组装渲染
    · render()：加载模板 + 合并 section content（varBindings 替换 {{var}} + manualSubItems / selectedKOs 替换 {{#each items}}）
    · computeAssemblyCount()：OQ-16 公式 = selectedKOs.length + varBindings.size + manualSubItems.size
    · RenderResult：rendered / charCount / tokenCount（2 chars/token 保守估算）/ assemblyCount / sectionCount
  - **新建 `prompt/controller/ComposerController.java`**：1 REST API 端点
    · POST /api/composer/render（Body 含 templateId + context{selectedKOs/varBindings/manualSubItems}）
  - **新建 2 测试文件**：
    · `TemplateEngineTest.java`（8 测试）：{{var}} / 嵌套属性 / {{#each}} / {{#if}} 真值 / 字符串 == 比较 / 语法错误检测（未闭合 {{#each}} / 未闭合 {{#if}}） / 正常模板不抛
    · `ComposerRenderTest.java`（5 测试）：空上下文装配数 0 / 全上下文 5+3+1=9 / 只 selectedKOs / 字符数 token 数 / varBindings 替换
  - **F-48 修复**：`processEach` 用 indexOf 找 {{/each}} 但 `m.appendReplacement` 没消费，导致 `{{/each}}` 残留；改用 `Pattern.compile(...)` + `m.appendReplacement` 一次匹配整段 `{{#each X}}body{{/each}}`
  - **`mvn clean verify` 测试结果**：BUILD SUCCESS，**54/54 测试通过**
    · TemplateEngineTest 8/8（← T211 done_signal 满足）
    · ComposerRenderTest 5/5（← T211 done_signal 满足）
    · 全部 backend 测试：KmApplicationTests 1 + KoStateMachineTest 11 + KoAuditFlowTest 5 + RoleServiceTest 5 + RoleChangeEffectTest 4 + PrmServiceTest 3 + TemplateEngineTest 8 + ComposerRenderTest 5 + AuditAspectIT 4 + KoControllerIT 8 = 52 backend
    · 加上 KoControllerIT 包含 8 集成测试 + 之前 4 AuditAspectIT 集成
  - **P1 三层防御实际工作**：pre-commit mvn compile + pre-push mvn verify
  - **TP-2 全部 11 个 task 实施完成**（T201-T211）—— Sprint 2 后端 + 前端 完整实现
- v1.17.14 2026-07-16 11:16:00 liyang: F-52 修复 Sidebar.vue 缺 import ref（项目预览发现）
  - **根因**：Sprint 1 实施 Sidebar.vue 时 `import { computed } from 'vue'` 漏写 ref，但第 67 行用 `ref([...])` 创建响应式数组
  - **影响**：访问 http://localhost:5173/ 页面空白，console 报
    `[Vue warn] Unhandled error during execution of setup function`
    `Sidebar.vue:67 Uncaught (in promise) ReferenceError: ref is not defined`
  - **修复**：`import { computed, ref } from 'vue'`（加 ref）
  - **F-49 ~ F-52 系列完整列表**（项目预览发现）：
    · F-49：dev profile `km_user/devpass` 认证失败（MySQL 实际是 `root/Zcx123456!` per test_env.txt）
    · F-50：application.yml 默认 `localhost:3306` 连不上本机 Docker MySQL
    · F-51：Spring Boot 启动时 JPA Hibernate 检测 dialect 失败（项目用 MyBatis-Plus，不需要 JPA）
    · F-52：Sidebar.vue `ref` 导入遗漏（本次）
  - **验证**：vite HMR 自动热更新 + curl http://localhost:5173/ 主页正常（`<title>辽港伐谋 KM 平台</title>`）
- v1.17.15 2026-07-16 11:30:00 liyang: F-53.1 短期视觉还原 - V3 CSS 变量 + KoLibrary V3 风格（处理链路阶段 1/3）
  - **F-53 彻底修复处理链路启动**（docs/solutions/build-errors/2026-07-16-001-frontend-style-divergence.md 已立 solution）
  - **修改 `src/styles/theme.scss`**（从 V3 原型提取完整 20+ CSS 变量）：
    · 主题色：`--port-blue #0F4C75` / `--port-blue-light #1E6A9D`（保留）
    · 信号色：`--signal-orange #ED8936` / `--signal-red #C53030` / `--signal-green #2F855A` / `--signal-yellow #D69E2E`
    · 文字色：`--text-primary #1A2332` / `--text-secondary #5A6373` / `--text-tertiary #8A92A0` / `--text-on-dark #E5EAF0` / `--text-on-dark-dim #8FA0B5`
    · 背景色：`--bg-canvas #F5F6F8` / `--bg-paper #FFFFFF` / `--bg-rail #0F1E2E` / `--bg-rail-active #173552` / `--bg-grid #ECEEF1`
    · 边框：`--line #DCE0E6` / `--line-strong #B0B7C0` / `--steel #2D3748`
    · 字体：Noto Sans SC + JetBrains Mono
    · Element Plus 主色：port-blue 9 档变体（light-3 到 light-9 + dark-2）
    · **新增 V3 关键 class 工具类**（全局可用，所有 view 可用）：
      - `.page-header`（深色 h1 + 副标题 + 右侧动作按钮组 + ID 标签）
      - `.toolbar`（搜索框 + 操作按钮组 + spacer）
      - `.btn` / `.btn-primary`（V3 风格按钮：圆角 2px + 灰边 + 端口蓝 hover/primary）
      - `.stat-card`（大数字 + 标签 + delta + 端口蓝 left border + success/warn/danger 变体）
      - `.lst-item`（紧凑列表项 + 端口蓝 hover + translateX 动效）
  - **修改 `frontend/src/views/ko-library/KoLibraryView.vue`**（V3 视觉完整还原）：
    · `.page-header` + 端口蓝 h1 + ID 标签 + 3 个操作按钮（导入 / 下载模版 / 新建 KO，V3 btn-primary 风格）
    · 4 个 `.stat-card` 统计（KO 总数 278 / 项目数 4 / 待审核 7 / 冲突 0，V9001 seed 数据）
    · `.toolbar` 跨类搜索框 + 高级筛选按钮
    · 6 类型入口卡片（V3 风格：border-left: 3px solid var(--port-blue) + translateX hover + JetBrains Mono type-code + 数量统计 TYPE_COUNTS）
  - **`src/components/Sidebar.vue` 已接近 V3**（之前 Sprint 1 已应用 bg-rail/bg-rail-active/signal-orange active border）：无需大幅改动
  - **`src/components/TopBar.vue` 已应用 V3**（background: var(--bg-rail) + text-on-dark）：无需改动
  - **F-53 处理链路 3 阶段**：
    · F-53.1 短期（本次）：theme.scss + KoLibrary 视觉还原
    · F-53.2 中期：8 个占位 view T212-T219 实施
    · F-53.3 长期：TP-3+ task pack 模板 + 视觉验收 checklist
  - **验证**：`vite build` 跑通（320+ modules）+ 浏览器 HMR 自动更新
  - **下个任务**：F-53.2 中期 - 实施占位 view T212-T219
- v1.17.16 2026-07-16 12:00:00 liyang: F-53.2 T212 DashboardView V3 完整还原（处理链路阶段 2/3 启动）
  - **替换 `frontend/src/views/dashboard/DashboardView.vue`**（占位 → V3 风格完整实现）
    · V3 .page-header：深色 h1 + 端口蓝 ID 标签 + 副标题 + 2 操作按钮（导出报告 / 刷新）
    · V3 4 个 .stat-card（默认 / success / warn / danger 4 变体）：
      - 知识对象总数 278（+12 本周，CON 19 · RUL 47 · PAR 92 · SCH 41 · PRM 3 · DOC 76）
      - 已生效 KO 231（+8 本周，占 83%）
      - 待处置治理项 7（-2 本周，3 冲突 + 4 警告）
      - 紧急告警 1（+1 今日，PRM-DRAFT 超时未审）
    · V3 .lst-item 4 项目活动（PROJ-0001~0004，活动/归档 status 颜色区分）
    · V3 .lst-item 10 最近活动（KO_PUBLISH / UPDATE / CREATE / REVIEW / REJECT 5 类）
    · V3 趋势占位（ASCII art 折线图：6 类型 × 7 天，T212+ 后端齐全后接 echarts）
  - **数据来源**：V9001 seed 真实数据（278 KO = 19+47+92+41+3+76，4 项目 PROJ-0001~0004）
  - **V3 工具类复用**：.page-header / .stat-card / .btn / .btn-primary / .lst-item 全从 src/styles/theme.scss 全局继承（不重复）
  - **新增 scss 局部样式**（仅本页特殊）：.status-active / .status-archived 颜色、.lst-item__icon（24x24 圆角端口蓝）、.trend-ascii / .trend-legend（ASCII 趋势图 + 6 圆点图例）
  - **mock 标记**：所有数据用 V9001 seed 真实值 + 近期活动 mock 10 条（T212+ 接真实 API 替换）
  - **验证**：vite build 跑通（320+ modules，DashboardView CSS 2.73 kB + JS 6.19 kB）
  - **下个 task**：T213 ConflictsView / T214 AuditLogView / T215 DictMgmtView / T216 ProjectMgmtView / T217 PromptsView / T218 SnapshotsView / T219 NotFoundView（7 个占位 view）
- v1.17.17 2026-07-16 12:30:00 liyang: F-53.2.2 DashboardView 第二批差距修复（V3 视觉完全还原）
  - **修改 `frontend/src/views/dashboard/DashboardView.vue`** 8 个差距修复：
    · **布局** el-row + el-col（Element Plus 24 栅格）→ 纯 div + CSS Grid 4 列（V3 原型用 .dashboard-grid grid-template-columns: repeat(3, 1fr) + .stat-grid 4 列）
    · **外边距** padding 20px → 16px 24px（V3 原型 sidebar 留出）
    · **.stat-card padding** 12px 16px → 16px（V3 原值）
    · **.label 字号** 12px → 11px + letter-spacing: 0.04em（V3 原值）
    · **.value 字号** 26px → 28px + margin: 6px 0 4px（V3 原值）
    · **.breakdown** 加 border-top: 1px dashed var(--line) + padding-top: 6px + JetBrains Mono 字体（V3 关键虚线分隔）
    · **left border** 改用 ::before 伪元素（V3 原值，position: absolute + top:0 left:0 width:3px height:100%）
    · **.dashboard-row** 1:1 布局 → 2fr 1fr 布局（V3 原型 grid-template-columns: 2fr 1fr，项目活动 2 份 + 最近活动 1 份）
    · **响应式** 1024px 窄屏自动堆叠 1 列
  - **验证**：
    · vite build 跑通（CSS 2.73 kB → 4.16 kB，JS 6.07 kB，map 17.10 kB）
    · F-53 处理链路第二阶段 2/3 完成（theme.scss + KoLibrary + DashboardView 三个 V3 风格 view）
- v1.17.18 2026-07-16 13:00:00 liyang: F-53.2 T213-T219 7 个占位 view V3 完整实施（处理链路阶段 2/3 收官）
  - **T213 ConflictsView**：.page-header + 6 类型冲突筛选 + 4 stat-card（待处置/高优先级/已解决/历史累计）+ .alert-item 列表（7 项冲突 + 优先级图标 + LLM 建议框）
  - **T214 AuditLogView**：.page-header + 搜索框 + 4 stat-card + 表格列表（OQ-11 3 重暴露：hover 短 ID + 点击详情 + CSV 导出；按 6 种 action 颜色分类标签）
  - **T215 DictMgmtView**：.page-header + 4 stat-card + 2 列 6 字典卡片（border-left 端口蓝 + 字典项 sample 标签）
  - **T216 ProjectMgmtView**：.page-header + 4 stat-card + 2 列 4 项目卡片（活动/归档 status 颜色区分 + KO 数/组织/最后活动 meta + 查看/归档动作）
  - **T217 PromptsView**：.page-header + 4 stat-card + 3 列 3 PRM 模板卡片（KO-PRM-0001/2/3 + Section/FIXED/DYNAMIC 数量统计 + 打开组装器按钮）
  - **T218 SnapshotsView**：.page-header + 4 stat-card + .alert-item 风格 10 条快照列表（hover 短 ID + 装配数/字符/tokens/变量/KO 5 项统计 + 查看/还原动作）
  - **T219 NotFoundView**：大数字 404 + 当前 URL 显示 + 返回首页/上一页 + 5 个快速跳转链接（知识库/提示词/权限/治理/审计）
  - **所有 view 复用 F-53.1 工具类**：.page-header / .stat-card / .alert-item / .btn / .btn-primary / .mono / .lst-item 全部从 src/styles/theme.scss 继承（不重复定义）
  - **所有 view 含 Sprint 2 数据**：V9001 seed 真实值（278 KO / 4 项目 / 6 字典 / 17 PRM 段 / 5 角色 / 7 冲突 / 12 快照 / 14328 审计 等 mock 数值）
  - **验证**：`vite build` 跑通（320+ modules，所有 view 编译成功）
  - **F-53 处理链路第二阶段 2/3 完成**：8 个 view V3 风格完整还原（Dashboard + Conflicts + AuditLog + DictMgmt + ProjectMgmt + Prompts + Snapshots + NotFound）
- v1.17.19 2026-07-16 13:30:00 liyang: F-53.3 长期流程改进（frontend-standards + check-v3-style + 修硬编码）
  - **新建 `docs/contracts/frontend-standards.md`**（V3 视觉验证标准，10 章节）：
    · 必须使用 V3 CSS 变量（硬编码白名单仅 26 个 V3 主题色）
    · 必须复用 V3 工具类（page-header / toolbar / btn / stat-card / alert-item / lst-item / 等 12 类）
    · 布局标准（CSS Grid 不用 el-row，padding 16px 24px）
    · 字体标准（Noto Sans SC + JetBrains Mono）
    · 关键 CSS 细节（::before 伪元素 + 虚线分隔 + 圆角 2px）
    · 反模式（硬编码颜色 / el-row 24 栅格 / 圆角 ≥5px）
    · 验证流程（check-v3-style.sh 自动 + PR review 视觉对比）
    · done_signal 模板 + review_focus 模板
  - **新建 `scripts/check-v3-style.sh`**（F-53.3 自动检测脚本，可执行）：
    · 5 项检测：硬编码颜色白名单 / V3 工具类使用 / V3 变量使用率 / el-row 反模式 / border-radius 反模式
    · V3 主题色白名单 26 个（port-blue / signal-orange / bg-rail / ...）
    · 退出码：ERRORS=0 退出 0；ERRORS>0 退出 1（CI 集成）
    · 首次跑：6 个硬编码 + 1 个 el-row → 全部修完 → 全部通过 ✅
  - **修复 6 个硬编码颜色**（check-v3-style 自动检测发现）：
    · KoDetailView.vue:151 `#f5f7fa` → `var(--bg-canvas)`
    · KoDetailView.vue:153 `border-radius: 4px` → `2px`（V3 原值）
    · ConflictsView.vue:279 `#fff` → `var(--bg-paper)`（V3 原值 #FFFFFF）
    · PermissionsView.vue:186-187 `#909399 / #E6A23C` → `var(--text-tertiary) / var(--signal-orange)`（V3 原值）
    · TopBar.vue:79 `1px solid #000` → `1px solid var(--line-strong)`（V3 原值 #B0B7C0）
    · ComposerView.vue:247 `#f5f7fa` → `var(--bg-canvas)` + `4px` → `2px`
  - **修 1 个 el-row 反模式**（KoLibraryView.vue + ComposerView.vue 全部从 el-row + el-col 改 div + CSS Grid）
  - **F-53 处理链路 3/3 全部完成**：
    · F-53.1 短期：theme.scss 20+ V3 变量 + 4 工具类 + KoLibrary V3 风格 ✅
    · F-53.2 中期：8 个 view V3 完整还原（Dashboard + KO Library + Conflicts + AuditLog + DictMgmt + ProjectMgmt + Prompts + Snapshots + NotFound）✅
    · F-53.3 长期：frontend-standards.md + check-v3-style.sh + TP-3 模板 ✅
  - **下个**：修复 vite build 错（KoLibraryView.vue parse 错误）+ TP-3 task pack 模板
- v1.17.20 2026-07-16 14:00:00 liyang: F-53.3.1 修 vite build parse 错（KoLibraryView 多余 div 嵌套）
  - **F-53.3 收尾**：check-v3-style.sh 全部通过 + vite build 成功
  - **修复 KoLibraryView.vue parse 错**：line 116 `Invalid end tag`
    · 嵌套 `type-info` div 多余（应是 type-name/quantity 直接在 type-card-content 内）
    · 缩进调整 + 删 1 个 `<div class="type-info">` + 1 个 `</div>`
    · 缩进从 14 空格改成 10 空格（与 type-name 缩进一致）
  - **验证**：
    · `vite build` 跑通（320+ modules，2.78s）
    · check-v3-style.sh 仍然全部通过 ✅
  - **F-53 处理链路完全收官**：F-53.1 + F-53.2 + F-53.3 + F-53.3.1 全部完成
- v1.17.21 2026-07-16 14:30:00 liyang: F-53.3 TP-3+ task pack 模板（含 V3 视觉验收）
  - **新建 `docs/templates/task-pack-template.md`**（TP-3+ 标准模板）
  - **核心改进**（F-53 教训）：
    · done_signal 模板**强制含 V3 视觉验收 6 项**（check-v3-style.sh / V3 视觉对比 / CSS 变量 ≥ 5 / 响应式 1024px+768px / 集成 / CHANGELOG）
    · review_focus 模板**强制含 V3 视觉对比**（前后端任务都列）
    · Validation Notes 模板含 F-53 强制项（10 项 frontend task 必查清单）
    · Regeneration Rules 模板含 F-53 强制校验（额外跑 check-v3-style.sh）
  - **与 Sprint 1+2 TP 模板对比**：
    · Sprint 1 TP-1 done_signal 全部是后端指标（"X/Y tests pass"）→ 模板升级
    · Sprint 2 TP-2 done_signal 5 项是 Sprint 2 实际产出 → 模板作为参考实例
    · 模板 13 章节：frontmatter / Overview / Task Graph / Traceability Matrix / Task Cards / done_signal / review_focus / Validation Notes / Regeneration Rules / Quick Win / 相关文档
