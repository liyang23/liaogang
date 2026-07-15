#!/usr/bin/env bash
# scripts/test-env-up.sh - 启动测试环境（MySQL + Redis + MinIO）
# 来源：TP-1 integration test environment
# 用途：docker compose 启动本地测试环境，供集成测试使用
# 配合：scripts/test-it.sh（up + test 一键）/ scripts/test-env-down.sh（停止）
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_DIR="$REPO_ROOT/deploy/docker"

echo "=== 启动测试环境（MySQL + Redis + MinIO）==="

# 1. 检查 docker
if ! command -v docker &> /dev/null; then
  echo "ERROR: Docker 未安装"
  exit 1
fi

# 2. 检查 docker compose v2
if ! docker compose version &> /dev/null; then
  echo "ERROR: Docker Compose v2 未安装（需要 'docker compose' 子命令）"
  echo "  参考：https://docs.docker.com/compose/install/"
  exit 1
fi

# 3. 启动容器
cd "$COMPOSE_DIR"
echo "  - docker compose up -d ..."
docker compose up -d

# 4. 等待健康检查通过（手动轮询，避免依赖 docker compose --wait 版本差异）
echo "  - 等待 MySQL 就绪（max 60s）..."
RETRIES=30
until docker exec km-mysql mysqladmin ping -h localhost -uroot -p'Zcx123456!' --silent 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [ "$RETRIES" -le 0 ]; then
    echo "ERROR: MySQL 启动超时"
    docker compose logs mysql
    exit 1
  fi
  sleep 2
done
echo "    ✓ MySQL OK (127.0.0.1:3306)"

echo "  - 等待 Redis 就绪（max 30s）..."
RETRIES=15
until docker exec km-redis redis-cli ping 2>/dev/null | grep -q PONG; do
  RETRIES=$((RETRIES - 1))
  if [ "$RETRIES" -le 0 ]; then
    echo "ERROR: Redis 启动超时"
    docker compose logs redis
    exit 1
  fi
  sleep 2
done
echo "    ✓ Redis OK (127.0.0.1:6379)"

echo "  - 等待 MinIO 就绪（max 60s）..."
RETRIES=30
until docker exec km-minio wget -q --spider http://localhost:9000/minio/health/live 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [ "$RETRIES" -le 0 ]; then
    echo "ERROR: MinIO 启动超时"
    docker compose logs minio
    exit 1
  fi
  sleep 2
done
echo "    ✓ MinIO OK (API 127.0.0.1:9001 / Console 127.0.0.1:9002)"

echo ""
echo "=== 测试环境已就绪 ==="
echo "  MySQL:  127.0.0.1:3306   root / Zcx123456!"
echo "           库: km_platform（dev）/ km_platform_it（it 自动创建）"
echo "  Redis:  127.0.0.1:6379   无密码（db 0 = dev / db 1 = it）"
echo "  MinIO:  127.0.0.1:9001   minioadmin / minioadmin@2026"
echo "          Console: http://127.0.0.1:9002"
echo ""
echo "下一步："
echo "  - 跑集成测试：./scripts/test-it.sh"
echo "  - 手动停止：./scripts/test-env-down.sh [--clean]"
echo "  - 查看日志：cd deploy/docker && docker compose logs -f"
echo "  - 查看状态：cd deploy/docker && docker compose ps"
