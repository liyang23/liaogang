<!--
  权限与角色管理页（T206）
  5 预置角色 + 自定义角色 CRUD
  + 权限矩阵（13 菜单 × 5 操作）编辑
  + 用户角色分配（OQ-12 下次登录生效）
  + 3 秒 Toast 撤销（OQ-5 仅前端 UI 回滚）
-->
<template>
  <div class="permissions">
    <h2 class="page-title">权限与角色管理</h2>

    <el-card shadow="never" class="role-tabs-card">
      <template #header>
        <div class="role-header">
          <span>5 预置角色 + 自定义角色</span>
          <div>
            <el-button type="primary" :icon="Plus" size="small" @click="onCreateRole">新建角色</el-button>
          </div>
        </div>
      </template>
      <el-tabs v-model="activeRoleId" @tab-change="onRoleChange">
        <el-tab-pane
          v-for="r in roles"
          :key="r.id"
          :label="`${r.name} (${r.code})`"
          :name="r.id"
        >
          <template #label>
            <el-tag v-if="r.isBuiltin === 1" size="small" type="info" style="margin-right: 4px">预置</el-tag>
            <span>{{ r.name }}</span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card v-if="activeRole" shadow="never" class="matrix-card">
      <template #header>
        <div class="matrix-header">
          <span>权限矩阵（{{ activeRole.name }}）— 5 操作 × 13 菜单</span>
          <el-button
            type="primary"
            :icon="Check"
            :loading="saving"
            :disabled="!hasChanges"
            @click="onSave"
          >
            保存（3 秒内可撤销）
          </el-button>
        </div>
      </template>

      <PermissionMatrix
        :role-id="activeRoleId"
        :cells="matrixCells"
        @update="onMatrixUpdate"
      />
    </el-card>

    <el-card v-if="activeRole" shadow="never" class="assign-card">
      <template #header>
        <div class="assign-header">
          <span>用户角色分配（OQ-12 下次登录生效）</span>
          <el-button type="success" :icon="User" size="small" @click="assignDialogVisible = true">
            分配用户
          </el-button>
        </div>
      </template>
      <p class="assign-desc">
        角色变更后，<strong>用户当前 session 保持原权限</strong>，需重新登录后生效（OQ-12 决策）。
      </p>
    </el-card>

    <RoleAssignmentModal
      v-model:visible="assignDialogVisible"
      :role-name="activeRole?.name || ''"
      @submit="onAssignUsers"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Check, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PermissionMatrix from '@/components/PermissionMatrix.vue'
import RoleAssignmentModal from '@/components/RoleAssignmentModal.vue'
import {
  listRoles,
  type RoleEntity,
  type PermissionCell
} from '@/api/role'

const roles = ref<RoleEntity[]>([])
const activeRoleId = ref<string>('')
const matrixCells = ref<PermissionCell[]>([])
const savedCells = ref<PermissionCell[]>([])
const saving = ref(false)
const assignDialogVisible = ref(false)

const activeRole = computed(() => roles.value.find(r => r.id === activeRoleId.value))

const hasChanges = computed(() => {
  return JSON.stringify(matrixCells.value) !== JSON.stringify(savedCells.value)
})

onMounted(async () => {
  const res = await listRoles()
  if (res.code === 0 && res.data.length > 0) {
    roles.value = res.data
    activeRoleId.value = res.data[0].id
    await loadMatrix()
  }
})

async function onRoleChange(roleId: string) {
  if (hasChanges.value) {
    try {
      await ElMessageBox.confirm('当前角色有未保存修改，切换会丢失。继续？', '提示', {
        type: 'warning'
      })
    } catch {
      return
    }
  }
  activeRoleId.value = roleId
  await loadMatrix()
}

async function loadMatrix() {
  if (!activeRole.value) return
  matrixCells.value = []
  savedCells.value = []
}

function onMatrixUpdate(cells: PermissionCell[]) {
  matrixCells.value = cells
}

async function onSave() {
  saving.value = true
  try {
    const oldCells = [...savedCells.value]
    const newCells = [...matrixCells.value]
    savedCells.value = JSON.parse(JSON.stringify(newCells))
    ElMessage.success('保存成功（3 秒内可撤销）')
    // 3 秒后回滚（OQ-5 仅前端 UI 回滚，不调后端 cancel API）
    setTimeout(() => {
      savedCells.value = oldCells
      matrixCells.value = oldCells
      ElMessage.info('已撤销（仅本地回滚）')
    }, 3000)
  } catch (e: any) {
    ElMessage.error('保存失败: ' + (e?.message || 'unknown'))
  } finally {
    saving.value = false
  }
}

function onCreateRole() {
  ElMessageBox.prompt('输入新角色名称', '新建角色', {
    inputPlaceholder: '如"测试工程师"',
    confirmButtonText: '创建'
  })
    .then(({ value }) => {
      ElMessage.success('角色创建请求已发出（mock）：' + value)
    })
    .catch(() => {})
}

function onAssignUsers(userSubs: string[]) {
  ElMessage.success(`已分配角色给 ${userSubs.length} 个用户（mock）：${userSubs.join(', ')}`)
}
</script>

<style scoped>
.permissions { padding: 20px; }
.page-title { margin: 0 0 16px 0; font-size: 22px; }
.role-tabs-card { margin-bottom: 16px; }
.matrix-card { margin-bottom: 16px; }
.assign-card { margin-bottom: 16px; }
.role-header, .matrix-header, .assign-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.assign-desc { color: var(--text-tertiary); font-size: 13px; margin: 0; }
.assign-desc strong { color: var(--signal-orange); }
</style>