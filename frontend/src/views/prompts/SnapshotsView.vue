<!--
  SnapshotsView V3 风格完整还原（F-53.2 T218）
  V3 原型：PRP 快照列表（版本号 + 时间 + 装配数 + 3 重暴露）
-->
<template>
  <div class="snapshots">
    <div class="page-header">
      <div class="title-row">
        <h1>PRP 快照 <span class="id">// PRP · PROMPT SNAPSHOTS</span></h1>
        <div class="right-actions">
          <button class="btn" @click="onExport"><span>⤓</span> 导出</button>
          <button class="btn btn-primary" @click="onCreate"><span>+</span> 新建快照</button>
        </div>
      </div>
      <p class="subtitle">PRP 组装快照（U8 实施时）· 每次 PRM 模板组装保存为快照 · 含完整 Section 内容 + 变量绑定 + 装配数</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">快照总数</div><div class="value">12</div><div class="delta">+3</div><div class="breakdown">V8 实施时累计</div></div>
      <div class="stat-card success"><div class="label">今日新增</div><div class="value">2</div><div class="delta">+2</div><div class="breakdown">PRM-0001 反复调试</div></div>
      <div class="stat-card"><div class="label">关联 PRM</div><div class="value">3</div><div class="delta">+0</div><div class="breakdown">3 个预置模板</div></div>
      <div class="stat-card warn"><div class="label">总装配数</div><div class="value">147</div><div class="delta">+24</div><div class="breakdown">所有快照累计（OQ-16 动态）</div></div>
    </div>

    <div class="card-section">
      <h3 class="section-title">快照列表 <span class="section-meta">12 条 · 按时间倒序 · hover ID 看完整 hash</span></h3>
      <ul class="snapshot-list">
        <li
          v-for="snapshot in snapshots"
          :key="snapshot.id"
          class="snapshot-item"
          @click="onSnapshotClick(snapshot.id)"
        >
          <div class="snapshot-item__id-col">
            <div class="snapshot-id-short" :title="snapshot.id">{{ snapshot.idShort }}</div>
            <div class="snapshot-id-version">{{ snapshot.version }}</div>
          </div>
          <div class="snapshot-item__main">
            <div class="snapshot-item__header">
              <span class="prm-tag mono">{{ snapshot.prmCode }}</span>
              <span class="separator">·</span>
              <span class="snapshot-name">{{ snapshot.name }}</span>
            </div>
            <div class="snapshot-item__meta">
              <span class="meta-item"><span class="meta-label">装配数</span><span class="meta-value mono">{{ snapshot.assemblyCount }}</span></span>
              <span class="meta-item"><span class="meta-label">字符</span><span class="meta-value mono">{{ snapshot.charCount }}</span></span>
              <span class="meta-item"><span class="meta-label">tokens</span><span class="meta-value mono">{{ snapshot.tokenCount }}</span></span>
              <span class="meta-item"><span class="meta-label">变量</span><span class="meta-value mono">{{ snapshot.varCount }}</span></span>
              <span class="meta-item"><span class="meta-label">KO</span><span class="meta-value mono">{{ snapshot.koCount }}</span></span>
              <span class="meta-item time mono">{{ snapshot.time }}</span>
            </div>
          </div>
          <div class="snapshot-item__actions">
            <button class="btn">查看</button>
            <button class="btn btn-primary">还原</button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

interface Snapshot {
  id: string; idShort: string; version: string; prmCode: string
  name: string; assemblyCount: number; charCount: number
  tokenCount: number; varCount: number; koCount: number; time: string
}

