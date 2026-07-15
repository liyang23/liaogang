# docs/private/

**本地私有文件目录，不进 git**（见仓库根 `.gitignore`）。

## 用途

存放**含敏感信息**的本地参考文件，例如：

- `llm_client.txt`：LLM 接口真实信息（curl + 响应示例 + API key）
- 其他含真实凭证的配置文件（生产/预发环境 key、个人测试 token 等）

## 约定

- 本目录及所有子目录、文件**均不提交到 git**
- 真实 API key / 密码 / 凭证**禁止提交**（即使放在本目录）
- 团队成员各自维护自己的私有文件
- 文档/Solution 引用本目录文件时，只引用**文件名**和**用途**，不引用内容

## 添加新文件

```bash
# 1. 把文件放到本目录（如 docs/private/team-credentials.txt）
# 2. 文件自动被 .gitignore 排除（docs/private/ 通配）
# 3. 引用时写相对路径或文件名（不贴内容）
```

## Sprint 1 相关文件

| 文件 | 内容 | 来源 |
|------|------|------|
| `llm_client.txt` | DeepSeek v4-flash 真实 curl + 响应示例（Q-I1 接入依据）| 算法团队 2026-07-15 |

## 关联

- `docs/solutions/integration-issues/q-i1-real-llm-integration.md`：基于 `llm_client.txt` 沉淀的真接入配置（已 commit）
- `docs/solutions/integration-issues/minio-port-semantics.md`：基于 `test_env.txt` 端口错误沉淀
- `.gitignore` `docs/private/` 段：自动 ignore 本目录
