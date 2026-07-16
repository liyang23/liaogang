<!--
  KO 类型列表页（T203）
  URL: /ko-:type （如 /ko-con / ko-rul / ...）
  展示某类型 KO 列表 + 跨类搜索 + 新建 + 详情跳转
-->
<template>
  <div class="ko-type-list">
    <h2 class="page-title">
      {{ typeName }} <el-tag size="small">{{ type.toUpperCase() }}</el-tag>
    </h2>

    <!-- 工具栏：搜索 + 新建 -->
    <div class="toolbar">
      <el-input
        v-model="searchQuery"
        :placeholder="`搜索 ${typeName}：title / id`"
        clearable
        style="width: 300px"
        @keyup.enter="doSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="doSearch">搜索</el-button>
        </template>
      </el-input>
      <el-button type="primary" :icon="Plus" @click="onCreate" style="margin-left: auto">
        新建 {{ typeName }}
      </el-button>
    </div>

    <!-- 列表 -->
    <el-table
      :data="rows"
      stripe
      v-loading="loading"
      @row-click="goDetail"
      style="cursor: pointer; margin-top: 12px"
    >
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="version" label="版本" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>

    <!-- 分页（TablePagination 通用组件） -->
    <TablePagination
      v-model:page="page"
      v-model:size="size"
      :total="total"
      @change="loadList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { KO_TYPE_NAMES, listKo, type KoListItem } from '@/api/ko'
import TablePagination from '@/components/TablePagination.vue'

const props = defineProps<{ type: string }>()
const router = useRouter()

// URL 参数 type 小写 → 转大写查 KO_TYPE_NAMES
const type = computed(() => props.type.toUpperCase())
const typeName = computed(() => KO_TYPE_NAMES[type.value] || type.value)

const rows = ref<KoListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const searchQuery = ref('')

async function loadList() {
  loading.value = true
  try {
    const res = await listKo({
      type: type.value,
      page: page.value,
      size: size.value
    })
    if (res.code === 0) {
      rows.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  // 简化为 reload（实际可用 searchKo 跨字段搜索）
  await loadList()
}

function statusType(status?: string): 'success' | 'warning' | 'info' | 'primary' {
  if (status === 'Active') return 'success'
  if (status === 'Published') return 'primary'
  if (status === 'Review' || status === 'Approved') return 'warning'
  return 'info'
}

function onCreate() {
  ElMessage.info('新建 ' + typeName + '：U4 完整新建流程在 T204 实施时接入（DRAFT → 状态机）')
}

function goDetail(row: KoListItem) {
  router.push({ name: 'ko-detail', params: { type: props.type, id: row.id } })
}

onMounted(loadList)
watch(type, loadList)
</script>

<style scoped>
.ko-type-list { padding: 20px; }
.page-title { margin: 0 0 16px 0; font-size: 22px; display: flex; align-items: center; gap: 8px; }
.toolbar { display: flex; align-items: center; gap: 12px; }
</style>