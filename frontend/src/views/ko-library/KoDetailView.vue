<!--
  KO 详情页（T203）
  URL: /ko-:type/:id （如 /ko-con/KO-CON-0001）
  展示 KO 完整定义 + 形式化定义 + 版本 + 引用
-->
<template>
  <div class="ko-detail" v-loading="loading">
    <!-- 返回按钮 -->
    <el-page-header @back="goBack" style="margin-bottom: 16px">
      <template #content>
        <span>KO 详情</span>
      </template>
    </el-page-header>

    <!-- 加载失败 -->
    <el-alert v-if="errorMsg" type="error" :title="errorMsg" :closable="false" />

    <!-- 标题区 -->
    <el-card v-if="detail" shadow="never">
      <template #header>
        <div class="detail-header">
          <h2 class="detail-title">
            <el-tag :type="statusType(detail.status)" size="default">{{ detail.status }}</el-tag>
            {{ detail.title }}
          </h2>
          <div class="detail-actions">
            <el-button type="primary" :icon="Edit" @click="onEdit" plain>编辑</el-button>
            <el-button type="danger" :icon="Delete" @click="onDelete" plain>删除</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag>{{ detail.typeName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>

        <el-descriptions-item label="编码">{{ detail.code }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ detail.projectId }}</el-descriptions-item>
        <el-descriptions-item label="效力">{{ detail.effect || '—' }}</el-descriptions-item>

        <el-descriptions-item label="层级">{{ detail.level || '—' }}</el-descriptions-item>
        <el-descriptions-item label="组织">{{ detail.organization || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createdBy || '—' }}</el-descriptions-item>

        <el-descriptions-item label="创建时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 形式化定义 -->
    <el-card v-if="detail?.definition" shadow="never" style="margin-top: 16px">
      <template #header>
        <h3 style="margin: 0">形式化定义</h3>
      </template>
      <pre class="definition">{{ detail.definition }}</pre>
    </el-card>

    <!-- 引用关系（KoReference 列表，T204 实施时填具体数据） -->
    <el-card v-if="detail?.references && detail.references.length > 0" shadow="never" style="margin-top: 16px">
      <template #header>
        <h3 style="margin: 0">引用关系（{{ detail.references.length }}）</h3>
      </template>
      <el-table :data="detail.references" stripe>
        <el-table-column prop="targetKoId" label="目标 KO ID" />
        <el-table-column prop="relationType" label="关系类型" width="160" />
        <el-table-column prop="description" label="描述" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getKo, deleteKo, type KoDetail as KoDetailType } from '@/api/ko'

const props = defineProps<{ type: string; id: string }>()
const router = useRouter()

const detail = ref<KoDetailType | null>(null)
const loading = ref(false)
const errorMsg = ref('')

async function loadDetail() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await getKo(props.id)
    if (res.code === 0) {
      detail.value = res.data
    } else {
      errorMsg.value = res.msg
    }
  } catch (e: any) {
    errorMsg.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function statusType(status?: string): 'success' | 'warning' | 'info' | 'primary' {
  if (status === 'Active') return 'success'
  if (status === 'Published') return 'primary'
  if (status === 'Review' || status === 'Approved') return 'warning'
  return 'info'
}

function goBack() {
  router.push({ name: 'ko-type-list', params: { type: props.type } })
}

function onEdit() {
  ElMessage.info('编辑 ' + props.id + '：U4 完整编辑流程在 T204 实施时接入')
}

async function onDelete() {
  try {
    await ElMessageBox.confirm(`确认删除 KO ${props.id}？此操作不可恢复（软删除）`, '删除确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  const res = await deleteKo(props.id)
  if (res.code === 0) {
    ElMessage.success('删除成功')
    goBack()
  } else {
    ElMessage.error(res.msg)
  }
}

onMounted(loadDetail)
watch(() => props.id, loadDetail)
</script>

<style scoped>
.ko-detail { padding: 20px; }
.detail-header { display: flex; align-items: center; justify-content: space-between; }
.detail-title { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 20px; }
.detail-actions { display: flex; gap: 8px; }
.definition {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>