<!--
  KO 库全景概览页（T203 + F-53 V3 视觉还原）
  6 类型入口卡片（CON / RUL / PAR / SCH / PRM / DOC）
  + 跨类搜索框（OQ-4：title + id + typeName）
  + 4 个统计卡片（V3 stat-card 风格：6 类型 KO 数量 + 总数 + 项目数 + 今日新增）
-->
<template>
  <div class="ko-library">
    <!-- V3 page-header：深色文字 h1 + 副标题 + 右侧动作按钮组 -->
    <div class="page-header">
      <div class="title-row">
        <h1>
          知识对象库
          <span class="id">// KO · KNOWLEDGE OBJECT</span>
        </h1>
        <div class="right-actions">
          <button class="btn" @click="goImport">
            <span>⤓</span> 导入
          </button>
          <button class="btn" @click="goDownloadTemplate">
            <span>⤓</span> 下载导入模版
          </button>
          <button class="btn btn-primary" @click="onCreate">
            <span>+</span> 新建 KO
          </button>
        </div>
      </div>
      <p class="subtitle">6 种类型（CON / RUL / PAR / SCH / PRM / DOC）· 跨类搜索 + 状态机 + 跨项目隔离 · 共 278 个知识对象（V9001 seed）</p>
    </div>

    <!-- V3 4 个 stat-card 统计（CSS Grid 4 列） -->
    <div class="stat-grid">
      <div class="stat-card success">
        <div class="stat-label">KO 总数</div>
        <div class="stat-value">278</div>
        <div class="stat-meta">+12 本周新增</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">项目数</div>
        <div class="stat-value">4</div>
        <div class="stat-meta">3 活动 + 1 归档</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-label">待审核</div>
        <div class="stat-value">7</div>
        <div class="stat-meta">Draft / Review 中</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-label">冲突项</div>
        <div class="stat-value">0</div>
        <div class="stat-meta">所有类型已协调</div>
      </div>
    </div>

    <!-- V3 toolbar：搜索框 + 操作 -->
    <div class="toolbar">
      <el-input
        v-model="searchQuery"
        placeholder="跨类搜索：title / id / typeName 任意字段（OQ-4）"
        clearable
        style="width: 360px"
        @keyup.enter="doSearch"
      >
        <template #append>
          <button class="btn btn-primary" @click="doSearch">
            <span>🔍</span> 搜索
          </button>
        </template>
      </el-input>
      <span class="toolbar__spacer" />
      <button class="btn" @click="onFilterToggle">
        <span>▼</span> 高级筛选
      </button>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" class="search-results">
      <el-table :data="searchResults" stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="typeName" label="类型" width="120" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="matchedField" label="匹配字段" width="120">
          <template #default="{ row }">
            <el-tag :type="matchedFieldType(row.matchedField)" size="small">
              {{ row.matchedField }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <button class="btn" @click="goDetail(row.id)">查看</button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 6 类型入口卡片（V3 风格 + 端口蓝 hover） -->
    <h3 class="section-title">6 类型入口（点击进入）</h3>
    <div class="type-cards">
      <div
        v-for="type in ALL_KO_TYPES"
        :key="type"
        class="type-card"
        @click="goTypeList(type)"
      >
        <div class="type-card-content">
          <div class="type-code">{{ type }}</div>
            <div class="type-info">
              <div class="type-name">{{ KO_TYPE_NAMES[type] }}</div>
              <div class="type-count">{{ TYPE_COUNTS[type] }} 个</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ALL_KO_TYPES, KO_TYPE_NAMES, searchKo, type KoSearchResult } from '@/api/ko'

const router = useRouter()
const searchQuery = ref('')
const searchResults = ref<KoSearchResult[]>([])

// V9001 seed 数据：6 类型 KO 数量分布（合计 278）
const TYPE_COUNTS: Record<string, number> = {
  CON: 19,
  RUL: 47,
  PAR: 92,
  SCH: 41,
  PRM: 3,
  DOC: 76
}

async function doSearch() {
  const query = searchQuery.value.trim()
  if (!query) {
    searchResults.value = []
    return
  }
  const res = await searchKo({ query })
  if (res.code === 0) {
    searchResults.value = res.data
  }
}

function matchedFieldType(field: string): 'success' | 'warning' | 'info' {
  if (field === 'title') return 'success'
  if (field === 'id') return 'warning'
  return 'info'
}

function goTypeList(type: string) {
  router.push({ name: 'ko-type-list', params: { type: type.toLowerCase() } })
}

function goDetail(id: string) {
  const match = id.match(/^KO-([A-Z]+)-/)
  const type = match ? match[1].toLowerCase() : 'con'
  router.push({ name: 'ko-detail', params: { type, id } })
}

function onCreate() {
  ElMessage.info('新建 KO：U4 完整新建流程在 T204 实施时接入（DRAFT → 状态机）')
}

function onImport() {
  ElMessage.info('批量导入：U4 完整导入在 T204 实施时接入')
}

function onDownloadTemplate() {
  ElMessage.info('导入模版下载：U4 完整导入模版在 T204 实施时生成')
}

function onFilterToggle() {
  ElMessage.info('高级筛选：U4 完整筛选在 T204 实施时接入')
}
</script>

<style lang="scss" scoped>
.ko-library {
  padding: 20px;
}

// === F-53 V3 视觉还原：page-header / stat-card / toolbar / type-card ===
// 大部分样式从 src/styles/theme.scss 全局工具类继承，scoped 块只放本页特殊样式

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 24px 0 12px 0;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-row { margin-bottom: 16px; }

.type-cards { margin-top: 12px; }

.type-card {
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-left: 3px solid var(--port-blue);
  border-radius: 2px;
  padding: 16px 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-left-color: var(--port-blue-light);
    transform: translateX(2px);
    box-shadow: 0 2px 6px rgba(15, 76, 117, 0.08);
  }
}

.type-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.type-code {
  font-family: 'JetBrains Mono', monospace;
  font-size: 26px;
  font-weight: 700;
  color: var(--port-blue);
  min-width: 80px;
}

.type-info { flex: 1; }

.type-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.type-count {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.search-results {
  background: var(--bg-paper);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 8px;
  margin-bottom: 16px;
}
</style>