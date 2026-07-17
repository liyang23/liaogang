<!--
  StaleSnapshotBadge.vue - U8 陈旧快照黄色 badge (T306)
  当 Snapshot.stale=true 时显示在时间线节点旁
-->
<template>
  <span class="stale-badge" :class="{ active: stale }" :aria-label="stale ? '陈旧快照' : '正常快照'">
    <span class="dot" />
    <span class="text">{{ stale ? 'STALE' : 'OK' }}</span>
  </span>
</template>

<script setup lang="ts">
defineProps<{
  stale: boolean
}>()
</script>

<style scoped>
.stale-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  font-family: 'JetBrains Mono', monospace;
  line-height: 1;
}
.stale-badge .dot {
  width: 4px; height: 4px;
  border-radius: 50%;
}
.stale-badge:not(.active) {
  background: var(--bg-paper, #f0f4fa);
  color: var(--text-tertiary, #909399);
}
.stale-badge:not(.active) .dot {
  background: var(--text-tertiary, #909399);
}
.stale-badge.active {
  background: var(--signal-orange-bg, #fff3e0);
  color: var(--signal-orange, #e67e22);
}
.stale-badge.active .dot {
  background: var(--signal-orange, #e67e22);
}
</style>
