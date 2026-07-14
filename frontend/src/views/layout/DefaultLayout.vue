<template>
  <div class="default-layout">
    <TopBar />
    <div class="default-layout__body">
      <Sidebar />
      <main class="default-layout__content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import TopBar from '@/components/TopBar.vue'
import Sidebar from '@/components/Sidebar.vue'
// T009 实现跨切组件（已 stub 引用）
</script>

<style lang="scss" scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-canvas);

  &__body {
    display: flex;
    flex: 1;
    overflow: hidden;
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 16px 24px;
  }
}
</style>
