<!--
  DashboardView V3 风格完整还原（F-53.2 T212）
  V3 原型区块：
  - .page-header 标题 + 副标题 + 右侧动作
  - .stat-card × 4 统计（KO 总数 / 已生效 / 待处置 / 紧急告警）+ delta 增量
  - 项目活动 .lst-item 列表（4 项目）
  - 最近活动 .lst-item（审核 / 创建 / 发布）
  - 趋势占位（V3 用 ASCII art 折线图，T212+ 后端数据齐全后接 echarts）
-->
<template>
  <div class="dashboard">
    <!-- V3 page-header -->
    <div class="page-header">
      <div class="title-row">
        <h1>
          总览
          <span class="id">// DASHBOARD · OVERVIEW</span>
        </h1>
        <div class="right-actions">
          <button class="btn" @click="onExport">
            <span>⤓</span> 导出报告
          </button>
          <button class="btn btn-primary" @click="onRefresh">
            <span>↻</span> 刷新
          </button>
        </div>
      </div>
      <p class="subtitle">知识对象库总览 · 6 类型 · 5 预置角色 · 4 项目 · 当前会话 ROLE-0001（系统管理员）</p>
    </div>

    <!-- V3 4 个 stat-card 统计（CSS Grid 4 列，不依赖 Element Plus el-row） -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">知识对象总数</div>
        <div class="value">278</div>
        <div class="delta">+12 本周</div>
        <div class="breakdown">CON 19 · RUL 47 · PAR 92 · SCH 41 · PRM 3 · DOC 76</div>
      </div>
      <div class="stat-card success">
        <div class="label">已生效 KO</div>
        <div class="value">231</div>
        <div class="delta">+8 本周</div>
        <div class="breakdown">占总 KO 83%</div>
      </div>
      <div class="stat-card warn">
        <div class="label">待处置治理项</div>
        <div class="value">7</div>
        <div class="delta down">-2 本周</div>
        <div class="breakdown">3 冲突 + 4 警告</div>
      </div>
      <div class="stat-card danger">
        <div class="label">紧急告警</div>
        <div class="value">1</div>
        <div class="delta">+1 今日</div>
        <div class="breakdown">PRM-DRAFT 超时未审</div>
      </div>
    </div>

    <!-- V3 dashboard-row 2fr 1fr 布局：项目活动 2 份 + 最近活动 1 份 -->
    <div class="dashboard-row">
      <!-- 左：4 项目活动（V3 .lst-item） -->
      <div class="card-section">
        <h3 class="section-title">项目活动 <span class="section-meta">4 个项目 · 3 活动 · 1 归档</span></h3>
        <div class="lst-list">
          <div
            v-for="project in projects"
            :key="project.code"
            class="lst-item"
            @click="onProjectClick(project.code)"
          >
            <div class="lst-item__header">
              <div class="lst-item__code">{{ project.code }}</div>
              <div class="lst-item__status" :class="project.statusClass">
                {{ project.statusLabel }}
              </div>
            </div>
            <div class="lst-item__title">{{ project.name }}</div>
            <div class="lst-item__meta">
              KO 数 <span class="mono">{{ project.koCount }}</span> · 最后活动
              <span class="mono">{{ project.lastActivity }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右：最近活动（V3 .lst-item） -->
      <div class="card-section">
        <h3 class="section-title">最近活动 <span class="section-meta">最近 10 条</span></h3>
        <div class="lst-list">
          <div
            v-for="activity in activities"
            :key="activity.id"
            class="lst-item lst-item--activity"
            @click="onActivityClick(activity.id)"
          >
            <div class="lst-item__icon">{{ activity.icon }}</div>
            <div class="lst-item__body">
              <div class="lst-item__title">
                <span class="mono">{{ activity.action }}</span>
                ·
                {{ activity.target }}
              </div>
              <div class="lst-item__meta">
                <span class="mono">{{ activity.user }}</span> · {{ activity.time }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：趋势占位（V3 实际用 ASCII art，T212+ 后端数据齐全后接 echarts） -->
    <div class="card-section">
      <h3 class="section-title">
        KO 创建趋势 <span class="section-meta">近 7 天 · 按类型</span>
      </h3>
      <div class="trend-placeholder">
        <div class="trend-ascii">
          <pre>{{ trendAscii }}</pre>
        </div>
        <div class="trend-legend">
          <span class="legend-item"><span class="dot dot-con" /> CON 19</span>
          <span class="legend-item"><span class="dot dot-rul" /> RUL 47</span>
          <span class="legend-item"><span class="dot dot-par" /> PAR 92</span>
          <span class="legend-item"><span class="dot dot-sch" /> SCH 41</span>
          <span class="legend-item"><span class="dot dot-prm" /> PRM 3</span>
          <span class="legend-item"><span class="dot dot-doc" /> DOC 76</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// ===== Mock 数据（V9001 seed + 近期活动，TODO T212+ 接真实 API） =====

interface Project {
  code: string
  name: string
  statusClass: 'status-active' | 'status-archived'
  statusLabel: string
  koCount: number
  lastActivity: string
}

const projects: Project[] = [
  {
    code: 'PROJ-0001',
    name: '大窑湾统筹优化',
    statusClass: 'status-active',
    statusLabel: '活动中',
    koCount: 121,
    lastActivity: '2 小时前'
  },
  {
    code: 'PROJ-0002',
    name: '堆场计划优化',
    statusClass: 'status-active',
    statusLabel: '活动中',
    koCount: 72,
    lastActivity: '昨天'
  },
  {
    code: 'PROJ-0003',
    name: '泊位分配算法',
    statusClass: 'status-active',
    statusLabel: '活动中',
    koCount: 49,
    lastActivity: '3 天前'
  },
  {
    code: 'PROJ-0004',
    name: '散货码头调度（已归档）',
    statusClass: 'status-archived',
    statusLabel: '已归档',
    koCount: 36,
    lastActivity: '2026-07-01'
  }
]

interface Activity {
  id: number
  icon: string
  action: string
  target: string
  user: string
  time: string
}

const activities: Activity[] = [
  { id: 1, icon: '✓', action: 'KO_PUBLISH', target: 'KO-CON-0019',  user: '李雷（系统管理员）', time: '2 分钟前' },
  { id: 2, icon: '✎', action: 'KO_UPDATE',   target: 'KO-RUL-0047',  user: '韩梅梅（业务专家）', time: '15 分钟前' },
  { id: 3, icon: '⊕', action: 'KO_CREATE',   target: 'KO-PAR-0093',  user: '张三（算法工程师）', time: '1 小时前' },
  { id: 4, icon: '⏱', action: 'KO_REVIEW',   target: 'KO-PRM-0003',  user: '王五（合规审核员）', time: '2 小时前' },
  { id: 5, icon: '✗', action: 'KO_REJECT',   target: 'KO-DOC-0076',  user: '王五（合规审核员）', time: '3 小时前' },
  { id: 6, icon: '⊕', action: 'KO_CREATE',   target: 'KO-CON-0018',  user: '张三（算法工程师）', time: '昨天' },
  { id: 7, icon: '✓', action: 'KO_PUBLISH',   target: 'KO-PAR-0092',  user: '李雷（系统管理员）', time: '昨天' },
  { id: 8, icon: '⊕', action: 'KO_IMPORT',    target: 'batch 24 个',  user: '李雷（系统管理员）', time: '2 天前' },
  { id: 9, icon: '⏱', action: 'KO_REVIEW',   target: 'KO-RUL-0046',  user: '王五（合规审核员）', time: '2 天前' },
  { id: 10, icon: '⊕', action: 'KO_CREATE',  target: 'KO-SCH-0041',  user: '韩梅梅（业务专家）', time: '3 天前' }
]

// V3 风格 ASCII 趋势图（mock 数据，T212+ 后端齐全后接 echarts）
const trendAscii = computed(() => {
  return `Day  -6  -5  -4  -3  -2  -1  Today
CON  ▁▁  ▂▃  ▃▂  ▂▁  ▁▁  ▁  ▃▂
RUL  ▁▁  ▁▁  ▂▁  ▃▂  ▂▂  ▁  ▂▃
PAR  ▂▂  ▃▃  ▃▃  ▄▄  ▃▃  ▂  ▄▄
SCH  ▁▁  ▂▁  ▁▁  ▂▁  ▁▁  ▁  ▂▁
PRM  ·   ·   ·   ·   ·   ·   ·
DOC  ▃▃  ▄▃  ▃▃  ▃▄  ▃▃  ▂  ▄▄`
})

function onExport() {
  ElMessage.success('导出报告：U9 实施时生成完整报告（T212+ TODO）')
}

function onRefresh() {
  ElMessage.info('数据已刷新（mock，T212+ 接真实 API）')
}

function onProjectClick(code: string) {
  router.push({ name: 'project-mgmt' })
}

function onActivityClick(id: number) {
  ElMessage.info('活动详情：U9 审计日志实施时跳详情页（T214 TODO）')
}
</script>

<style lang="scss" scoped>
/* === F-53.2.2 DashboardView V3 完整还原（第二批差距修复） === */

.dashboard {
  /* V3: padding: 16px 24px（sidebar 留出） */
  padding: 16px 24px;
}

/* === V3 原型 .stat-card 关键还原（不依赖全局 .stat-card 工具类差异） === */

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 16px;  /* V3 原值 */
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-radius: 2px;

  /* V3 原型用 ::before 伪元素做 3px left border（更精准） */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 3px;
    height: 100%;
    background: var(--port-blue);
  }
  &.success::before { background: var(--signal-green); }
  &.warn::before { background: var(--signal-orange); }
  &.danger::before { background: var(--signal-red); }

  .label {
    font-size: 11px;  /* V3 原值 */
    color: var(--text-secondary);
    letter-spacing: 0.04em;  /* V3 原值 */
  }
  .value {
    font-family: 'JetBrains Mono', monospace;
    font-size: 28px;  /* V3 原值 */
    font-weight: 700;
    color: var(--text-primary);
    margin: 6px 0 4px;  /* V3 原值 */
  }
  .delta {
    font-family: 'JetBrains Mono', monospace;
    font-size: 11px;
    color: var(--signal-green);
    &.down { color: var(--signal-red); }
  }
  .breakdown {
    font-size: 10px;
    color: var(--text-tertiary);
    margin-top: 6px;
    border-top: 1px dashed var(--line);  /* V3 关键：虚线分隔 */
    padding-top: 6px;
    font-family: 'JetBrains Mono', monospace;
  }
}

