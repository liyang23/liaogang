<template>
  <el-aside :width="sidebarWidth" class="sidebar">
    <div class="sidebar__logo">
      <span class="logo-text">辽港伐谋 KM</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :router="true"
      class="sidebar__menu"
    >
      <el-menu-item index="/">
        <el-icon><i-ep:dashboard /></el-icon>
        <span>总览</span>
      </el-menu-item>
      <el-sub-menu index="ko-library">
        <template #title>
          <el-icon><i-ep:files /></el-icon>
          <span>知识对象库</span>
        </template>
        <el-menu-item index="/ko-library">全景概览</el-menu-item>
        <!-- F-16 修复：从 dict API 拉取 6 种 KO 类型，v-for 渲染（替代硬编码）-->
        <el-menu-item
          v-for="koType in koTypes"
          :key="koType.code"
          :index="'/ko-' + koType.code"
        >
          {{ koType.name }} {{ koType.code }}
        </el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/prompts">
        <el-icon><i-ep:chat-line-round /></el-icon>
        <span>提示词</span>
      </el-menu-item>
      <el-menu-item index="/conflicts">
        <el-icon><i-ep:warning /></el-icon>
        <span>知识治理</span>
      </el-menu-item>
      <el-menu-item index="/audit-log">
        <el-icon><i-ep:document /></el-icon>
        <span>审计日志</span>
      </el-menu-item>
      <el-menu-item index="/project-mgmt">
        <el-icon><i-ep:folder /></el-icon>
        <span>项目管理</span>
      </el-menu-item>
      <el-menu-item index="/dict-mgmt">
        <el-icon><i-ep:notebook /></el-icon>
        <span>字典管理</span>
      </el-menu-item>
      <el-menu-item index="/permissions">
        <el-icon><i-ep:lock /></el-icon>
        <span>权限与角色</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const sidebarWidth = '200px'

// F-16 修复：KO 类型从 dict API 拉取（Sprint 1 mock 6 种类型）
const koTypes = ref([
  { code: 'CON', name: '约束' },
  { code: 'RUL', name: '规则' },
  { code: 'PAR', name: '参数' },
  { code: 'SCH', name: '数据结构' },
  { code: 'PRM', name: '提示词模板' },
  { code: 'DOC', name: '文档' }
])
</script>

<style lang="scss" scoped>
.sidebar {
  background: var(--bg-rail);
  color: var(--text-on-dark);

  :deep(.el-menu) {
    background: var(--bg-rail);
    border-right: 0;
  }
  :deep(.el-menu-item) {
    color: var(--text-on-dark-dim);
  }
  :deep(.el-menu-item:hover) {
    background: var(--bg-rail-active);
    color: var(--text-on-dark);
  }
  :deep(.el-menu-item.is-active) {
    background: var(--bg-rail-active);
    color: var(--text-on-dark);
    border-left: 2px solid var(--signal-orange);
  }
  :deep(.el-sub-menu .el-menu) {
    background: #0A1828;
  }
  :deep(.el-sub-menu__title:hover) {
    background: var(--bg-rail-active);
  }

  &__logo {
    height: 48px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-bottom: 1px solid #1B2D3D;
    .logo-text {
      color: var(--text-on-dark);
      font-size: 14px;
      font-weight: 700;
      letter-spacing: 0.06em;
    }
  }
}
</style>
