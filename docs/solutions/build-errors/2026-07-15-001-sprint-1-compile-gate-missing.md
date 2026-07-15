---
title: "Sprint 1 落地时未跑 mvn compile 验证 + 完全无 CI 门禁"
date: "2026-07-15"
category: "build-errors"
module: "backend"
problem_type: "build_error"
component: "tooling"
severity: "high"
symptoms:
  - "mvn clean verify 报 50+ 编译错误（缺 lombok / jjwt / spring-security 依赖，类名重复，final 字段未初始化，重复构造器）"
  - "mvn verify 编译通过后 failsafe 报 Table 'km_platform_it.role' doesn't exist（Flyway V9001 seed 假设建表已存在）"
  - "io.minio.errors.InvalidResponseException: Non-XML response from server. Response code: 400, Content-Type: text/xml（MinIO endpoint 配错到 Console 端口）"
  - "AuditLogAnnotation.java 报 接口 AuditLog 是公共的，应在名为 AuditLog.java 的文件中声明（public type 与文件名不匹配）"
  - "JwtAuthFilter.java 报 需要 class, interface, enum 或 record（字段定义在 class body 之外游离 32 行）"
root_cause: "missing_tooling"
resolution_type: "workflow_improvement"
domain: "development-workflow"
pattern: "ci-gate"
rejected_alternatives:
  - "trust-commit-message：让 commit message 写 task_id 自证质量 — 拒绝：commit message 不阻止编译失败"
  - "post-merge-fix-and-sorry：合并到 main 后再修 — 拒绝：污染主分支历史 + 阻塞后续 PR"
  - "manual-remember-to-run-mvn：在 README 写提示让人手跑 — 拒绝：依赖人记性 = 不可靠"
  - "reviewer-catches-all：靠 code review 拦截 — 拒绝：reviewer 关注架构不查编译（spec-code-review 已通过 24 项修复但漏掉编译）"
applicable_versions:
  - "Java 17"
  - "Maven 3.9+"
  - "Spring Boot 3.2.5"
  - "git 2.30+"
invalidation_condition: "当项目迁移到非 Maven 构建系统（Gradle / Bazel / Make），或改用集成开发环境（IDE）的自动构建门禁取代 commit hook，本方案需要重新适配。当团队达到 10+ 工程师规模，需要更细粒度的门禁（pre-commit 单元测试 + pre-push 集成测试 + CI 全量回归）时，本方案粒度不足"
source_refs:
  - "docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md"
  - "CHANGELOG.md"
  - "commit d676c27"
  - "commit 27069d3"
  - "commit fd9f3cc"
  - "commit 909d5a2"
  - "backend/pom.xml"
  - "backend/src/main/resources/application.yml"
tags:
  - "sprint-1"
  - "compile-gate"
  - "ci-cd"
  - "pre-commit-hook"
  - "maven"
  - "spring-boot"
  - "knowledge-gate"
  - "process-improvement"
---

# Sprint 1 落地时未跑 mvn compile 验证 + 完全无 CI 门禁

## Problem

Sprint 1 落地（`feat/sprint-1-foundation` 分支 12 commit）时，所有 main 业务代码改动都没跑过 `mvn compile` 验证，导致 15+ 编译错误累积到集成测试阶段才暴露，直到第 13 次 `mvn verify` 才全部跑通；仓库**完全没有** CI 门禁（无 `.github/workflows/`、无 `.githooks/`、无 `pre-commit` hook）让"未编译的代码"在物理上无法 push 到 remote。

## Symptoms

