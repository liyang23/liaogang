<!--
  权限矩阵组件（T206）
  13 菜单 × 5 操作 复选框矩阵
  按 3 组（主功能 / 治理 / 配置）分组显示
-->
<template>
  <el-table :data="tableData" border stripe>
    <el-table-column prop="menuName" label="菜单" min-width="180" fixed />
    <el-table-column
      v-for="op in OPERATIONS"
      :key="op.key"
      :label="op.label"
      align="center"
      width="80"
    >
      <template #default="{ row }">
        <el-checkbox
          :model-value="isAllowed(row.menuId, op.key)"
          @update:model-value="(val: boolean) => onToggle(row.menuId, op.key, val)"
        />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OPERATIONS, MENU_ITEMS, type Operation, type PermissionCell } from '@/api/role'

const props = defineProps<{
  /** 当前角色 ID */
  roleId: string
  /** 初始权限 cells（后端加载） */
  cells: PermissionCell[]
}>()

const emit = defineEmits<{
  (e: 'update', cells: PermissionCell[]): void
}>()

/** 转换为按 menuId 分组的查找表 */
const cellsByMenu = computed(() => {
  const map: Record<string, PermissionCell[]> = {}
  for (const cell of props.cells) {
    if (!map[cell.menuId]) map[cell.menuId] = []
    map[cell.menuId].push(cell)
  }
  return map
})

/** 13 菜单 × 3 组分组（v0.32 §4.1.5 权限查找指南） */
const tableData = computed(() => {
  // 按 group 分组排序
  const groups: Array<'主功能' | '治理' | '配置'> = ['主功能', '治理', '配置']
  const rows: Array<{ menuId: string; menuName: string; group: string }> = []
  for (const g of groups) {
    for (const m of MENU_ITEMS.filter(m => m.group === g)) {
      rows.push({ menuId: m.id, menuName: m.name, group: g })
    }
  }
  return rows
})

/** 查 cell 是否允许 */
function isAllowed(menuId: string, op: Operation): boolean {
  const cell = cellsByMenu.value[menuId]?.find(c => c.operation === op)
  return cell?.allowed ?? false
}

/** 切换 cell 触发 update 事件（父组件收集后调用 save API） */
function onToggle(menuId: string, op: Operation, val: boolean) {
  // 深拷贝 cells + 修改
  const newCells: PermissionCell[] = JSON.parse(JSON.stringify(props.cells))
  const idx = newCells.findIndex(c => c.menuId === menuId && c.operation === op)
  if (idx >= 0) {
    newCells[idx].allowed = val
  } else {
    newCells.push({ roleId: props.roleId, menuId, operation: op, allowed: val })
  }
  emit('update', newCells)
}
</script>

<style scoped>
/* Element Plus 默认 el-table 样式已足够 */
</style>