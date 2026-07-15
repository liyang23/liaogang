---
title: "MinIO 端口语义：9000 = S3 API，9001 = Console Web UI（test_env.txt 写反）"
date: "2026-07-15"
category: "integration-issues"
module: "infra"
problem_type: "integration_issue"
component: "tooling"
severity: "medium"
symptoms:
  - "io.minio.errors.InvalidResponseException: Non-XML response from server. Response code: 400, Content-Type: text/xml; charset=utf-8, body: <?xml version=\"1.0\" encoding=\"UTF-8\"?><Error><Code>InvalidArgument</Code><Message>S3 API Requests must be made to API port.</Message>"
  - "MinioBucketInitializer 启动时 bucketExists 调用失败，IllegalStateException 抛出，Spring context 启动失败"
  - "curl http://127.0.0.1:9001/ 返回 HTML 页面（Console UI），不是 XML API 响应"
  - "curl http://127.0.0.1:9000/ 返回 403 + XML 错误（API 端口但未签名被拒）"
root_cause: "config_error"
resolution_type: "config_change"
domain: "integration-environment"
pattern: "service-port-semantics"
rejected_alternatives:
  - "改 MinIO 启动参数让 API 跑在 9001 — 拒绝：违反 MinIO 标准端口约定，运维侧混用 API/Console 端口会增加认知负担"
  - "加端口嗅探代码自动选可用端口 — 拒绝：增加生产代码复杂度且不可靠，应在配置侧约束"
  - "让 application-it.yml 兼容两个端口 — 拒绝：API 和 Console 协议不同（XML API vs HTML UI），无法用同一 SDK 访问"
applicable_versions:
  - "MinIO 8.x"
  - "MinIO 7.x"
invalidation_condition: "如果 MinIO 在未来版本改变默认端口约定（API 不再是 9000 或 Console 不再是 9001），本方案需要重新适配"
source_refs:
  - "backend/src/test/resources/application-it.yml"
  - "test_env.txt"
  - "deploy/docker/docker-compose.yml"
  - "backend/src/test/java/com/liaogang/famou/km/test/MinioBucketInitializer.java"
  - "docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md"
tags:
  - "minio"
  - "port-semantics"
  - "s3-api"
  - "console"
  - "integration-test"
  - "configuration"
  - "sprint-1"
---

# MinIO 端口语义：9000 = S3 API，9001 = Console Web UI

## Problem

集成测试时 `test_env.txt` 把 MinIO 端口写为 9001，导致 `application-it.yml` 把 MinIO Java SDK 的 endpoint 配到 9001（实际是 Console Web UI 端口），启动时 `bucketExists` 调用被 MinIO 拒绝（`S3 API Requests must be made to API port`），导致 Spring context 启动失败，所有 IT 用例无法运行。

## Symptoms

- `io.minio.errors.InvalidResponseException: Non-XML response from server. Response code: 400, Content-Type: text/xml; charset=utf-8, body: <?xml version="1.0" encoding="UTF-8"?><Error><Code>InvalidArgument</Code><Message>S3 API Requests must be made to API port.</Message>` —— MinIO SDK 发 S3 API 请求到 Console 端口，MinIO 拒绝
- `MinioBucketInitializer` 的 `ApplicationRunner` 在 Spring context 启动时调用 `bucketExists`，失败抛出 `IllegalStateException("MinIO bucket 初始化失败")`，导致所有集成测试（`@SpringBootTest` 加载 context）全部失败
- `curl http://127.0.0.1:9001/` 返回 HTML 页面（MinIO Console SPA），`Content-Type: text/html`，不是 S3 API 期望的 XML
- `curl http://127.0.0.1:9000/` 返回 `HTTP 403` + `Server: MinIO` + XML 错误"authorization mechanism not supported"（**这才是 S3 API 端口**，被拒是因为没用 AWS4 签名，不是端口错）

## What Didn't Work

