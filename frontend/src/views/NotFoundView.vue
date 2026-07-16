<!--
  NotFoundView V3 风格完整还原（F-53.2 T219）
  V3 原型：404 页面 + 大数字 + 链接回首页
-->
<template>
  <div class="not-found">
    <div class="nf-card">
      <div class="nf-code">404</div>
      <div class="nf-id">// NOT_FOUND · PAGE_MISSING</div>
      <div class="nf-title">页面未找到</div>
      <div class="nf-desc">
        请求的 URL <code class="mono">{{ route.path }}</code> 不存在或已被移除
      </div>
      <div class="nf-actions">
        <button class="btn btn-primary" @click="goHome">返回首页</button>
        <button class="btn" @click="goBack">返回上一页</button>
      </div>
      <div class="nf-quick">
        <span class="nf-quick__label">快速跳转：</span>
        <a class="nf-quick__link" @click.prevent="goPath('/ko-library')">知识库</a>
        <span class="nf-quick__sep">·</span>
        <a class="nf-quick__link" @click.prevent="goPath('/prompts')">提示词</a>
        <span class="nf-quick__sep">·</span>
        <a class="nf-quick__link" @click.prevent="goPath('/permissions')">权限</a>
        <span class="nf-quick__sep">·</span>
        <a class="nf-quick__link" @click.prevent="goPath('/conflicts')">治理</a>
        <span class="nf-quick__sep">·</span>
        <a class="nf-quick__link" @click.prevent="goPath('/audit-log')">审计</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

function goHome() { router.push('/') }
function goBack() { router.back() }
function goPath(path: string) { router.push(path) }
</script>

<style lang="scss" scoped>
.not-found { display: flex; align-items: center; justify-content: center; min-height: 60vh; padding: 40px 24px; }

.nf-card {
  background: var(--bg-paper); border: 1px solid var(--line);
  border-left: 3px solid var(--signal-red); border-radius: 2px;
  padding: 40px 60px; text-align: center; max-width: 600px;
}

.nf-code {
  font-family: 'JetBrains Mono', monospace; font-size: 96px;
  font-weight: 700; color: var(--signal-red); line-height: 1; margin-bottom: 8px;
}

.nf-id { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--text-tertiary); letter-spacing: 0.04em; margin-bottom: 16px; }

.nf-title { font-size: 18px; font-weight: 600; color: var(--text-primary); margin-bottom: 8px; }

.nf-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 24px;
  .mono { background: var(--bg-canvas); padding: 1px 6px; border-radius: 2px; color: var(--port-blue); }
}

.nf-actions { display: flex; gap: 8px; justify-content: center; margin-bottom: 24px; }

.nf-quick { font-size: 11px; color: var(--text-tertiary); padding-top: 16px; border-top: 1px dashed var(--line); }

.nf-quick__label { margin-right: 6px; }
.nf-quick__link { color: var(--port-blue); cursor: pointer; &:hover { text-decoration: underline; } }
.nf-quick__sep { margin: 0 6px; color: var(--line); }
</style>