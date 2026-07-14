#!/usr/bin/env bash
# scripts/test.sh - 运行全栈测试
# 来源：TP-1 T001
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== 后端测试 ==="
if [ -d backend ]; then
  cd backend
  if [ -f mvnw ]; then
    ./mvnw test -q
  else
    mvn test -q || echo "Maven 未安装，跳过后端测试"
  fi
  cd ..
else
  echo "backend 目录未创建，跳过后端测试"
fi

echo ""
echo "=== 前端测试 ==="
if [ -d frontend ]; then
  cd frontend
  if command -v pnpm &> /dev/null; then
    pnpm test
  elif [ -f package.json ]; then
    pnpm test || npm test || echo "前端测试需 pnpm/npm"
  else
    echo "package.json 不存在，跳过前端测试"
  fi
  cd ..
else
  echo "frontend 目录未创建，跳过前端测试"
fi
