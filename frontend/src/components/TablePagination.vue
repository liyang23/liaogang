<template>
  <el-pagination
    :current-page="currentPage"
    :page-size="pageSize"
    :page-sizes="[10, 20, 50, 100]"
    :total="total"
    layout="total, sizes, prev, pager, next, jumper"
    class="table-pagination"
    @size-change="handleSizeChange"
    @current-change="handlePageChange"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  currentPage: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
}>()

const currentPage = ref(props.currentPage)
const pageSize = ref(props.pageSize)

watch(() => props.currentPage, (val) => { currentPage.value = val })
watch(() => props.pageSize, (val) => { pageSize.value = val })

function handleSizeChange(size: number) {
  pageSize.value = size
  emit('update:pageSize', size)
  emit('update:currentPage', 1) // 重置到第一页
}

function handlePageChange(page: number) {
  currentPage.value = page
  emit('update:currentPage', page)
}
</script>

<style lang="scss" scoped>
.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding: 8px 0;
}
</style>
