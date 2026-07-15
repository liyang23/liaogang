#!/usr/bin/env bash
# scripts/test-it.sh - 集成测试一键运行
# 来源：TP-1 integration test
# 用途：启动 MySQL + Redis + MinIO + 跑后端集成测试（it profile）
# 流程：test-env-up.sh → 等待就绪 → mvn test -Dspring.profiles.active=it → 可选 stop
#
# 用法：
#   ./scripts/test-it.sh                # 默认：up + test + 保留容器
#   ./scripts/test-it.sh --no-up        # 假设环境已就绪，仅跑测试
#   ./scripts/test-it.sh --stop         # 跑完后停止容器（保留数据）
#   ./scripts/test-it.sh --clean        # 跑完后停止容器 + 清理数据
#   ./scripts/test-it.sh --no-up --stop # 仅跑测试 + 停止（适合 CI）

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

UP_ENV=true
STOP_AFTER=false
CLEAN_AFTER=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-up)        UP_ENV=false; shift ;;
    --stop)         STOP_AFTER=true; shift ;;
    --clean|-c)     STOP_AFTER=true; CLEAN_AFTER=true; shift ;;
    -h|--help)
      echo "用法：$0 [--no-up] [--stop] [--clean]"
      echo "  --no-up     跳过 docker compose up（假设环境已就绪；适合 CI 重入）"
      echo "  --stop      测试完成后停止容器（保留数据卷）"
      echo "  --clean, -c 测试完成后停止容器 + 清理数据卷"
      echo ""
      echo "示例："
      echo "  $0                # 本地开发：up + 跑测试 + 容器继续运行"
      echo "  $0 --stop         # 跑完即停（保留数据）"
      echo "  $0 --no-up --stop # CI 模式：环境由外部托管，测试完即释放"
      exit 0
      ;;
    *) echo "未知参数: $1"; exit 1 ;;
  esac
done

# 1. 启动测试环境
if [ "$UP_ENV" = "true" ]; then
  "$SCRIPT_DIR/test-env-up.sh"
fi

# 2. 检查后端 mvnw
if [ ! -x "$REPO_ROOT/backend/mvnw" ]; then
  echo "ERROR: backend/mvnw 不存在或不可执行"
  exit 1
fi

# 3. 跑后端集成测试（failsafe 阶段，*IT.java；激活 it profile）
echo ""
echo "=== 跑后端集成测试（mvn verify / it profile）==="
echo "  - surefire:  *Test.java / *Tests.java（单元测试，不激活 it）"
echo "  - failsafe:  *IT.java（集成测试，激活 it profile → MySQL/Redis/MinIO 真实环境）"
cd "$REPO_ROOT/backend"
./mvnw verify -Dspring.profiles.active=it
TEST_EXIT=$?

# 4. 测试结果
echo ""
if [ $TEST_EXIT -eq 0 ]; then
  echo "=== 集成测试通过 ==="
else
  echo "=== 集成测试失败（exit=$TEST_EXIT）==="
fi

# 5. 收尾
if [ "$STOP_AFTER" = "true" ]; then
  echo ""
  if [ "$CLEAN_AFTER" = "true" ]; then
    "$SCRIPT_DIR/test-env-down.sh" --clean
  else
    "$SCRIPT_DIR/test-env-down.sh"
  fi
fi

exit $TEST_EXIT
