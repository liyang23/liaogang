#!/usr/bin/env bash
# check-v3-style.sh - F-53.3 自动检测 frontend view 是否符合 V3 视觉标准
# 来源：F-53 修复链路长期改进（处理链路阶段 3/3）
# 依据：docs/contracts/frontend-standards.md
# 用途：CI / pre-commit hook / PR review 必跑
set -e

# 路径配置
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VIEWS_DIR="$REPO_ROOT/frontend/src/views"
COMPONENTS_DIR="$REPO_ROOT/frontend/src/components"

# V3 主题色白名单（来源 theme.scss F-53.1 实施）
# 硬编码这些值是允许的（因为它们就是 V3 变量值本身）
ALLOWED_COLORS=(
  "#0F4C75"  # port-blue
  "#1E6A9D"  # port-blue-light
  "#ED8936"  # signal-orange
  "#C26418"  # signal-orange-deep
  "#C53030"  # signal-red
  "#2F855A"  # signal-green
  "#D69E2E"  # signal-yellow
  "#1A2332"  # text-primary
  "#5A6373"  # text-secondary
  "#8A92A0"  # text-tertiary
  "#E5EAF0"  # text-on-dark
  "#8FA0B5"  # text-on-dark-dim
  "#F5F6F8"  # bg-canvas
  "#FFFFFF"  # bg-paper
  "#0F1E2E"  # bg-rail
  "#173552"  # bg-rail-active
  "#ECEEF1"  # bg-grid
  "#DCE0E6"  # line
  "#B0B7C0"  # line-strong
  "#2D3748"  # steel
  "#0B3D60"  # port-blue-dark-2
  "#4A6B8E"  # port-blue-light-3
  "#6E89A6"  # port-blue-light-5
  "#92A7BF"  # port-blue-light-7
  "#A4B6C9"  # port-blue-light-8
  "#B6C5D3"  # port-blue-light-9
  "#000000"  # border-bottom: 1px solid #000 (V3 实际值)
  "#0A1828"  # sub-menu 深色
  "#1B2D3D"  # sidebar border
)

# V3 工具类白名单（必须使用，不能重新定义）
V3_CLASSES=(
  "page-header"
  "toolbar"
  "btn "
  "btn-primary"
  "stat-card"
  "stat-grid"
  "alert-item"
  "lst-item"
  "lst-item__"
  "trend-"
  "activity-list"
  "not-found"
  "stat-row"
  "main-row"
  "card-section"
  "section-title"
)

# 检测变量
ERRORS=0
CHECKS=0
WARNINGS=0

echo "=== F-53.3 V3 视觉标准检查 ==="
echo ""

# 1. 检查硬编码颜色（V3 白名单之外）
echo "--- 1. 检查硬编码颜色 ---"
for file in $(find "$VIEWS_DIR" "$COMPONENTS_DIR" -name "*.vue" 2>/dev/null); do
  # 提取十六进制颜色（6 位或 3 位）
  hex_colors=$(grep -oE "#[0-9a-fA-F]{6}|#[0-9a-fA-F]{3}\b" "$file" 2>/dev/null | sort -u || true)
  for color in $hex_colors; do
    # 转大写比较
    color_upper=$(echo "$color" | tr 'a-f' 'A-F')
    # 检查是否在白名单
    allowed=0
    for allow in "${ALLOWED_COLORS[@]}"; do
      if [ "$color_upper" = "$(echo "$allow" | tr 'a-f' 'A-F')" ]; then
        allowed=1
        break
      fi
    done
    if [ $allowed -eq 0 ]; then
      # 检查是否是 CSS 变量定义本身（如 :root { --port-blue: #0F4C75; } 允许）
      if grep -q "color_upper" "$file" 2>/dev/null; then :; fi
      # 找位置
      line_num=$(grep -nE "$color\\b" "$file" 2>/dev/null | head -1 | cut -d: -f1)
      echo "  ❌ $file:$line_num: 硬编码颜色 $color（应改用 V3 变量 var(--*)）"
      ERRORS=$((ERRORS + 1))
    fi
  done
