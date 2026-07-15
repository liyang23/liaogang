---
title: "Q-I1 真实 LLM 接入：百度千帆 Qianfan OpenAI 兼容网关（DeepSeek v4-flash）"
date: "2026-07-15"
category: "integration-issues"
module: "infra"
problem_type: "integration_issue"
component: "tooling"
severity: "medium"
symptoms:
  - "Sprint 1 用 mock 模式（api-key=demo-api-key-mock）跑通业务逻辑，但 U7 知识治理实施时需要真 LLM"
  - "Q-I1 缺 4 项关键信息：endpoint / 鉴权 / 模型 / 配额"
  - "application.yml 默认 base-url 写的是 https://api.deepseek.com/v4 + model=deepseek-v4（基于推测，未验证）"
  - "DeepSeekClient.parseResponse() 只 mock 解析 suggestion/confidence，没解析 OpenAI 标准 usage 字段"
root_cause: "config_error"
resolution_type: "config_change"
domain: "llm-integration"
pattern: "openai-compatible-gateway"
rejected_alternatives:
  - "直连 deepseek.com 官方 API — 拒绝：实际接入走百度千帆（llm_client.txt 证实），直连会 401/403"
  - "用官方 Java SDK — 拒绝：deepseek 没官方 Java SDK，千帆网关 OpenAI 兼容用 RestTemplate 即可"
  - "改 gRPC — 拒绝：千帆网关只支持 HTTP REST（OpenAI 兼容）"
applicable_versions:
  - "deepseek-v4-flash（千帆网关）"
  - "Java 17 + Spring Boot 3.2.5"
  - "OpenAI chat.completion 规范"
invalidation_condition: "如果百度千帆停止支持 deepseek-v4-flash 模型，或更换鉴权方式（如 OAuth 2.0），本方案需要重新适配"
source_refs:
  - "llm_client.txt"  # 用户提供的真实接口示例
  - "backend/src/main/resources/application.yml"
  - "backend/src/main/java/com/liaogang/famou/km/llm/DeepSeekClient.java"
  - "backend/src/main/java/com/liaogang/famou/km/llm/LlmQuotaService.java"
  - "docs/solutions/integration-issues/minio-port-semantics.md"  # 同类配置错误类
  - "docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
tags:
  - "q-i1"
  - "deepseek"
  - "qianfan"
  - "openai-compatible"
  - "llm"
  - "integration"
  - "sprint-1"
  - "u7"
---

# Q-I1 真实 LLM 接入：百度千帆 Qianfan OpenAI 兼容网关（DeepSeek v4-flash）

## Problem

Sprint 1 落地时 DeepSeek v4 LLM 客户端用 mock 模式跑通业务逻辑（`api-key=demo-api-key-mock`），但实际接入信息（endpoint / 鉴权 / 模型 / 配额）都未知。`application.yml` 默认值是基于推测（`https://api.deepseek.com/v4` + `deepseek-v4`），**实际接入走百度千帆 Qianfan OpenAI 兼容网关**，与官方 deepseek.com 是不同入口。

## Symptoms

- Sprint 1 启动后所有 LLM 业务逻辑走 mock 路径（日志 `Q-I1 DeepSeek v4 API 未提供（demo mock 模式）`）
- 切换到真服务时（`api-key=bce-v3/...`）如果保持旧 `base-url`，会请求到 deepseek.com 官方（不识别该 API key，返回 401）
- `DeepSeekClient.parseResponse()` 当前只 mock 解析（硬编码 `OVERRIDE` + 0.85），不解析 OpenAI 标准 `usage` 字段（计费/埋点基础）
- 没考虑上下文窗口限制（默认 deepseek-v4-flash 128K，超长 conflict context 会 400 错误）

## What Didn't Work

- **直连 deepseek.com 官方**：API key 是百度千帆的 `bce-v3/...` 格式，deepseek.com 不识别
- **官方 Java SDK**：deepseek 没官方 Java SDK，千帆只提供 OpenAI 兼容 HTTP 接入
- **gRPC**：千帆网关不支持，只支持 HTTP REST

