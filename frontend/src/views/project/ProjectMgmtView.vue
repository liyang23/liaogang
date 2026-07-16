<!--
  ProjectMgmtView V3 风格完整还原（F-53.2 T216）
  V3 原型：4 项目卡片（PROJ-0001~0004 活动/归档）+ KO 数 + 最后活动
-->
<template>
  <div class="project-mgmt">
    <div class="page-header">
      <div class="title-row">
        <h1>项目管理 <span class="id">// PROJECTS · MANAGEMENT</span></h1>
        <div class="right-actions">
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="搜索项目代码 / 名称..."
          />
          <button class="btn" @click="onExport"><span>⤓</span> 导出</button>
          <button class="btn btn-primary" @click="onCreate"><span>+</span> 新建项目</button>
        </div>
      </div>
      <p class="subtitle">4 个项目 · 3 活动 + 1 归档（PROJ-0004 散货码头 2026-07-01 归档）· 共 278 KO 跨项目分布</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">项目总数</div><div class="value">4</div><div class="delta">+0</div><div class="breakdown">3 活动 + 1 归档</div></div>
      <div class="stat-card success"><div class="label">活动项目</div><div class="value">3</div><div class="delta">+0</div><div class="breakdown">PROJ-0001/2/3 正常推进</div></div>
      <div class="stat-card warn"><div class="label">归档项目</div><div class="value">1</div><div class="delta">+1 本月</div><div class="breakdown">PROJ-0004 散货码头 7-1 归档</div></div>
      <div class="stat-card"><div class="label">跨项目 KO</div><div class="value">278</div><div class="delta">+12 本周</div><div class="breakdown">V9001 seed 分布</div></div>
    </div>

    <div class="project-grid">
      <div
        v-for="project in filteredProjects"
        :key="project.code"
        class="project-card"
        :class="project.statusClass"
        @click="onProjectClick(project.code)"
      >
        <div class="project-card__header">
          <div class="project-card__code">{{ project.code }}</div>
          <div class="project-card__status" :class="project.statusClass">{{ project.statusLabel }}</div>
        </div>
        <div class="project-card__name">{{ project.name }}</div>
        <div class="project-card__meta">
          <div class="meta-row"><span class="meta-label">KO 数</span><span class="meta-value mono">{{ project.koCount }}</span></div>
          <div class="meta-row"><span class="meta-label">组织</span><span class="meta-value">{{ project.organization || '—' }}</span></div>
          <div class="meta-row"><span class="meta-label">最后活动</span><span class="meta-value">{{ project.lastActivity }}</span></div>
        </div>
        <div class="project-card__actions">
          <button class="btn">查看 KO</button>
          <button class="btn" v-if="project.status === 'active'">归档</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Project {
  code: string; name: string
  status: 'active' | 'archived'
  statusClass: 'status-active' | 'status-archived'
  statusLabel: string
  koCount: number
  organization: string
  lastActivity: string
}

const searchQuery = ref('')

const projects: Project[] = [
  { code: 'PROJ-0001', name: '大窑湾统筹优化', status: 'active', statusClass: 'status-active', statusLabel: '活动中', koCount: 121, organization: '辽港集团（10000510）', lastActivity: '2 小时前' },
  { code: 'PROJ-0002', name: '堆场计划优化', status: 'active', statusClass: 'status-active', statusLabel: '活动中', koCount: 72, organization: '—', lastActivity: '昨天' },
  { code: 'PROJ-0003', name: '泊位分配算法', status: 'active', statusClass: 'status-active', statusLabel: '活动中', koCount: 49, organization: '—', lastActivity: '3 天前' },
  { code: 'PROJ-0004', name: '散货码头调度', status: 'archived', statusClass: 'status-archived', statusLabel: '已归档', koCount: 36, organization: '—', lastActivity: '2026-07-01 归档' }
]

const filteredProjects = computed(() => {
  if (!searchQuery.value) return projects
  const q = searchQuery.value.toLowerCase()
  return projects.filter(p => p.code.toLowerCase().includes(q) || p.name.toLowerCase().includes(q))
})

function onExport() { ElMessage.success('导出：U9 实施时生成完整导出') }
function onCreate() { ElMessage.info('新建项目：U9 实施时完整 CRUD') }
function onProjectClick(code: string) { ElMessage.info(`详情：${code}（U9 实施时跳详情页）`) }
</script>

<style lang="scss" scoped>
.project-mgmt { padding: 16px 24px; }

.search-input {
  width: 240px; height: 28px; padding: 0 8px;
  background: var(--bg-paper); border: 1px solid var(--line);
  border-radius: 2px; color: var(--text-primary);
  font-size: 13px; margin-right: 4px;
}

.project-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}

.project-card {
  background: var(--bg-paper); border: 1px solid var(--line);
  border-left: 3px solid var(--port-blue); border-radius: 2px;
  padding: 16px 20px; cursor: pointer; transition: all 0.15s;
  &:hover { border-left-color: var(--port-blue-light); transform: translateX(2px); }
  &.status-archived { border-left-color: var(--text-tertiary); opacity: 0.7; }
}

.project-card__header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }

.project-card__code {
  font-family: 'JetBrains Mono', monospace; font-size: 13px;
  color: var(--port-blue); font-weight: 700;
}

.project-card__status {
  font-size: 10px; padding: 2px 6px; border-radius: 2px; font-weight: 600;
}

.status-active { background: rgba(47, 133, 90, 0.1); color: var(--signal-green); }
.status-archived { background: rgba(140, 140, 140, 0.1); color: var(--text-tertiary); }

.project-card__name { font-size: 15px; font-weight: 500; color: var(--text-primary); margin-bottom: 12px; }

.project-card__meta { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; font-size: 11px; }

.meta-row { display: flex; gap: 8px; }
.meta-label { color: var(--text-tertiary); min-width: 60px; }
.meta-value { color: var(--text-primary); &.mono { color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; font-size: 10px; } }

.project-card__actions { display: flex; gap: 6px; }
</style>