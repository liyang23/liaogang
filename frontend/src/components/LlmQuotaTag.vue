<!--
  LlmQuotaTag.vue - LLM 配额 4 态 chip 组件 (T303)
  normal / low / exhausted / loading 4 态 (origin R11c chip 视觉规范)
-->
<template>
  <span class="llm-quota-tag" :class="stateClass" :data-state="state">
    <span class="dot" />
    <span class="text">{{ displayText }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  /** normal / low / exhausted / loading 4 态 */
  state?: 'normal' | 'low' | 'exhausted' | 'loading'
  used?: number
  limit?: number
}>(), {
  state: 'normal',
  used: 0,
  limit: 100,
})

const stateClass = computed(() => `state-${props.state}`)

const displayText = computed(() => {
  if (props.state === 'loading') return '调用中…'
  if (props.state === 'exhausted') return `0/${props.limit} (已耗尽)`
  return `${props.used}/${props.limit}`
})
</script>

<style scoped>
.llm-quota-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  line-height: 1;
}
.llm-quota-tag .dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  display: inline-block;
}
.state-normal { background: var(--bg-paper, #f0f4fa); color: var(--port-blue, #2c5dba); }
.state-normal .dot { background: var(--port-blue, #2c5dba); }
.state-low { background: var(--signal-orange-bg, #fff3e0); color: var(--signal-orange, #e67e22); }
.state-low .dot { background: var(--signal-orange, #e67e22); }
.state-exhausted { background: var(--signal-red-bg, #ffe5e5); color: var(--signal-red, #d33); }
.state-exhausted .dot { background: var(--signal-red, #d33); }
.state-loading { background: var(--bg-paper, #f0f4fa); color: var(--text-tertiary, #909399); }
.state-loading .dot { background: var(--text-tertiary, #909399); animation: pulse 1.5s infinite; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
