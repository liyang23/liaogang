#!/usr/bin/env bash
# scripts/seed.sh - 加载 seed 数据
# 来源：TP-1 T001 + T010
# 用途：Flyway V9001 seed 加载（4 项目 + 5 角色 + 6 类型 KO 278 条 + 6 字典 + 9 量纲）
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== Flyway V9001 seed 加载 ==="
echo "（依赖 MySQL + Spring Boot 启动后自动执行）"
echo ""
echo "Seed 内容："
echo "  - 4 项目：PROJ-0001（121 KO）/ PROJ-0002（72）/ PROJ-0003（49）/ PROJ-0004（36 归档）"
echo "  - 5 预置角色：ROLE-0001 系统管理员 / ROLE-0002 合规审核员 / ROLE-0003 算法工程师 / ROLE-0004 业务专家 / ROLE-0005 只读观察者"
echo "  - 6 类型 KO 共 278 条：CON 19 / RUL 47 / PAR 92 / SCH 41 / PRM 3 / DOC 76"
echo "  - 6 字典：类型介绍 / 效力分级 / 权威分级 / 类型分组名称 / 知识对象概念 / 量纲配置"
echo "  - 9 量纲：% / 元/TEU / h / 条 / 辆/岸桥 / 栏 / 人/班 / 台/班 / 箱/h"
echo ""
echo "如需手动触发："
echo "  cd backend && ./mvnw flyway:migrate"
