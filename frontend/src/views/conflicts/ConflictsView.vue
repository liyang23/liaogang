<!--
  ConflictsView V3 风格完整还原（F-53.2 T213）
  V3 原型区块：
  - .page-header（标题 + 副标题 + 6 类型冲突筛选 + 自动建议按钮）
  - .alert-item 列表（3 重暴露：图标 + 标题 + 元数据 + 详情按钮）
  - 6 类型冲突 C1-C6 + H1-H6 标签 + 优先级
-->
<template>
  <div class="conflicts">
    <div class="page-header">
      <div class="title-row">
        <h1>
          知识治理
          <span class="id">// CONFLICTS · KNOWLEDGE GOVERNANCE</span>
        </h1>
        <div class="right-actions">
          <select class="filter-select">
            <option value="all">全部类型</option>
            <option value="C1">C1 字段冲突</option>
            <option value="C2">C2 范围冲突</option>
            <option value="C3">C3 引用冲突</option>
            <option value="C4">C4 优先级冲突</option>
            <option value="C5">C5 业务冲突</option>
            <option value="C6">C6 时序冲突</option>
          </select>
          <button class="btn" @click="onExport">
            <span>⤓</span> 导出报告
          </button>
          <button class="btn btn-primary" @click="onAutoResolve">
            <span>✦</span> 自动建议（U7 LLM）
          </button>
        </div>
      </div>
      <p class="subtitle">6 种 C 类冲突 + H 类健康检查 · 实时检测 + LLM 主动建议（OQ-9 接入 DeepSeek v4）· 共 7 项待处置</p>
    </div>

    <!-- 顶部 4 个 stat-card 汇总 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">待处置冲突</div>
        <div class="value">7</div>
        <div class="delta">+2 本周</div>
        <div class="breakdown">C 类 5 + H 类 2</div>
      </div>
      <div class="stat-card warn">
        <div class="label">高优先级</div>
        <div class="value">3</div>
        <div class="delta">+1 本周</div>
        <div class="breakdown">C1 字段 × 2 + C4 优先级 × 1</div>
      </div>
      <div class="stat-card success">
        <div class="label">本周已解决</div>
        <div class="value">4</div>
        <div class="delta up">+4 本周</div>
        <div class="breakdown">通过 LLM 仲裁快路径</div>
      </div>
      <div class="stat-card">
        <div class="label">历史累计</div>
        <div class="value">23</div>
        <div class="delta">+7 本周</div>
        <div class="breakdown">过去 30 天</div>
      </div>
    </div>

    <!-- 冲突列表 -->
    <div class="card-section">
      <h3 class="section-title">
        冲突列表
        <span class="section-meta">{{ conflicts.length }} 项 · 按优先级降序</span>
      </h3>
      <ul class="alert-list">
        <li
          v-for="conflict in conflicts"
          :key="conflict.id"
          class="alert-item"
          :class="conflict.priorityClass"
        >
          <div class="alert-icon">{{ conflict.priorityLabel }}</div>
          <div class="alert-body">
            <div class="alert-title">
              <span class="mono">{{ conflict.code }}</span>
              ·
              {{ conflict.typeLabel }}
              ·
              {{ conflict.title }}
            </div>
            <div class="alert-meta">
              <span class="mono">{{ conflict.koA }}</span>
              <span> ↔ </span>
              <span class="mono">{{ conflict.koB }}</span>
              <span> · 优先级 {{ conflict.priority }} · {{ conflict.time }}</span>
            </div>
            <div class="alert-desc">{{ conflict.description }}</div>
            <div v-if="conflict.suggestion" class="alert-suggestion">
              <span class="suggestion-label">⚦ LLM 建议</span>
              {{ conflict.suggestion }}
            </div>
          </div>
          <div class="alert-actions">
            <button class="btn">查看详情</button>
            <button class="btn btn-primary">采纳建议</button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

interface Conflict {
  id: number
  code: string
  typeLabel: string
  title: string
  koA: string
  koB: string
  priority: number
  priorityClass: 'priority-high' | 'priority-mid' | 'priority-low'
  priorityLabel: string
  time: string
  description: string
  suggestion?: string
}