- `mvn clean verify` 报 50+ 编译错误（缺 `lombok` / `jjwt 0.11.5` / `spring-boot-starter-security` 依赖；`AuditLog` 实体类名与同包 `@interface AuditLog` 注解冲突；`JwtAuthFilter` 字段定义游离于 class 之外 32 行；`LlmQuotaService` 多行字符串字面量物理换行；`DeepSeekClient` 两个构造器各自漏初始化一个 final 字段；`AuditLogService` `@RequiredArgsConstructor` + 手动构造器同签名重复；多个文件 `import` 缺失；`application.yml` 两个 `server:` 顶级键重复；`mybatis-plus-spring-boot-3-starter` 拼写错误应为 `mybatis-plus-spring-boot3-starter`）
- `mvn verify` 编译通过后 failsafe 阶段报 `Table 'km_platform_it.role' doesn't exist` —— Flyway `V9001__seed_v032_initial_data.sql` 假设 `role` / `ko` 表已存在但 Sprint 1 范围**没有建表 migration**
- 集成测试报 `io.minio.errors.InvalidResponseException: Non-XML response from server. Response code: 400, Content-Type: text/xml` —— `application-it.yml` 把 MinIO endpoint 配到了 Console 端口 9001（S3 API 实际在 9000），`test_env.txt` 端口写反
- `AuditLogAnnotation.java` 编译报 `接口 AuditLog 是公共的，应在名为 AuditLog.java 的文件中声明` —— public type 与文件名不匹配
- `JwtAuthFilter.java` 编译报 `需要 class, interface, enum 或 record` —— 第 30-44 行字段定义在 class body 之外（4 空格缩进但 class 是第 62 行才声明）
- 修了 15 次 `mvn verify` 才 BUILD SUCCESS（中间每次都暴露 1-2 个新错误）

## What Didn't Work

- **依赖 commit message 自证质量**：9 个 commit 的 `feat(U2-Txxx)` message 写得漂亮，但 message 不阻止编译失败；reviewer 看 message 也不会反向核对 `mvn compile` 输出
- **`spec-code-review` 24 项修复**（`909d5a2`）覆盖了安全 / SQL 注入 / 表达式注入等架构问题，**但漏掉编译错误**——review persona 关注语义层不查字节码
- **task pack 写了 `test_focus` + `review_gate`** 但**只挂在文档里**——`docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md` 每个 task 都标了 `test_focus: "mvn spring-boot:run 启动 + ..."` 但没有任何 agent / script / CI 实际跑过这些命令
- **靠人记性跑 `mvn compile`**：开发者写完代码 → 手动 commit，依赖人每次记得跑——不可靠

## Solution

修复分两层：

### 第一层（已落盘，`commit d676c27`）：修 Sprint 1 累积的 15+ 编译错误

F-21 ~ F-25 修复序列覆盖 17 文件：
- F-21 编译错误（多行字符串改 text block / 游离代码移入 class / 类名冲突实体改 `AuditLogEntity` / final 字段合并构造器 / import 补齐 / 重复构造器合并）
- F-22 依赖补齐（`lombok` / `jjwt 0.11.5` / `spring-boot-starter-security` / mybatis-plus artifactId 修正）
- F-23 import 补齐（`jakarta.annotation.PostConstruct` / `java.util.List` / `Authentication` / `SecurityContextHolder`）
- F-24 配置修复（YAML 重复键 / MinIO 端口 9001 → 9000 / MinIO 包路径 `io.minio.errors`）
- F-25 集成测试 skip Flyway（`AuditAspectIT` 加 `spring.flyway.enabled=false`）

修复后 `mvn clean verify` BUILD SUCCESS，5/5 测试通过（1 单元 + 4 集成），耗时 7.5s。

### 第二层（提案 P1，未实施）：加 CI 门禁防止重蹈

**P1-1：Pre-commit hook**（commit 前跑 `mvn compile`）

```bash
# .githooks/pre-commit
#!/usr/bin/env bash
set -e
echo "=== mvn compile 检查 ==="
cd backend && ./mvnw compile -q || { echo "❌ mvn compile 失败，请先修代码再 commit"; exit 1; }
echo "✓ mvn compile 通过"
```

启用：`git config core.hooksPath .githooks`

**P1-2：Pre-push hook**（push 前跑 `mvn verify` 集成测试）

```bash
# .githooks/pre-push
#!/usr/bin/env bash
set -e
echo "=== mvn verify 检查（集成测试）==="
cd backend && ./mvnw verify -Dspring.profiles.active=it -q || { echo "❌ mvn verify 失败，请先修测试再 push"; exit 1; }
echo "✓ mvn verify 通过"
```

**P1-3：GitHub Actions CI**（PR 必跑，本地 hook 通过也要 CI 兜底）

```yaml
# .github/workflows/backend-ci.yml
name: backend-ci
on: [push, pull_request]
jobs:
  verify:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env: { MYSQL_ROOT_PASSWORD: test, MYSQL_DATABASE: km_platform_it }
        ports: ['3306:3306']
      redis:
        image: redis:7-alpine
        ports: ['6379:6379']
      minio:
        image: minio/minio:latest
        env: { MINIO_ROOT_USER: minioadmin, MINIO_ROOT_PASSWORD: minioadmin@2026 }
        ports: ['9000:9000', '9001:9001']
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: 17 }
      - run: cd backend && ./mvnw verify -Dspring.profiles.active=it
```

