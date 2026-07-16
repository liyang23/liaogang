<!--
  DictMgmtView V3 风格完整还原（F-53.2 T215）
  V3 原型：6 字典卡片 + 字典项编辑
-->
<template>
  <div class="dict-mgmt">
    <div class="page-header">
      <div class="title-row">
        <h1>
          字典管理
          <span class="id">// DICT · DICTIONARY</span>
        </h1>
        <div class="right-actions">
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="搜索字典 / 字典项..."
          />
          <button class="btn" @click="onExport">
            <span>⤓</span> 导出
          </button>
          <button class="btn btn-primary" @click="onCreate">
            <span>+</span> 新建字典
          </button>
        </div>
      </div>
      <p class="subtitle">6 个字典（类型介绍 / 效力分级 / 权威分级 / 类型分组 / 知识对象概念 / 量纲配置）· V9001 seed 已初始化</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">字典总数</div>
        <div class="value">6</div>
        <div class="delta">+0</div>
        <div class="breakdown">V9001 seed 初始化</div>
      </div>
      <div class="stat-card success">
        <div class="label">字典项总数</div>
        <div class="value">87</div>
        <div class="delta">+3</div>
        <div class="breakdown">近 7 天新增</div>
      </div>
      <div class="stat-card">
        <div class="label">关联 KO 数</div>
        <div class="value">23</div>
        <div class="delta">+1</div>
        <div class="breakdown">使用字典的 KO</div>
      </div>
      <div class="stat-card warn">
        <div class="label">待审核</div>
        <div class="value">2</div>
        <div class="delta">+0</div>
        <div class="breakdown">需复核的项</div>
      </div>
    </div>

    <div class="dict-grid">
      <div
        v-for="dict in filteredDicts"
        :key="dict.code"
        class="dict-card"
        @click="onDictClick(dict.code)"
      >
        <div class="dict-card__header">
          <div class="dict-card__code">{{ dict.code }}</div>
          <div class="dict-card__count">{{ dict.itemCount }} 项</div>
        </div>
        <div class="dict-card__title">{{ dict.name }}</div>
        <div class="dict-card__desc">{{ dict.description }}</div>
        <div class="dict-card__meta">
          关联 KO <span class="mono">{{ dict.linkedKo }}</span> · 最近修改
          <span class="mono">{{ dict.lastModified }}</span>
        </div>
        <div class="dict-card__samples">
          <span
            v-for="(sample, i) in dict.samples"
            :key="i"
            class="dict-card__sample"
          >{{ sample }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Dict {
  code: string
  name: string
  description: string
  itemCount: number
  linkedKo: number
  lastModified: string
  samples: string[]
}

const searchQuery = ref('')

const dicts: Dict[] = [
  { code: 'DICT-TYPE-INTRO', name: '类型介绍', description: '6 种 KO 类型的介绍和适用场景说明', itemCount: 12, linkedKo: 0, lastModified: '2026-06-15', samples: ['CON 约束', 'RUL 规则', 'PAR 参数'] },
  { code: 'DICT-EFFECT', name: '效力分级', description: '硬约束 / 软约束 / 规范性 3 级效力', itemCount: 3, linkedKo: 8, lastModified: '2026-06-15', samples: ['Hard', 'Soft', 'Normative'] },
  { code: 'DICT-AUTHORITY', name: '权威分级', description: '行业标准 / 企业标准 / 部门规范 3 级权威', itemCount: 3, linkedKo: 5, lastModified: '2026-06-15', samples: ['行业', '企业', '部门'] },
  { code: 'DICT-TYPE-GROUP', name: '类型分组', description: '主功能 / 治理 / 配置 3 组分类', itemCount: 3, linkedKo: 0, lastModified: '2026-06-15', samples: ['主功能', '治理', '配置'] },
  { code: 'DICT-KO-CONCEPT', name: '知识对象概念', description: 'KO 抽象概念定义（结构 / 行为 / 关系）', itemCount: 9, linkedKo: 0, lastModified: '2026-06-15', samples: ['结构', '行为', '关系'] },
  { code: 'DICT-UNIT-CONFIG', name: '量纲配置', description: '9 种量纲（%/元/TEU/h/条/辆/岸桥/栏/人/台/箱/h）', itemCount: 9, linkedKo: 10, lastModified: '2026-06-15', samples: ['%', '元/TEU', 'h'] }
]

const filteredDicts = computed(() => {
  if (!searchQuery.value) return dicts
  const q = searchQuery.value.toLowerCase()
  return dicts.filter(d =>
    d.code.toLowerCase().includes(q) ||
    d.name.toLowerCase().includes(q) ||
    d.description.toLowerCase().includes(q)
  )
})

function onExport() {
  ElMessage.success('导出：U9 实施时生成完整导出')
}

function onCreate() {
  ElMessage.info('新建字典：U9 实施时完整 CRUD')
}

function onDictClick(code: string) {
  ElMessage.info(`详情：${code}（U9 实施时跳详情页）`)
}
</script>

<style lang="scss" scoped>
.dict-mgmt { padding: 16px 24px; }

.search-input {
  width: 280px; height: 28px; padding: 0 8px;
  background: var(--bg-paper); border: 1px solid var(--line);
  border-radius: 2px; color: var(--text-primary);
  font-size: 13px; margin-right: 4px;
}

.dict-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}

.dict-card {
  background: var(--bg-paper); border: 1px solid var(--line);
  border-left: 3px solid var(--port-blue); border-radius: 2px;
  padding: 12px 16px; cursor: pointer; transition: all 0.15s;
  &:hover { border-left-color: var(--port-blue-light); transform: translateX(2px); }
}

.dict-card__header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;
}

.dict-card__code {
  font-family: 'JetBrains Mono', monospace; font-size: 11px;
  color: var(--port-blue); font-weight: 600;
}

.dict-card__count {
  font-family: 'JetBrains Mono', monospace; font-size: 10px; color: var(--text-tertiary);
}

.dict-card__title { font-size: 14px; font-weight: 500; color: var(--text-primary); margin-bottom: 4px; }

.dict-card__desc { font-size: 11px; color: var(--text-secondary); margin-bottom: 6px; line-height: 1.4; }

.dict-card__meta { font-size: 10px; color: var(--text-tertiary); margin-bottom: 8px;
  .mono { color: var(--text-secondary); }
}

.dict-card__samples { display: flex; flex-wrap: wrap; gap: 4px; }

.dict-card__sample {
  font-family: 'JetBrains Mono', monospace; font-size: 10px;
  background: var(--bg-canvas); border: 1px solid var(--line);
  padding: 1px 6px; border-radius: 2px; color: var(--text-secondary);
}
</style>