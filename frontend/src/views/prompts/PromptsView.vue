<!--
  PromptsView V3 风格完整还原（F-53.2 T217）
  V3 原型：3 个 PRM 模板卡片（KO-PRM-0001/0002/0003）+ Section 数 + 新建按钮
-->
<template>
  <div class="prompts">
    <div class="page-header">
      <div class="title-row">
        <h1>提示词 <span class="id">// PRM · PROMPT TEMPLATE</span></h1>
        <div class="right-actions">
          <button class="btn" @click="onImport"><span>⤓</span> 导入</button>
          <button class="btn" @click="onExport"><span>⤓</span> 导出</button>
          <button class="btn btn-primary" @click="onCreate"><span>+</span> 新建模板</button>
        </div>
      </div>
      <p class="subtitle">3 个 PRM 模板（KO-PRM-0001 / 0002 / 0003）· 17 个 Section 总数 · FIXED + DYNAMIC 两种 Section 模式 · 三栏组装器（U6）</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">PRM 模板</div><div class="value">3</div><div class="delta">+0</div><div class="breakdown">V9004 seed 初始化</div></div>
      <div class="stat-card success"><div class="label">Section 总数</div><div class="value">17</div><div class="delta">+0</div><div class="breakdown">9 + 3 + 5</div></div>
      <div class="stat-card"><div class="label">FIXED 模式</div><div class="value">12</div><div class="delta">变量赋值</div><div class="breakdown">含 {{var}} 占位</div></div>
      <div class="stat-card warn"><div class="label">DYNAMIC 模式</div><div class="value">5</div><div class="delta">动态选择</div><div class="breakdown">KO 选 + 手动子项</div></div>
    </div>

    <div class="prm-grid">
      <div
        v-for="prm in prms"
        :key="prm.code"
        class="prm-card"
        @click="onPrmClick(prm.code)"
      >
        <div class="prm-card__header">
          <div class="prm-card__code">{{ prm.code }}</div>
          <div class="prm-card__version">{{ prm.version }}</div>
        </div>
        <div class="prm-card__name">{{ prm.name }}</div>
        <div class="prm-card__desc">{{ prm.description }}</div>
        <div class="prm-card__stats">
          <div class="stat-item"><span class="stat-item__label">Section</span><span class="stat-item__value">{{ prm.sectionCount }}</span></div>
          <div class="stat-item"><span class="stat-item__label">FIXED</span><span class="stat-item__value">{{ prm.fixedCount }}</span></div>
          <div class="stat-item"><span class="stat-item__label">DYNAMIC</span><span class="stat-item__value">{{ prm.dynamicCount }}</span></div>
        </div>
        <div class="prm-card__action"><button class="btn btn-primary">打开组装器</button></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

interface Prm {
  code: string; name: string; description: string; version: string
  sectionCount: number; fixedCount: number; dynamicCount: number
}

const prms: Prm[] = [
  { code: 'KO-PRM-0001', name: '大窑湾统筹优化', description: '大窑湾集装箱码头泊位-岸桥-堆场-拖车-空叉统筹调度优化', version: 'v1.0.0', sectionCount: 9, fixedCount: 7, dynamicCount: 2 },
  { code: 'KO-PRM-0002', name: '堆场计划优化', description: '进口堆场箱区分配与翻箱率优化算法', version: 'v1.0.0', sectionCount: 3, fixedCount: 3, dynamicCount: 0 },
  { code: 'KO-PRM-0003', name: '泊位分配算法', description: '基于深度强化学习的动态泊位分配算法研发', version: 'v1.0.0', sectionCount: 5, fixedCount: 2, dynamicCount: 3 }
]

function onImport() { ElMessage.info('导入：U6 实施时完整 PRM 导入') }
function onExport() { ElMessage.success('导出：U6 实施时完整 PRM 导出') }
function onCreate() { ElMessage.info('新建模板：U6 实施时完整 CRUD') }
function onPrmClick(code: string) { ElMessage.info(`打开组装器：${code}（U6 三栏组装器 /composer/${code}）`) }
</script>

<style lang="scss" scoped>
.prompts { padding: 16px 24px; }

.prm-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}

.prm-card {
  background: var(--bg-paper); border: 1px solid var(--line);
  border-left: 3px solid var(--port-blue); border-radius: 2px;
  padding: 16px 20px; cursor: pointer; transition: all 0.15s;
  &:hover { border-left-color: var(--port-blue-light); transform: translateX(2px); }
}

.prm-card__header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.prm-card__code { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--port-blue); font-weight: 700; }
.prm-card__version { font-family: 'JetBrains Mono', monospace; font-size: 10px; color: var(--text-tertiary); }

.prm-card__name { font-size: 14px; font-weight: 500; color: var(--text-primary); margin-bottom: 6px; }
.prm-card__desc { font-size: 11px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 12px; }

.prm-card__stats { display: flex; gap: 16px; margin-bottom: 12px; padding-top: 8px; border-top: 1px dashed var(--line); }

.stat-item { display: flex; flex-direction: column; }
.stat-item__label { font-size: 9px; color: var(--text-tertiary); letter-spacing: 0.04em; margin-bottom: 2px; }
.stat-item__value { font-family: 'JetBrains Mono', monospace; font-size: 16px; font-weight: 700; color: var(--text-primary); }

.prm-card__action { text-align: right; }
</style>