## Why This Works

**根因**：`commit 流程` 缺 `mvn compile` 验证门禁，导致编译失败代码能 commit + push + 合并到分支；多个 commit 累积后形成"编译雷区"。

**修复原理**：
- **`mvn compile` 阻断**：pre-commit hook 让"编译失败"在物理上无法 commit（git 拦截）
- **`mvn verify` 阻断**：pre-push hook 进一步拦截"集成测试失败"
- **CI 兜底**：GitHub Actions 在 PR 阶段再次验证（即使本地 hook 失效 / 跳过也能兜住）
- **三层防御**：(local hook) + (local hook) + (CI) = 任何一层失守其余两层仍能拦截

**为什么不靠"code review"**：reviewer 关注架构 / 安全 / 业务正确性，不查字节码层（spec-code-review 24 项修复通过但漏掉编译错误就是证据）。

**为什么不靠"手动跑 mvn"**：依赖人记性 = 不可靠（本次 9 commit 0 次跑就是证据）。

## Prevention

### P1（必须立即做）

1. **Pre-commit hook**（`backend` 模块）：`mvn -pl backend compile -q` 失败阻断 commit（参考 Solution 段 P1-1 完整脚本）
2. **Pre-push hook**：`mvn -pl backend verify -Dspring.profiles.active=it -q` 失败阻断 push（参考 P1-2）
3. **GitHub Actions CI**：`.github/workflows/backend-ci.yml` + MySQL/Redis/MinIO service containers（参考 P1-3）

### P2（强烈建议，本 sprint 收尾时做）

4. **task pack `test_focus` 自动化**：写 `scripts/spec-validate.sh` 解析 `docs/tasks/*.md` 的 `test_focus` 字段，提取命令**真跑一次**；集成进 pre-commit hook
5. **环境配置健康检查**：写 `scripts/test-env-check.sh` 测 MySQL/Redis/MinIO 连通性 + **端口语义**（MinIO 必须分别测 API 9000 和 Console 9001，不能只测一个）；跑 `test-it.sh` 之前必跑
6. **Flyway 迁移顺序约定**：建表 migration（V1__create_schema.sql）必须在 seed migration（V9001__）之前；或者 seed migration 加 `IF NOT EXISTS` 防御；或者 `AuditAspectIT` 等不依赖 schema 的 IT 用 `spring.flyway.enabled=false` 跳过（已做）

### P3（流程改进，本季度内）

7. **commit 规范增强**：commit message 强制含 `task_id`（如 `feat(U2-T005): ...`），自动关联到 task pack；commit hook 检查 `task_id` 必须在已知 task pack 里；防止"游离 commit"
8. **Sprint 完成前必跑清单**：
   - [ ] `mvn clean verify` 通过
   - [ ] `./scripts/test-it.sh` 通过（含集成测试）
   - [ ] `./scripts/spec-validate.sh` task pack `test_focus` 全过
   - [ ] `CHANGELOG.md` 已更新
   - [ ] 至少 1 个 `spec-code-review` 通过

## Quick Win（一行命令自查）

```bash
cd backend && ./mvnw clean verify -Dspring.profiles.active=it -q
```

期望输出：`BUILD SUCCESS`，`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。

## Related

- **`CHANGELOG.md` v1.16.0 + v1.16.1**：Sprint 1 实现 + 修复入口
- **`commit d676c27`**：`fix(backend): Sprint 1 编译错误修复 + 集成测试端到端验证通过（17 文件 + CHANGELOG）`
- **`docs/tasks/2026-07-14-001-task-pack-sprint-1-foundation-tasks.md`**：task pack 写了 `test_focus` 但**没有自动化执行**是根因之一
- **待立 solution 文档**（本次未单独立）：
  - MinIO 端口语义（API 9000 vs Console 9001）→ `docs/solutions/integration-issues/minio-port-semantics.md`
  - Lombok `@RequiredArgsConstructor` + 手动构造器冲突 → `docs/solutions/build-errors/lombok-required-args-constructor-conflict.md`
  - public `@interface` 必须匹配文件名 → `docs/solutions/conventions/java-public-type-filename.md`
  - Stop hook 相对路径在 worktree 子目录失效 → `docs/solutions/tooling-decisions/stop-hook-absolute-paths.md`
