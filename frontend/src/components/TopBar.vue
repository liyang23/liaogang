<template>
  <el-header class="topbar">
    <div class="topbar__breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">总览</el-breadcrumb-item>
        <el-breadcrumb-item v-for="(crumb, i) in breadcrumbs" :key="i">
          {{ crumb }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="topbar__actions">
      <el-badge :value="3" class="topbar__notification">
        <el-tooltip content="通知" placement="bottom">
          <el-button :icon="Bell" circle />
        </el-tooltip>
      </el-badge>
      <el-dropdown @command="handleUserCommand">
        <span class="topbar__user">
          <el-avatar :size="28">{{ userInitials }}</el-avatar>
          <span class="topbar__user-name">{{ displayName }}</span>
          <span class="topbar__user-role">{{ currentRoleLabel }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const breadcrumbs = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  return segments.slice(1) // 去掉第一段（总览）
})

const displayName = computed(() => auth.displayName || (auth.token ? '会话失效' : '未登录'))
const userInitials = computed(() => displayName.value.slice(0, 2))
const currentRoleLabel = computed(() => {
  const map: Record<string, string> = {
    'ROLE-0001': '系统管理员',
    'ROLE-0002': '合规审核员',
    'ROLE-0003': '算法工程师',
    'ROLE-0004': '业务专家',
    'ROLE-0005': '只读观察者'
  }
  return map[auth.currentRole] || '未知'
})

function handleUserCommand(command: string) {
  if (command === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  background: var(--bg-rail);
  color: var(--text-on-dark);
  padding: 0 16px;
  border-bottom: 1px solid #000;

  &__breadcrumb {
    color: var(--text-on-dark);
    :deep(.el-breadcrumb__item) {
      color: var(--text-on-dark-dim);
    }
    :deep(.el-breadcrumb__item:last-child) {
      color: var(--text-on-dark);
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__notification {
    color: var(--text-on-dark);
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 0 8px;
    border-radius: 4px;
    transition: background 0.2s;
    &:hover {
      background: var(--bg-rail-active);
    }
  }
  &__user-name {
    color: var(--text-on-dark);
    font-size: 13px;
  }
  &__user-role {
    color: var(--text-on-dark-dim);
    font-size: 11px;
    padding: 2px 6px;
    background: var(--bg-rail-active);
    border-radius: 2px;
  }
}
</style>