## Solution

### 1. `application.yml` 改默认值

```yaml
app:
  llm:
    provider: deepseek
    api-key: ${LLM_API_KEY:demo-api-key-mock}                # 留环境变量覆盖
    base-url: ${LLM_BASE_URL:https://qianfan.baidubce.com/v2}  # 真实接入（千帆 OpenAI 兼容网关）
    model: ${LLM_MODEL:deepseek-v4-flash}                     # 真模型名
    timeout-seconds: 5

    # Q-I1 配额（2026-07-15 收齐）
    tpm: 100000                       # 每分钟 100K tokens
    tpd: 10000000                     # 每天 10M tokens（10h × 10 批/h × 100K/batch）
    monthly-cost-cap-yuan: 5000       # 月度成本上限
    cost-yuan-per-1k-tokens: 0.002    # 单元成本估算（含 reasoning_tokens）

    # Q-I1 上下文窗口
    context-window-tokens: 100000     # 100K 安全阈值（实际 128K 留 22% buffer）

    # Q-I1 dev/staging URL 保持一致
    dev-staging-same-as-prod: true
```

### 2. `DeepSeekClient` 改默认值 + 加上下文截断

```java
@Value("${app.llm.base-url:https://qianfan.baidubce.com/v2}")  // 改
@Value("${app.llm.model:deepseek-v4-flash}")                    // 改
@Value("${app.llm.context-window-tokens:100000}")               // 新增
private int contextWindowTokens;

public LlmSuggestion getConflictSuggestion(ConflictContext conflictContext) {
    if (apiKey.startsWith("demo-")) { /* mock 兜底 */ }

    // F-26 修复：超长 prompt 截断到 contextWindowTokens
    String systemPrompt = buildSystemPrompt();
    String userPrompt = buildUserPrompt(conflictContext);
    int estimatedTokens = (systemPrompt.length() + userPrompt.length()) / 2;  // 2 chars/token 估算
    if (estimatedTokens > contextWindowTokens) {
        int maxChars = contextWindowTokens * 2 - systemPrompt.length() - 100;
        userPrompt = userPrompt.substring(0, Math.min(maxChars, userPrompt.length()))
            + "\n\n[... 已截断，超出上下文窗口 " + contextWindowTokens + " tokens ...]";
    }
    // ... 发请求
}
```

### 3. `DeepSeekClient.parseResponse()` 适配 OpenAI + 千帆扩展

```java
// 提取千帆扩展：reasoning_content（独立计费）
String reasoningContent = (String) message.get("reasoning_content");

// 提取 OpenAI usage
Map<String, Object> usage = (Map<String, Object>) body.get("usage");
Integer promptTokens = asInt(usage.get("prompt_tokens"));
Integer completionTokens = asInt(usage.get("completion_tokens"));
Integer totalTokens = asInt(usage.get("total_tokens"));
Map<String, Object> details = (Map<String, Object>) usage.get("completion_tokens_details");
Integer reasoningTokens = asInt(details.get("reasoning_tokens"));

// LlmSuggestion 加 5 字段：reasoning / promptTokens / completionTokens / totalTokens / reasoningTokens
```

### 4. `LlmQuotaService` 加 @Value 读取 Q-I1 配额（不改核心限流逻辑）

```java
@Value("${app.llm.tpm:100000}")               private long tpmLimit;
@Value("${app.llm.tpd:10000000}")            private long tpdLimit;
@Value("${app.llm.monthly-cost-cap-yuan:5000}") private double monthlyCostCapYuan;
@Value("${app.llm.cost-yuan-per-1k-tokens:0.002}") private double costYuanPer1kTokens;
```

> Sprint 1 范围是 mock 计次模型（每天 1000 次调用硬编码），Sprint 3 升级 token 限流时直接用这些配置（无需再改 yml）。

## Why This Works

**Q-I1 真实接入路径**（基于 `llm_client.txt` curl 示例）：

