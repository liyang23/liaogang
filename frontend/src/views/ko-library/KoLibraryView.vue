<!--
  KO 库全景概览页（T203）
  6 类型入口卡片（CON / RUL / PAR / SCH / PRM / DOC）
  + 跨类搜索框（OQ-4：title + id + typeName）
-->
<template>
  <div class="ko-library">
    <h2 class="page-title">KO 库</h2>
    <p class="page-desc">知识对象库（6 种类型）— 约束 / 规则 / 参数 / 数据结构 / 提示词模板 / 文档</p>

    <!-- 跨类搜索 -->
    <el-card class="search-card" shadow="never">
      <el-input
        v-model="searchQuery"
        placeholder="跨类搜索：title / id / typeName 任意字段（OQ-4）"
        clearable
        @keyup.enter="doSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="doSearch">搜索</el-button>
        </template>
      </el-input>
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
              <el-button text type="primary" @click="goDetail(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 6 类型入口卡片 -->
    <h3 class="section-title">6 类型入口</h3>
    <el-row :gutter="20" class="type-cards">
      <el-col v-for="type in ALL_KO_TYPES" :key="type" :span="8">
        <el-card class="type-card" shadow="hover" @click="goTypeList(type)">
          <div class="type-card-content">
            <div class="type-code">{{ type }}</div>
            <div class="type-name">{{ KO_TYPE_NAMES[type] }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ALL_KO_TYPES, KO_TYPE_NAMES, searchKo, type KoSearchResult } from '@/api/ko'

const router = useRouter()
const searchQuery = ref('')
const searchResults = ref<KoSearchResult[]>([])

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
  // id 格式 KO-{TYPE}-{NNNN}，从 ID 提取 TYPE
  const match = id.match(/^KO-([A-Z]+)-/)
  const type = match ? match[1].toLowerCase() : 'con'
  router.push({ name: 'ko-detail', params: { type, id } })
}
</script>

<style scoped>
.ko-library { padding: 20px; }
.page-title { margin: 0 0 8px 0; font-size: 24px; }
.page-desc { color: #909399; margin: 0 0 20px 0; }
.search-card { margin-bottom: 24px; }
.search-results { margin-top: 16px; }
.section-title { font-size: 16px; margin: 24px 0 12px 0; color: #303133; }
.type-cards { margin-top: 12px; }
.type-card { cursor: pointer; transition: all 0.2s; margin-bottom: 16px; }
.type-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.type-card-content { display: flex; align-items: center; gap: 16px; padding: 8px 0; }
.type-code {
  font-family: 'JetBrains Mono', monospace;
  font-size: 28px;
  font-weight: bold;
  color: #0F4C75;  /* 端口蓝（DESIGN.md 颜色 token） */
  min-width: 80px;
}
.type-name { font-size: 16px; color: #303133; }
</style>