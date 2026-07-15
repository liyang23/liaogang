#!/usr/bin/env bash
# scripts/test-env-down.sh - 停止测试环境
# 用途：docker compose 停止本地测试环境（默认保留数据）
# 用法：
#   ./scripts/test-env-down.sh         # 停止容器（数据卷保留，下次启动恢复）
#   ./scripts/test-env-down.sh --clean # 停止容器 + 清理数据卷（彻底重置）
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_DIR="$REPO_ROOT/deploy/docker"

CLEAN_DATA=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --clean|-c) CLEAN_DATA=true; shift ;;
    -h|--help)
      echo "用法：$0 [--clean]"
      echo "  --clean, -c   清理数据卷（mysql_data / redis_data / minio_data）"
      exit 0
      ;;
    *) echo "未知参数: $1"; exit 1 ;;
  esac
done

if ! command -v docker &> /dev/null; then
  echo "ERROR: Docker 未安装"
  exit 1
fi

if [ ! -d "$COMPOSE_DIR" ]; then
  echo "ERROR: docker-compose 目录不存在：$COMPOSE_DIR"
  exit 1
fi

cd "$COMPOSE_DIR"

if [ "$CLEAN_DATA" = "true" ]; then
  echo "=== 停止测试环境 + 清理数据卷 ==="
  docker compose down -v
  echo "  ✓ 容器已停止 + 数据卷已清理"
else
  echo "=== 停止测试环境（保留数据）==="
  docker compose down
  echo "  ✓ 容器已停止（数据卷保留：km_mysql_data / km_redis_data / km_minio_data）"
fi
echo ""
echo "下次启动：./scripts/test-env-up.sh 或 ./scripts/test-it.sh"