const conflicts: Conflict[] = [
  {
    id: 1,
    code: 'C1-0001',
    typeLabel: 'C1 字段冲突',
    title: '集装箱容量定义不一致',
    koA: 'KO-CON-0019',
    koB: 'KO-CON-0020',
    priority: 1,
    priorityClass: 'priority-high',
    priorityLabel: 'P1',
    time: '10 分钟前',
    description: 'KO-CON-0019 定义 TEU = 20ft 标准箱，KO-CON-0020 定义 TEU = 22.5 立方米。同一字段（容量）不同单位。',
    suggestion: '建议统一为 20ft 标准箱（行业惯例），KO-CON-0020 添加单位换算系数。采纳此方案需 OQ-12 决策记录。'
  },
  {
    id: 2,
    code: 'C4-0015',
    typeLabel: 'C4 优先级冲突',
    title: '约束优先级与规则冲突',
    koA: 'KO-RUL-0033',
    koB: 'KO-RUL-0041',
    priority: 1,
    priorityClass: 'priority-high',
    priorityLabel: 'P1',
    time: '1 小时前',
    description: 'KO-RUL-0033（硬约束 优先级=10）vs KO-RUL-0041（软约束 优先级=5）：同一调度场景下，软约束反而覆盖硬约束。',
    suggestion: '建议 RUL-0041 的优先级提升到 15，确保硬约束不被覆盖；或 RUL-0033 的优先级调整到 8。'
  },
  {
    id: 3,
    code: 'C2-0008',
    typeLabel: 'C2 范围冲突',
    title: '堆场范围定义重叠',
    koA: 'KO-PAR-0073',
    koB: 'KO-PAR-0081',
    priority: 2,
    priorityClass: 'priority-high',
    priorityLabel: 'P2',
    time: '3 小时前',
    description: 'KO-PAR-0073（大窑湾堆场 1-50 号位）与 KO-PAR-0081（大窑湾堆场 30-80 号位）：范围 30-50 双重定义。',
    suggestion: '建议统一为 KO-PAR-0073（1-50）并删除 KO-PAR-0081，或调整 KO-PAR-0081 为 51-80 避免重叠。'
  },
  {
    id: 4,
    code: 'H2-0002',
    typeLabel: 'H2 健康检查',
    title: 'PRM 模板超时未审',
    koA: 'KO-PRM-0003',
    koB: '—',
    priority: 1,
    priorityClass: 'priority-high',
    priorityLabel: '⚠',
    time: '昨天',
    description: 'KO-PRM-0003 状态停留在 DRAFT 已超过 7 天未提交审核（SLA 违规）。',
    suggestion: '建议联系作者 张三（算法工程师）确认进度，或由 系统管理员 自动提交。'
  },
  {
    id: 5,
    code: 'C3-0012',
    typeLabel: 'C3 引用冲突',
    title: 'PRM 引用已删除的 PAR',
    koA: 'KO-PRM-0001',
    koB: 'KO-PAR-0042',
    priority: 2,
    priorityClass: 'priority-mid',
    priorityLabel: 'P2',
    time: '2 天前',
    description: 'KO-PRM-0001 Section 3 引用 KO-PAR-0042（船型系数），但 PAR-0042 已软删除。',
    suggestion: '建议更新 PRM-0001 引用最新可用 PAR（如 KO-PAR-0041）或恢复 KO-PAR-0042。'
  },
  {
    id: 6,
    code: 'H1-0007',
    typeLabel: 'H1 健康检查',
    title: 'KO 跨项目引用',
    koA: 'KO-CON-0014',
    koB: 'KO-CON-0005',
    priority: 3,
    priorityClass: 'priority-low',
    priorityLabel: '⚠',
    time: '3 天前',
    description: 'KO-CON-0014（PROJ-0001 大窑湾）引用 KO-CON-0005（PROJ-0002 堆场），跨项目引用需确认。',
    suggestion: '建议将 KO-CON-0005 提取为公共 KO（独立项目），或修改 KO-CON-0014 移除跨项目引用。'
  },
  {
    id: 7,
    code: 'C5-0003',
    typeLabel: 'C5 业务冲突',
    title: '作业时间窗冲突',
    koA: 'KO-RUL-0022',
    koB: 'KO-RUL-0028',
    priority: 3,
    priorityClass: 'priority-low',
    priorityLabel: 'P3',
    time: '5 天前',
    description: 'KO-RUL-0022（白天作业 8-18 点）vs KO-RUL-0028（夜间作业 22-6 点）：时间窗覆盖关系不清。',
    suggestion: '建议明确优先级 + 冲突解决策略，或拆分为独立场景。'
  }
]

function onExport() {
  ElMessage.success('导出报告：U9 实施时生成完整报告')
}

function onAutoResolve() {
  ElMessage.info('自动建议（U7 LLM 接入 DeepSeek v4 实施时）')
}
</script>

<style lang="scss" scoped>
.conflicts {
  padding: 16px 24px;
}

// 筛选 select（V3 工具栏内）
.filter-select {
  height: 28px;
  padding: 0 8px;
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-radius: 2px;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
  margin-right: 4px;
}

// alert 列表（V3 原型 ul + li + 1px border-bottom 分隔）
.alert-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.alert-item {
  padding: 10px 14px;
  border-bottom: 1px solid var(--bg-grid);
  display: flex;
  gap: 10px;
  align-items: flex-start;
  font-size: 12px;

  &:last-child { border-bottom: none; }
}

// 优先级图标方块
.alert-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  background: var(--signal-red);
  color: var(--bg-paper);
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.priority-high .alert-icon { background: var(--signal-red); }
.priority-mid .alert-icon { background: var(--signal-orange); }
.priority-low .alert-icon { background: var(--signal-yellow); color: var(--text-primary); }

.alert-body { flex: 1; min-width: 0; }

.alert-title {
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 2px;

  .mono { color: var(--port-blue); font-weight: 600; }
}

.alert-meta {
  color: var(--text-tertiary);
  font-size: 10px;
  font-family: 'JetBrains Mono', monospace;
  margin-bottom: 4px;
}

.alert-desc {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
}

.alert-suggestion {
  background: rgba(15, 76, 117, 0.05);
  border-left: 2px solid var(--port-blue);
  padding: 6px 8px;
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-primary);

  .suggestion-label {
    font-weight: 600;
    color: var(--port-blue);
    margin-right: 4px;
  }
}

.alert-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}
</style>