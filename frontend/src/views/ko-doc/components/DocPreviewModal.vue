<!--
  DocPreviewModal.vue - U10 文档预览 900px 中央 Dialog (T314)
  与 §3 ManualSubItemModal 抽共用 DialogBase 骨架 (R12a 视觉一致性继承 design token)
  6 格式分发 (PDF / DOCX / XLSX / PPTX / TXT / PNG / JPG)
-->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="900px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="doc-preview-modal"
  >
    <!-- 客户端预校验 ≤ 10MB (NFR-25) -->
    <div v-if="fileSize > 10 * 1024 * 1024" class="oversize-warning">
      <el-alert
        title="文件过大"
        type="error"
        :closable="false"
        show-icon
      >
        文件大小 {{ (fileSize / 1024 / 1024).toFixed(2) }} MB 超过 10MB 限制
      </el-alert>
    </div>

    <!-- 6 格式分发 -->
    <div v-else class="preview-content">
      <iframe
        v-if="strategy === 'PDFJS_DIRECT'"
        :src="previewUrl"
        class="pdf-iframe"
        title="PDF 预览"
      />
      <iframe
        v-else-if="strategy === 'PDFJS_CONVERTED'"
        :src="previewUrl"
        class="pdf-iframe"
        title="DOCX/XLSX/PPTX 转换 PDF 预览"
      />
      <pre v-else-if="strategy === 'TEXT_DIRECT'" class="text-preview">{{ fileContent }}</pre>
      <img
        v-else-if="strategy === 'IMAGE_DIRECT'"
        :src="previewUrl"
        class="image-preview"
        :alt="fileName"
      />
      <div v-else class="unsupported">
        <el-alert
          title="不支持的格式"
          type="warning"
          :closable="false"
          show-icon
        >
          不支持预览该文件格式，请下载后查看。
          <el-link type="primary" :href="previewUrl" :underline="false">下载文件</el-link>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="modal-footer">
        <el-link type="primary" :href="previewUrl" :underline="false">下载原文件</el-link>
        <el-button @click="onClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  strategy?: 'PDFJS_DIRECT' | 'PDFJS_CONVERTED' | 'TEXT_DIRECT' | 'IMAGE_DIRECT' | 'UNSUPPORTED'
  previewUrl?: string
  fileName?: string
  fileSize?: number
  fileContent?: string
}>(), {
  modelValue: false,
  title: '文档预览',
  strategy: 'PDFJS_DIRECT',
  previewUrl: '',
  fileName: '',
  fileSize: 0,
  fileContent: '',
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

function onClose() {
  visible.value = false
}
</script>

<style scoped>
.doc-preview-modal :deep(.el-dialog__body) {
  padding: 16px 20px;
  min-height: 500px;
}
.preview-content {
  width: 100%;
  min-height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.pdf-iframe {
  width: 100%;
  height: 600px;
  border: 1px solid var(--border-color, #ebeef5);
  border-radius: 4px;
}
.text-preview {
  width: 100%;
  height: 500px;
  padding: 16px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  background: var(--bg-paper, #f5f7fa);
  border-radius: 4px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.image-preview {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
}
.unsupported {
  text-align: center;
  padding: 40px 20px;
}
.oversize-warning {
  text-align: center;
  padding: 40px 20px;
}
.modal-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
}
</style>
