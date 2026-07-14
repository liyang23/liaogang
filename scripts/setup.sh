#!/usr/bin/env bash
# scripts/setup.sh - Monorepo 本地开发环境初始化
# 来源：TP-1 T001 (Sprint 1 基础架构)
# 用途：clone 后首次启动本地开发环境
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== 辽港伐谋 KM 平台 - 本地开发环境初始化 ==="

# 1. 验证 Node.js
if ! command -v node &> /dev/null; then
  echo "ERROR: Node.js 未安装（需要 >= 18）"
  exit 1
fi
NODE_MAJOR=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_MAJOR" -lt 18 ]; then
  echo "ERROR: Node.js 版本过低（需要 >= 18，当前 $(node -v)）"
  exit 1
fi
echo "Node.js: $(node -v)"

# 2. 验证 Java
if ! command -v java &> /dev/null; then
  echo "ERROR: Java 未安装（需要 >= 17）"
  exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_MAJOR" -lt 17 ]; then
  echo "ERROR: Java 版本过低（需要 >= 17，当前 $(java -version 2>&1 | head -1)）"
  exit 1
fi
echo "Java: $(java -version 2>&1 | head -1)"

# 3. 验证 Maven
if ! command -v mvn &> /dev/null; then
  echo "WARN: Maven 未安装，建议安装 Maven 3.9+ 或使用 ./mvnw"
fi

# 4. 验证 Docker
if ! command -v docker &> /dev/null; then
  echo "WARN: Docker 未安装（K8s + MySQL + MinIO + Redis 需 Docker）"
fi

# 5. 创建目录结构（如缺失）
mkdir -p frontend/src/{api,components,composables,directives,router,stores,utils,views,assets}
mkdir -p backend/src/{main,test}/java/com/liaogang/famou/km
mkdir -p backend/src/main/resources/{db/migration,seed}
mkdir -p deploy/helm/{templates,charts}
mkdir -p docs/{brainstorms,plans,tasks,contracts}
mkdir -p scripts

# 6. 复制 .env.example
if [ ! -f .env.example ] && [ -f .env.template ]; then
  cp .env.template .env.example
fi

echo ""
echo "=== 目录结构已创建 ==="
echo "下一步："
echo "  1. 启动 MySQL + MinIO + Redis（docker-compose up -d）"
echo "  2. 启动后端（cd backend && ./mvnw spring-boot:run）"
echo "  3. 启动前端（cd frontend && pnpm install && pnpm dev）"
echo ""
echo "详见 docs/plans/2026-07-13-001-feat-liaogang-famou-km-platform-mvp-plan.md"
