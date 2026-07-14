# 辽港伐谋知识管理平台

> 港口运筹优化算法知识管理平台 — Monorepo 起点

## 项目简介

辽港伐谋（Famou）项目致力于港口运筹优化算法的研发与应用。本平台承载：
- **知识对象库（KO Library）**：6 种类型（CON 约束 / RUL 规则 / PAR 参数 / SCH 数据结构 / PRM 提示词模板 / DOC 文档）
- **提示词组装器（Composer）**：基于 PRM 模板 + Section 自定义 + Handlebars 模板语法
- **知识治理（Governance）**：6 种 C 类冲突 + 1 种 H 类健康检测，LLM 主动建议
- **审计日志 + 5 预置角色 + 可调整权限矩阵**

## 目录结构

```
.
├── frontend/                  # Vue 3 + Vite + Element Plus
│   ├── src/
│   │   ├── api/              # Axios + API 封装
│   │   ├── components/       # 通用组件（KO 表格、状态机、Tab 容器）
│   │   ├── directives/       # 自定义指令（权限指令）
│   │   ├── router/           # Vue Router 4
│   │   ├── stores/           # Pinia stores
│   │   ├── utils/            # 工具函数（Handlebars 子集、Markdown 渲染）
│   │   ├── views/            # 页面组件
│   │   ├── App.vue
│   │   └── main.ts
│   ├── package.json
│   └── vite.config.ts
├── backend/                   # Spring Boot 3 + MyBatis-Plus + MySQL 8
│   ├── src/main/java/com/liaogang/famou/km/
│   │   ├── auth/             # 辽港统一认证集成（OQ-23 取代 OIDC）
│   │   ├── ko/               # KO 库（实体 / 服务 / 状态机）
│   │   ├── prompt/           # 提示词系统（PRM / Handlebars）
│   │   ├── governance/      # 知识治理（冲突检测 / LLM 建议）
│   │   ├── audit/            # 审计日志
│   │   ├── project/          # 项目管理
│   │   ├── role/             # 角色 + 权限
│   │   ├── dict/             # 字典管理
│   │   ├── document/         # 文档预览
│   │   └── llm/              # DeepSeek v4 客户端
│   ├── src/main/resources/
│   │   ├── db/migration/     # Flyway V9001~V9011
│   │   └── seed/             # 5 预置角色权限矩阵 YAML
│   └── pom.xml
├── deploy/                    # K8s + Helm
│   └── helm/
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
│           ├── frontend-deployment.yaml
│           ├── backend-deployment.yaml
│           └── ingress.yaml
├── docs/
│   ├── brainstorms/          # 精炼后的 PRD（v0.32）
│   │   └── liaogang-famou-km-platform-requirements.md
│   ├── plans/                # 技术规划
│   │   └── 2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md
│   └── tasks/                # 任务包
│       └── 2026-07-14-001-task-pack-sprint-1-foundation-tasks.md
├── scripts/
│   ├── setup.sh              # 本地开发环境初始化
│   ├── seed.sh               # Flyway V9001 seed 加载
│   └── test.sh               # 全栈测试
├── .gitignore
├── CHANGELOG.md
└── README.md
```

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 3.4+ / 5+ / 2.4+ / 2+ / 5+ |
| 后端 | Spring Boot + MyBatis-Plus + MySQL + Flyway | 3.2+ / 3.5+ / 8+ / 9+ |
| 数据库 | MySQL 8 + Redis 7 + MinIO (对象存储) | 8+ / 7+ / latest |
| 部署 | Kubernetes + Helm | 1.27+ / 3+ |
| 鉴权 | 辽港统一认证（OQ-23 取代 OIDC） | — |
| LLM | DeepSeek v4 | — |

## 快速开始

### 1. 克隆与初始化

```bash
git clone <repo-url> liaogang-famou-km
cd liaogang-famou-km
./scripts/setup.sh   # 验证 Node.js 18+ / Java 17+ / Docker
```

### 2. 启动依赖服务

```bash
# MySQL 8 + Redis 7 + MinIO
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8
docker run -d --name redis -p 6379:6379 redis:7
docker run -d --name minio -e MINIO_ROOT_USER=minio -e MINIO_ROOT_PASSWORD=minio123 -p 9000:9000 -p 9001:9001 minio/minio server /data
```

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run
# 默认端口 8080，OpenAPI 文档： http://localhost:8080/v3/api-docs
```

### 4. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
# 默认端口 5173
```

### 5. 访问

- 前端：http://localhost:5173
- 后端 API：http://localhost:8080/api
- MinIO Console：http://localhost:9001

## 文档引用

| 文档 | 说明 |
|------|------|
| [`docs/brainstorms/liaogang-famou-km-platform-requirements.md`](docs/brainstorms/liaogang-famou-km-platform-requirements.md) | 精炼后的 PRD（v0.32 + 22 OQ 决议）|
| [`docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`](docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md) | MVP 技术规划（10 Implementation Unit × 4 Sprint）|
| [`docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md`](docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md) | Sprint 1 基础架构 Task Pack（9 任务 × 4 wave）|
| [`辽港统一认证集成方案.md`](../辽港统一认证集成方案.md) | 鉴权集成（OQ-23 取代原 OIDC）|
| [`辽港伐谋知识管理平台_原型_V3.html`](../辽港伐谋知识管理平台_原型_V3.html) | UI 原型（V3.0 基准）|
| [`DESIGN.md`](../DESIGN.md) | 设计 token（颜色 / 字体 / 间距）|

## Sprint 0 启动前 owner 输入

| 输入 ID | 内容 | 负责人 | 截止 |
|--------|------|--------|------|
| Q-I1 | DeepSeek v4 接入细节（HTTP/gRPC/SDK + 配额）| 算法团队 | 2026-07-20 |
| Q-I2 | 辽港慧应用 APIKEY + 招商云 PAAS 订阅地址（OQ-23 取代 OIDC 端点）| IT/安全 + 慧应用项目组 | Sprint 0 启动前 |
| Q-I3 | MinIO 部署位置 | 基础架构 | Sprint 0 启动前 |
| Q-I5 | K8s Ingress Controller 选型 | 基础架构 | Sprint 0 启动前 |

未收齐时，U2/U3/U10 保留 mock 客户端（DeepSeekClient mock + 辽港慧应用 mock + MinIO 配置占位）。

## License

内部使用 / 暂未开源