const snapshots: Snapshot[] = [
  { id: 'SNAP-20260715-000023', idShort: 'SNAP-...', version: 'v3.2', prmCode: 'KO-PRM-0001', name: '大窑湾 PRM 第 3 轮调试', assemblyCount: 12, charCount: 2847, tokenCount: 1424, varCount: 6, koCount: 5, time: '30 分钟前' },
  { id: 'SNAP-20260715-000022', idShort: 'SNAP-...', version: 'v3.1', prmCode: 'KO-PRM-0001', name: '大窑湾 PRM 调整变量顺序', assemblyCount: 11, charCount: 2730, tokenCount: 1365, varCount: 6, koCount: 4, time: '1 小时前' },
  { id: 'SNAP-20260715-000021', idShort: 'SNAP-...', version: 'v3.0', prmCode: 'KO-PRM-0001', name: '大窑湾 PRM 首版', assemblyCount: 9, charCount: 2456, tokenCount: 1228, varCount: 5, koCount: 3, time: '今天上午' },
  { id: 'SNAP-20260714-000020', idShort: 'SNAP-...', version: 'v1.2', prmCode: 'KO-PRM-0002', name: '堆场 PRM 含 3 个 SCH 引用', assemblyCount: 5, charCount: 1234, tokenCount: 617, varCount: 2, koCount: 3, time: '昨天' },
  { id: 'SNAP-20260714-000019', idShort: 'SNAP-...', version: 'v1.1', prmCode: 'KO-PRM-0002', name: '堆场 PRM 添加翻箱率', assemblyCount: 4, charCount: 1100, tokenCount: 550, varCount: 2, koCount: 2, time: '昨天' },
  { id: 'SNAP-20260713-000018', idShort: 'SNAP-...', version: 'v1.0', prmCode: 'KO-PRM-0002', name: '堆场 PRM 初始版', assemblyCount: 3, charCount: 800, tokenCount: 400, varCount: 1, koCount: 2, time: '2 天前' },
  { id: 'SNAP-20260712-000017', idShort: 'SNAP-...', version: 'v1.3', prmCode: 'KO-PRM-0003', name: '泊位 DRL 训练快照', assemblyCount: 7, charCount: 1890, tokenCount: 945, varCount: 3, koCount: 4, time: '3 天前' },
  { id: 'SNAP-20260712-000016', idShort: 'SNAP-...', version: 'v1.2', prmCode: 'KO-PRM-0003', name: '泊位 DRL 调整奖励', assemblyCount: 6, charCount: 1700, tokenCount: 850, varCount: 3, koCount: 3, time: '3 天前' },
  { id: 'SNAP-20260710-000015', idShort: 'SNAP-...', version: 'v1.1', prmCode: 'KO-PRM-0003', name: '泊位 DRL 迭代', assemblyCount: 5, charCount: 1500, tokenCount: 750, varCount: 2, koCount: 3, time: '5 天前' },
  { id: 'SNAP-20260710-000014', idShort: 'SNAP-...', version: 'v1.0', prmCode: 'KO-PRM-0003', name: '泊位 DRL 初始版', assemblyCount: 4, charCount: 1200, tokenCount: 600, varCount: 2, koCount: 2, time: '5 天前' }
]

function onExport() { ElMessage.success('导出：U8 实施时完整导出') }
function onCreate() { ElMessage.info('新建快照：U8 实施时完整 CRUD') }
function onSnapshotClick(id: string) { ElMessage.info(`详情：${id}（U8 实施时跳详情 + 还原）`) }
</script>

<style lang="scss" scoped>
.snapshots { padding: 16px 24px; }

.snapshot-list { list-style: none; margin: 0; padding: 0; }

.snapshot-item {
  padding: 10px 14px; border-bottom: 1px solid var(--bg-grid);
  display: flex; gap: 12px; align-items: flex-start; cursor: pointer; transition: background 0.15s;
  &:hover { background: rgba(15, 76, 117, 0.04); }
  &:last-child { border-bottom: none; }
}

.snapshot-item__id-col { display: flex; flex-direction: column; gap: 2px; min-width: 100px; flex-shrink: 0; }

.snapshot-id-short { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--port-blue); font-weight: 600; cursor: help; }
.snapshot-id-version { font-family: 'JetBrains Mono', monospace; font-size: 9px; color: var(--text-tertiary); }

.snapshot-item__main { flex: 1; min-width: 0; }

.snapshot-item__header { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }

.prm-tag { background: var(--bg-rail); color: var(--text-on-dark); padding: 1px 6px; border-radius: 2px; font-size: 10px; font-weight: 600; }
.separator { color: var(--text-tertiary); }
.snapshot-name { font-size: 12px; color: var(--text-primary); font-weight: 500; }

.snapshot-item__meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 10px; color: var(--text-tertiary); }

.meta-item { display: flex; gap: 4px; }
.meta-label { color: var(--text-tertiary); }
.meta-value { color: var(--text-secondary); &.mono { color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; } &.time { color: var(--text-tertiary); margin-left: auto; } }

.snapshot-item__actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }
</style>