/* V3 dashboard-row 布局：2fr 1fr（项目活动 2 份 + 最近活动 1 份） */
.dashboard-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;

  /* 响应式：窄屏自动堆叠 */
  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

/* V3 4 个 stat-card 容器：3 列等宽 grid（不依赖 el-row 栅格） */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* V3 容器（项目活动 / 最近活动 / 趋势） */
.card-section {
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 16px 20px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  display: flex;
  align-items: baseline;
  gap: 8px;

  .section-meta {
    font-family: 'JetBrains Mono', monospace;
    font-size: 10px;
    font-weight: 400;
    color: var(--text-tertiary);
  }
}

/* V3 项目活动列表（V3 原型用 .lst-item，已继承全局） */
.lst-list {
  display: flex;
  flex-direction: column;
  gap: 0;  /* V3 list 之间用 border-bottom 分隔，无 gap */
}

// 状态颜色
.status-active {
  background: rgba(47, 133, 90, 0.1);
  color: var(--signal-green);
}

.status-archived {
  background: rgba(140, 140, 140, 0.1);
  color: var(--text-tertiary);
}

// 项目条目扩展
.lst-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.lst-item__code {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: var(--port-blue);
  font-weight: 600;
}

.lst-item__status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  font-weight: 600;
}

.lst-item__title {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 4px;
}

.lst-item__meta {
  font-size: 11px;
  color: var(--text-tertiary);

  .mono {
    color: var(--text-secondary);
  }
}

// 活动条目（带图标）
.lst-item--activity {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.lst-item__icon {
  width: 24px;
  height: 24px;
  border-radius: 2px;
  background: var(--bg-rail);
  color: var(--text-on-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
}

.lst-item__body {
  flex: 1;
  min-width: 0;
}

// 趋势占位
.trend-placeholder {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trend-ascii {
  background: var(--bg-canvas);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 12px 16px;
  overflow-x: auto;

  pre {
    font-family: 'JetBrains Mono', monospace;
    font-size: 11px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin: 0;
    white-space: pre;
  }
}

.trend-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 11px;
  color: var(--text-secondary);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--port-blue);
}

.dot-con { background: var(--port-blue); }
.dot-rul { background: var(--steel); }
.dot-par { background: var(--signal-orange); }
.dot-sch { background: var(--signal-yellow); }
.dot-prm { background: var(--signal-green); }
.dot-doc { background: var(--text-tertiary); }
</style>