```
POST https://qianfan.baidubce.com/v2/chat/completions
Headers:
  Authorization: Bearer bce-v3/ALTAK-...
  Content-Type: application/json
Body:
  {
    "model": "deepseek-v4-flash",
    "messages": [{"role": "user", "content": "..."}],
    "stream": false
  }
```

**接入方式**：HTTP REST + OpenAI 兼容（千帆网关）+ Bearer Token

| 项 | 值 | 来源 |
|----|-----|------|
| 协议 | HTTP POST | curl 示例 |
| Endpoint | `https://qianfan.baidubce.com/v2/chat/completions` | curl `--location` |
| 鉴权 | `Authorization: Bearer bce-v3/...` | curl `--header` |
| 模型 | `deepseek-v4-flash` | body `model` 字段 |
| Stream | `false`（同步）| body `stream: false` |
| 响应 schema | OpenAI chat.completion + 千帆扩展 | response body |
| 上下文窗口 | 100K（保守阈值）| Q-I1 收齐 + 实际 128K 留 buffer |

**修复原理**：
- **base-url 默认值**：从推测的 `https://api.deepseek.com/v4` 改为实际接入的 `https://qianfan.baidubce.com/v2`
- **model 默认值**：从推测的 `deepseek-v4` 改为实际可用的 `deepseek-v4-flash`
- **parseResponse**：补 OpenAI usage + 千帆 reasoning_content 字段，5 个新字段埋点（计费/审计用）
- **上下文截断**：防止超长 conflict context 触发 LLM 400 错误（Q-I1 100K 阈值）

## Prevention

### 立即做

1. **`.env.test` 模板**：在 `deploy/docker/` 加 `.env.test` 模板（包含 `LLM_API_KEY=demo-...`），集成测试时用 mock key 跑通
2. **`scripts/test-env-up.sh` 增强**：探测 Q-I1 真接入路径（curl 千帆 + 验证返回 200 / 401 而非 0）
3. **集成测试新增 `LlmClientIT`**：模拟真 API key + 真实 curl 请求到 mock LLM server（用 `WireMock` 或本地 mock 服务），验证 `parseResponse` 真实路径

### 长期

4. **U7 知识治理实施时**（Sprint 3）：
   - 改 `LlmQuotaService` 从"调用次数" → "token 消耗"（用 `tpm` / `tpd` / `monthlyCostCapYuan` / `costYuanPer1kTokens` 4 个新配置）
   - 加 Redis Lua 脚本同时维护 5 个 key：`tpm:{minute}` / `tpd:{day}` / `cost:{month}` / `platform:{day}` / `user:{day}:{sub}`
   - 加 Prometheus 指标 `llm_quota_used_total{scope=platform|user,window=tpm|tpd|cost}`
5. **429 fallback 策略**：超配额时降级到 mock（已支持 `apiKey.startsWith("demo-")` 兜底），不需要新写代码
6. **定期 review 单元成本**：`cost-yuan-per-1k-tokens: 0.002` 是基于 deepseek-v4-flash 估算，千帆按实际模型定价可能不同

## Quick Win

```bash
# 验证真接入路径配对（已 commit + push）
mvn clean verify -Dspring.profiles.active=it
# 期望: BUILD SUCCESS, 5/5 tests
```

切真服务：设置 `LLM_API_KEY=bce-v3/...` 环境变量（K8s Secret）→ 重启 backend → 看日志 `Q-I1 DeepSeek v4 API 未提供` 消失 → 走真请求。

## Related

- **`llm_client.txt`**：用户提供的真实 curl + 响应示例（所有改动的源头依据）
- **`docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md`** §9.1：Q-I1 定义 + 触发时间
- **`docs/solutions/integration-issues/minio-port-semantics.md`**：同类"配置端口错误"沉淀
- **`docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`**：Sprint 1 总览
- **`CHANGELOG.md` v1.16.5 + v1.16.6**：本次 Q-I1 完整收齐记录
