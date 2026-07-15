---
title: "Claude Code Stop hook 相对路径在 worktree 子目录解析失败（应改绝对路径）"
date: "2026-07-15"
category: "tooling-decisions"
module: "tooling"
problem_type: "tooling_decision"
component: "tooling"
severity: "low"
symptoms:
  - "Stop hook error: Failed with non-blocking status code: node:internal/modules/cjs/loader:1459 - throw err - Error: Cannot find module '/Users/.../worktree/backend/.claude/hooks/prd-readiness-guard'"
  - "code: 'MODULE_NOT_FOUND' — node 找不到 hook 脚本"
  - "worktree 根目录跑命令时 hook 正常工作；cd 到 backend/ 子目录后 hook 报 MODULE_NOT_FOUND"
  - "修复后 Stop hook 不再报错（无论 cwd 在 worktree 根还是子目录）"
root_cause: "incomplete_setup"
resolution_type: "config_change"
domain: "claude-code-tooling"
pattern: "absolute-paths-for-process-cwd-resolution"
rejected_alternatives:
  - "在每个 worktree 子目录建 .claude/hooks symlink 指回主目录 — 拒绝：治标不治本，开发者可能 cd 到任意子目录"
  - "改 hook 配置用 process.cwd() 解析 — 拒绝：Claude Code hook 文档明确 hooks 接收 args 相对路径，cwd 由调用者决定"
  - "在 worktree 根目录的 settings.json 单独配置 — 拒绝：worktree 内的 settings.json 是从主目录复制的，git 跟踪会冲突"
applicable_versions:
  - "Claude Code 1.x / 2.x"
  - "spec-first 0.2.x"
invalidation_condition: "如果 Claude Code 改变 hook 路径解析逻辑（从 process.cwd() 改为基于 hook 配置文件所在目录），本方案需要重新适配"
source_refs:
  - ".claude/settings.json"
  - ".worktrees/sprint-1/.claude/settings.json"
  - "scripts/test-env-up.sh"
  - "docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md"
tags:
  - "claude-code"
  - "stop-hook"
  - "worktree"
  - "absolute-path"
  - "node"
  - "sprint-1"
---

# Claude Code Stop hook 相对路径在 worktree 子目录解析失败（应改绝对路径）

## Decision

把 `.claude/settings.json` 里 4 个 hook（SessionStart / UserPromptExpansion / PreToolUse / Stop）的 `args` 数组从相对路径 `.claude/hooks/X` 改成**绝对路径** `/Users/<user>/<repo>/.claude/hooks/X`。

## Context

Claude Code 在主目录跑命令时 Stop hook 正常（`node .claude/hooks/prd-readiness-guard` 解析为 `/Users/.../liaogang/.claude/hooks/prd-readiness-guard`）。但用 git worktree 在 `feat/sprint-1-foundation` 分支工作时：

- 开发者频繁 `cd backend/` 跑 `mvn verify` / `./mvnw spring-boot:run` / `./scripts/test-it.sh`
- Claude Code 触发 Stop hook 时 `process.cwd()` 是 `backend/`，相对路径解析为 `backend/.claude/hooks/prd-readiness-guard`（不存在）
- node 报 `MODULE_NOT_FOUND`，Stop hook 失败（non-blocking，不影响 Claude Code 工作，但每次 turn 结束都报一次错误，污染日志）

## Guidance

### 改前（错）

```json
{
  "hooks": {
    "Stop": [
      {
        "matcher": ".*",
        "hooks": [
          {
            "type": "command",
            "command": "node",
            "args": [".claude/hooks/prd-readiness-guard"]  // 相对路径！
          }
        ]
      }
    ]
  }
}
```

### 改后（对）

```json
{
  "hooks": {
    "Stop": [
      {
        "matcher": ".*",
        "hooks": [
          {
            "type": "command",
            "command": "node",
            "args": ["/Users/liyang129/data/liaogang/.claude/hooks/prd-readiness-guard"]  // 绝对路径
          }
        ]
      }
    ]
  }
}
```

需要改的 4 个 hook：
- SessionStart → `session-start`
- UserPromptExpansion → `spec-plan-guard`
- PreToolUse → `prd-prewrite-guard`
- Stop → `prd-readiness-guard`

### 适用条件

- **使用 git worktree**（worktree 子目录与主目录 `.claude/` 不在同一相对路径）
- **使用 git hooks / pre-commit**（worktree 内 .githooks 通过 `core.hooksPath` 配置绝对路径）
- **开发者经常在子目录跑命令**（mvn / pnpm / 集成测试）

### 不适用

- 只在主仓根目录跑命令（相对路径在根目录能解析）
- 不使用 git worktree
- hook 脚本是 `cd` 到固定目录再执行的（cwd 在 hook 内部已固定）

## Why This Matters

**症状（不致命但污染）**：
- 每次 turn 结束都报 `MODULE_NOT_FOUND` 错误
- 开发者注意力被无效错误消耗
- 真实错误可能淹没在 hook 错误中

**为什么不简单在 worktree 内建 symlink**：
- worktree 内 `.claude/hooks` 建 symlink 指回主目录 → 只解决 worktree 根目录跑命令的问题，**子目录跑还是失败**（因为 cwd 是子目录）
- 治标不治本

**为什么不改 hook 配置用 process.cwd() 解析**：
- Claude Code 的 hook 文档明确 hooks 接收 args 相对路径，cwd 由调用者决定
- 改 hook 脚本去 `chdir` 到固定目录是 hack，且破坏 hook 通用性

**为什么绝对路径是根治**：
- 绝对路径与 process.cwd() 无关，**无论 cwd 在哪都正确解析**
- 一处修改，所有环境都生效（主目录 + worktree + 子目录）
- 代价：机器耦合（换电脑要改路径），但对单人项目可接受

## When to Apply

- 创建新 git worktree 时
- 团队成员第一次 clone 项目后
- Stop hook 报 `MODULE_NOT_FOUND` 时
- 任何 Claude Code hook 在 worktree 报错时

## Examples

### 示例 1：主目录正常 + worktree 子目录报错

```bash
# 主目录跑（cwd = /Users/x/Repo）
$ git status
# Stop hook 正常：node /Users/x/Repo/.claude/hooks/prd-readiness-guard ✅

# worktree 跑（cwd = /Users/x/Repo/.worktrees/feat-x）
$ cd .worktrees/feat-x/backend
$ ./mvnw verify
# Stop hook 报错：node /Users/x/Repo/.worktrees/feat-x/backend/.claude/hooks/prd-readiness-guard ❌
#   ↑ 路径错了（应该是 /Users/x/Repo/.claude/hooks/...）
```

### 示例 2：修后正常

```bash
# 改 .claude/settings.json 4 个 args 为绝对路径
$ cd .worktrees/feat-x/backend
$ ./mvnw verify
# Stop hook 正常：node /Users/x/Repo/.claude/hooks/prd-readiness-guard ✅
# （无论 cwd 在哪都正确）
```

### 示例 3：单克隆工作流（不 worktree）

```bash
# 普通 git clone + 不用 worktree
# 相对路径 .claude/hooks/X 在根目录能解析
# 不用改绝对路径
```

## Related

- **`.claude/settings.json`**：4 个 hook args 已改为绝对路径
- **`.worktrees/sprint-1/.claude/settings.json`**：worktree 内副本也同步改了
- **`docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`**：Sprint 1 总览
