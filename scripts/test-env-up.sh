#!/usr/bin/env bash
# scripts/test-env-up.sh - 启动测试环境（MySQL + Redis + MinIO）
# 来源：TP-1 integration test environment
# 用途：探测 host 端口（3306/6379/9001）是否已被真实 MySQL/Redis/MinIO 占用；
#       若已被正确服务占用则跳过 docker compose up，否则启动容器并等待就绪。
# 配合：scripts/test-it.sh（up + test 一键）/ scripts/test-env-down.sh（停止）
# 注意：docker compose 起容器时**会跟 host 端口冲突**！仅在 host 未部署对应服务时才起容器。
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_DIR="$REPO_ROOT/deploy/docker"

echo "=== 启动测试环境（MySQL + Redis + MinIO）==="

# 1. 探测 host 端口是否已被真实服务占用（用协议层握手，避免误判）
#    函数成功（exit 0）= host 已有该服务；失败（exit 1）= 需起容器
probe_mysql() {
  python3 -c "
import socket, sys
try:
    s = socket.create_connection(('127.0.0.1', 3306), timeout=2)
    data = s.recv(1024)
    s.close()
    if len(data) > 4 and data[4] == 0x0A:
        end = data.index(0x00, 5)
        v = data[5:end].decode('utf-8', errors='replace')
        print(v)
        sys.exit(0)
except Exception:
    pass
sys.exit(1)
" 2>/dev/null
}

probe_redis() {
  python3 -c "
import socket, sys
try:
    s = socket.create_connection(('127.0.0.1', 6379), timeout=2)
    s.sendall(b'*1\r\n\$4\r\nPING\r\n')
    resp = s.recv(64)
    s.close()
    if b'PONG' in resp:
        sys.exit(0)
except Exception:
    pass
sys.exit(1)
" 2>/dev/null
}

probe_minio() {
  curl -sf -o /dev/null --max-time 2 http://127.0.0.1:9001/minio/health/live 2>/dev/null
}

# 2. 判断 host 服务可用性（用 exit code，不依赖 stdout）
NEED_COMPOSE=false
MYSQL_VER=""

if probe_mysql > /dev/null; then
  MYSQL_VER=$(probe_mysql)
  HOST_MYSQL_DESC="已部署（$MYSQL_VER）"
else
  NEED_COMPOSE=true
  HOST_MYSQL_DESC="未检测到（将启动容器）"
fi

if probe_redis; then
  HOST_REDIS_DESC="已部署（host）"
else
  NEED_COMPOSE=true
  HOST_REDIS_DESC="未检测到（将启动容器）"
fi

if probe_minio; then
  HOST_MINIO_DESC="已部署（host）"
else
  NEED_COMPOSE=true
  HOST_MINIO_DESC="未检测到（将启动容器）"
fi

echo "  - 探测 host 服务："
echo "    MySQL : $HOST_MYSQL_DESC"
echo "    Redis : $HOST_REDIS_DESC"
echo "    MinIO : $HOST_MINIO_DESC"

# 3. 如果全部已有 → 跳过 docker compose
if [ "$NEED_COMPOSE" = "false" ]; then
  echo ""
  echo "=== host 已部署全部 3 个服务，跳过 docker compose up ==="
  echo "（如需强制重启容器，先 ./scripts/test-env-down.sh --clean，再重跑本脚本）"
  echo ""
  echo "=== 测试环境已就绪（host 模式）==="
  echo "  MySQL:  127.0.0.1:3306   $MYSQL_VER"
  echo "  Redis:  127.0.0.1:6379"
  echo "  MinIO:  127.0.0.1:9001"
  echo ""
  echo "下一步："
  echo "  - 跑集成测试：./scripts/test-it.sh --no-up"
  exit 0
fi

# 4. 需要 docker compose：检查 docker / compose v2
if ! command -v docker &> /dev/null; then
  echo "ERROR: Docker 未安装（需要起容器补齐未部署的服务）"
  exit 1
fi
if ! docker compose version &> /dev/null; then
  echo "ERROR: Docker Compose v2 未安装（需要 'docker compose' 子命令）"
  echo "  参考：https://docs.docker.com/compose/install/"
  exit 1
fi

# 5. 启动容器
cd "$COMPOSE_DIR"
echo "  - docker compose up -d ..."
docker compose up -d

# 6. 等待健康检查通过
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
echo "    ✓ MySQL OK (容器内 3306)"

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
echo "    ✓ Redis OK (容器内 6379)"

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
echo "    ✓ MinIO OK (容器内 9000)"

echo ""
echo "=== 测试环境已就绪（容器模式）==="
echo "  MySQL:  127.0.0.1:3306   root / Zcx123456!   （容器: km-mysql）"
echo "  Redis:  127.0.0.1:6379   （容器: km-redis）"
echo "  MinIO:  127.0.0.1:9001   minioadmin / minioadmin@2026   （容器: km-minio）"
echo "  MinIO Console: http://127.0.0.1:9002"
echo ""
echo "下一步："
echo "  - 跑集成测试：./scripts/test-it.sh"
echo "  - 停止容器（保留数据）：./scripts/test-env-down.sh"
echo "  - 停止 + 清数据：./scripts/test-env-down.sh --clean"