- **用 9001 直接配给 SDK**：MinIO 看到 S3 API 请求，发到 Console handler，Console handler 返回 400（不识别 API path）
- **改成 http://127.0.0.1:9001/ 后再加 path prefix**：MinIO 路径是统一的（无论 API 还是 Console 都从 `/` 开始），path prefix 不能区分协议
- **依赖 MinIO 自动 redirect 到 API**：MinIO 不会自动从 Console 跳到 API

## Solution

把 `application-it.yml` 的 MinIO endpoint 从 `http://127.0.0.1:9001` 改成 `http://127.0.0.1:9000`（S3 API 端口）。

```yaml
# 改前（错）
app:
  minio:
    endpoint: http://127.0.0.1:9001  # 这是 Console 端口！

# 改后（对）
app:
  minio:
    endpoint: http://127.0.0.1:9000  # S3 API 端口
    access-key: minioadmin
    secret-key: minioadmin@2026
    bucket-name: km-platform-it
```

Console 端口（9001）仅用于浏览器访问 `http://127.0.0.1:9002`（宿主机）或 `http://127.0.0.1:9001`（容器内）查看 bucket / object 列表。

## Why This Works

**根因**：`test_env.txt` 提供配置时把 MinIO 的 9001 误标为 API 端口，实际上：

| 端口 | 协议 | 用途 | 谁用 |
|------|------|------|------|
| 9000 | S3 API（XML over HTTP）| SDK / curl / AWS CLI | MinIO Java SDK / Python boto3 / aws s3 cp |
| 9001 | Web Console（HTML SPA）| 浏览器 | 运维 / 开发者人工查看 |

MinIO 镜像的 `--address` 参数默认 `:9000`（API 端口），`--console-address` 默认 `:9001`（Console 端口）。**API 永远是 9000，Console 永远是 9001**，不会变。

**修复原理**：让 SDK 连到正确的 API 端口（9000），所有 S3 操作（`bucketExists` / `makeBucket` / `putObject`）才能正常发到 MinIO 的 S3 handler。

## Prevention

### 立即做

1. **配置健康检查脚本**：写 `scripts/test-env-check.sh` 测 MinIO **必须分别**测 9000（API，期望 403 + XML）和 9001（Console，期望 200 + HTML），两者响应都"对"才算健康
   ```bash
   # 探测 API 端口：期望 403 + Server: MinIO + Content-Type: text/xml
   curl -sI http://127.0.0.1:9000/ | head -3
   # 探测 Console 端口：期望 200 + Server: MinIO Console + Content-Type: text/html
   curl -sI http://127.0.0.1:9001/ | head -3
   ```

2. **`test-env-up.sh` 探测脚本增强**：当前 `probe_minio` 只测 `/minio/health/live`（哪个端口都支持），建议同时探测两个端口并打印标签（API vs Console），让配置错误一眼可见

3. **`test_env.txt` 文档加注释**：明确"9000 = S3 API / 9001 = Console，SDK 连 9000，浏览器访问 9001"

### 长期

4. **运维交付模板**：MinIO / 对象存储类服务交付时，文档必须分两段写：
   - **API 连接信息**（endpoint / access-key / secret-key / bucket）
   - **管理控制台信息**（console URL / admin 账号）
   不要混在一段

5. **CI 容器端口扫描**：CI 跑集成测试前，扫描 service container 暴露的端口，**断言 9000 是 S3 API 协议**（通过发送未签名 GET 请求并验证返回 403 + XML Content-Type 实现）

## Related

- **`docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`**：Sprint 1 教训总览，本文是该教训的一个子案例
- **`test_env.txt`**：用户提供的测试环境配置（端口写反是源头）
- **`deploy/docker/docker-compose.yml`**：docker compose 端口映射 `9000:9000`（API）+ `9001:9001`（Console）—— 端口映射正确
- **`backend/src/test/resources/application-it.yml`**：修后版本
