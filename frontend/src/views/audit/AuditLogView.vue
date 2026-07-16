<!--
  AuditLogView V3 风格完整还原（F-53.2 T214）
  V3 原型 3 重暴露（OQ-11）：hover tooltip + 点击弹窗 + CSV 导出
-->
<template>
  <div class="audit-log">
    <div class="page-header">
      <div class="title-row">
        <h1>
          审计日志
          <span class="id">// AUDIT · SYSTEM LOG</span>
        </h1>
        <div class="right-actions">
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="搜索 user / action / target / id..."
          />
          <button class="btn" @click="onExport">
            <span>⤓</span> 导出 CSV
          </button>
        </div>
      </div>
      <p class="subtitle">全平台操作审计 · 12 个月保留（FR-26 验收 NFR-15）· 含 KO_CREATE / KO_UPDATE / KO_REVIEW / KO_PUBLISH / ROLE_CHANGE / LOGIN 6 类操作</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">今日审计</div>
        <div class="value">87</div>
        <div class="delta">+12 较昨日</div>
        <div class="breakdown">USER_ROLE_LOGIN 23 + KO_CREATE 8 + ...</div>
      </div>
      <div class="stat-card warn">
        <div class="label">高风险操作</div>
        <div class="value">3</div>
        <div class="delta">+1 今日</div>
        <div class="breakdown">USER_ROLE_CHANGE 2 + KO_DELETE 1</div>
      </div>
      <div class="stat-card success">
        <div class="label">用户操作</div>
        <div class="value">5</div>
        <div class="delta">活跃用户</div>
        <div class="breakdown">近 24h</div>
      </div>
      <div class="stat-card">
        <div class="label">总记录数</div>
        <div class="value">14,328</div>
        <div class="delta">+87 今日</div>
        <div class="breakdown">12 个月累计</div>
      </div>
    </div>

    <div class="card-section">
      <h3 class="section-title">审计记录 <span class="section-meta">最近 50 条 · hover ID 看完整 hash · 点击看详情</span></h3>
      <table class="audit-table">
        <thead>
          <tr>
            <th class="col-id">ID（hover 看完整）</th>
            <th>操作</th>
            <th>目标 KO</th>
            <th>详情</th>
            <th>操作人</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="log in logs"
            :key="log.id"
            class="audit-row"
            @click="onLogClick(log.id)"
          >
            <td class="col-id">
              <span class="audit-id-short" :title="log.id">{{ log.idShort }}</span>
            </td>
            <td>
              <span class="action-tag" :class="`action-${log.actionClass}`">
                {{ log.action }}
              </span>
            </td>
            <td class="mono">{{ log.target }}</td>
            <td class="detail-cell">{{ log.detail }}</td>
            <td>{{ log.user }}</td>
            <td class="mono time-cell">{{ log.time }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

interface AuditLog {
  id: string
  idShort: string
  action: string
  actionClass: 'ko' | 'role' | 'login' | 'system'
  target: string
  detail: string
  user: string
  time: string
}

const searchQuery = ref('')

const logs: AuditLog[] = [
  { id: 'AUDIT-20260715-000123', idShort: 'AUDIT-...', action: 'KO_PUBLISH', actionClass: 'ko', target: 'KO-CON-0019', detail: '发布版本 v2.0.0，状态 Active', user: '李雷', time: '2 分钟前' },
  { id: 'AUDIT-20260715-000122', idShort: 'AUDIT-...', action: 'KO_UPDATE', actionClass: 'ko', target: 'KO-RUL-0047', detail: '修改规则 4 处', user: '韩梅梅', time: '15 分钟前' },
  { id: 'AUDIT-20260715-000121', idShort: 'AUDIT-...', action: 'KO_CREATE', actionClass: 'ko', target: 'KO-PAR-0093', detail: '新增参数「装卸效率系数」', user: '张三', time: '1 小时前' },
  { id: 'AUDIT-20260715-000120', idShort: 'AUDIT-...', action: 'KO_REVIEW', actionClass: 'ko', target: 'KO-PRM-0003', detail: '提交审核：合规性 95%', user: '王五', time: '2 小时前' },
  { id: 'AUDIT-20260715-000119', idShort: 'AUDIT-...', action: 'KO_REJECT', actionClass: 'ko', target: 'KO-DOC-0076', detail: '驳回原因：格式不符', user: '王五', time: '3 小时前' },
  { id: 'AUDIT-20260715-000118', idShort: 'AUDIT-...', action: 'ROLE_CHANGE', actionClass: 'role', target: 'ROLE-0005', detail: '分配给 user-002，下次登录生效', user: '李雷', time: '昨天' },
  { id: 'AUDIT-20260715-000117', idShort: 'AUDIT-...', action: 'LOGIN', actionClass: 'login', target: '—', detail: 'ROLE-0001 系统管理员', user: '李雷', time: '昨天 18:45' },
  { id: 'AUDIT-20260715-000116', idShort: 'AUDIT-...', action: 'KO_IMPORT', actionClass: 'ko', target: 'batch 24', detail: '导入 24 个 KO', user: '李雷', time: '2 天前' }
]

const filteredLogs = computed(() => {
  if (!searchQuery.value) return logs
  const q = searchQuery.value.toLowerCase()
  return logs.filter(l =>
    l.id.toLowerCase().includes(q) ||
    l.action.toLowerCase().includes(q) ||
    l.target.toLowerCase().includes(q) ||
    l.user.toLowerCase().includes(q)
  )
})

function onExport() {
  ElMessage.success('CSV 导出：U9 实施时生成完整导出')
}

function onLogClick(id: string) {
  ElMessage.info(`详情：ID ${id}（OQ-11 弹窗实施时显示完整元数据）`)
}
</script>

<style lang="scss" scoped>
.audit-log {
  padding: 16px 24px;
}

.search-input {
  width: 280px;
  height: 28px;
  padding: 0 8px;
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-radius: 2px;
  color: var(--text-primary);
  font-size: 13px;
  margin-right: 4px;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;

  thead {
    background: var(--bg-rail);
    color: var(--text-on-dark);
    th {
      padding: 8px 12px;
      text-align: left;
      font-weight: 600;
      font-size: 11px;
      letter-spacing: 0.04em;
    }
  }

  tbody {
    tr.audit-row {
      border-bottom: 1px solid var(--bg-grid);
      cursor: pointer;
      transition: background 0.15s;
      &:hover { background: rgba(15, 76, 117, 0.04); }
      &:last-child { border-bottom: none; }
    }
    td { padding: 8px 12px; vertical-align: top; }
  }
}

.audit-id-short {
  font-family: 'JetBrains Mono', monospace;
  color: var(--port-blue);
  font-weight: 600;
  cursor: help;
}

.action-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 2px;
  font-size: 10px;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
}

.action-ko { background: rgba(15, 76, 117, 0.1); color: var(--port-blue); }
.action-role { background: rgba(237, 137, 54, 0.1); color: var(--signal-orange-deep); }
.action-login { background: rgba(47, 133, 90, 0.1); color: var(--signal-green); }
.action-system { background: rgba(138, 146, 160, 0.1); color: var(--text-tertiary); }

.detail-cell {
  color: var(--text-secondary);
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  color: var(--text-tertiary);
  font-size: 11px;
  white-space: nowrap;
}
</style>