done

if [ $ERRORS -eq 0 ]; then
  echo "  ✓ 未发现硬编码颜色"
fi

# 2. 检查 V3 工具类复用
echo ""
echo "--- 2. 检查 V3 工具类使用 ---"
for file in $(find "$VIEWS_DIR" -name "*.vue" 2>/dev/null); do
  # 检查 .vue 文件是否使用 V3 工具类（在 template 或 style 中）
  for cls in "${V3_CLASSES[@]}"; do
    if grep -qE "\\.${cls}" "$file" 2>/dev/null; then
      CHECKS=$((CHECKS + 1))
      break
    fi
  done
done

if [ $CHECKS -gt 0 ]; then
  echo "  ✓ $CHECKS 个 view 使用 V3 工具类"
else
  echo "  ⚠ 未发现 V3 工具类使用（可能是 placeholder view）"
fi

# 3. 检查 var(--port-blue) 使用（主题色应用率）
echo ""
echo "--- 3. 检查 V3 变量使用率 ---"
var_count=0
for file in $(find "$VIEWS_DIR" -name "*.vue" 2>/dev/null); do
  count=$(grep -cE "var\\(--[a-z-]+\\)" "$file" 2>/dev/null || echo 0)
  if [ "$count" -gt 0 ]; then
    var_count=$((var_count + count))
  fi
done

if [ $var_count -gt 0 ]; then
  echo "  ✓ 累计 V3 变量使用 $var_count 次（应 ≥ 50）"
else
  echo "  ⚠ V3 变量使用 0 次（应 ≥ 5/核心 view）"
  WARNINGS=$((WARNINGS + 1))
fi

# 4. 检查 el-row + el-col 反模式（V3 用 CSS Grid，不用 24 栅格）
echo ""
echo "--- 4. 检查 el-row + el-col 反模式 ---"
for file in $(find "$VIEWS_DIR" -name "*.vue" 2>/dev/null); do
  if grep -qE "<el-row" "$file" 2>/dev/null; then
    line_num=$(grep -nE "<el-row" "$file" 2>/dev/null | head -1 | cut -d: -f1)
    echo "  ⚠ $file:$line_num: 使用 <el-row>（V3 原型用 CSS Grid，替换为 <div class=\"stat-grid\">）"
    WARNINGS=$((WARNINGS + 1))
  fi
done

if [ $WARNINGS -eq 0 ]; then
  echo "  ✓ 未发现 el-row 反模式"
fi

# 5. 检查 border-radius: 8px 或以上（V3 是 2px）
echo ""
echo "--- 5. 检查 border-radius 反模式 ---"
for file in $(find "$VIEWS_DIR" -name "*.vue" 2>/dev/null); do
  big_radius=$(grep -nE "border-radius: [5-9]px|border-radius: 1[0-9]px" "$file" 2>/dev/null | head -3)
  if [ -n "$big_radius" ]; then
    echo "$big_radius" | while IFS= read -r line; do
      echo "  ⚠ $file: $line（V3 是 2px，用 .btn/.stat-card 工具类）"
      WARNINGS=$((WARNINGS + 1))
    done
  fi
done

if [ $WARNINGS -eq 0 ]; then
  echo "  ✓ 未发现 border-radius 反模式"
fi

# 总结
echo ""
echo "=== 总结 ==="
echo "  错误（硬编码颜色）: $ERRORS"
echo "  警告（反模式）: $WARNINGS"
echo "  V3 工具类使用: $CHECKS 个 view"
echo "  V3 变量使用: $var_count 次"

if [ $ERRORS -gt 0 ]; then
  echo ""
  echo "  ❌ F-53.3 检查失败：请修复硬编码颜色后重跑"
  exit 1
fi

if [ $WARNINGS -gt 0 ]; then
  echo ""
  echo "  ⚠ F-53.3 检查通过（有警告，建议优化）"
  exit 0
fi

echo ""
echo "  ✅ F-53.3 检查全部通过"
exit 0
