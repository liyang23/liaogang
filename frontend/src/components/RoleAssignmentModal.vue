<!--
  角色分配弹窗（T206）
  输入 user_sub（多个用逗号分隔）→ 分配当前选中角色
  OQ-5 跨设备撤销不可行：本组件不调 cancel API，分配后用户当前 session 不变
-->
<template>
  <el-dialog
    :model-value="visible"
    title="分配角色"
    width="480px"
    @update:model-value="(v) => emit('update:visible', v)"
  >
    <el-form label-width="80px">
      <el-form-item label="角色">
        <el-tag>{{ roleName }}</el-tag>
      </el-form-item>
      <el-form-item label="用户 sub">
        <el-input
          v-model="userSubs"
          type="textarea"
          :rows="3"
          placeholder="多个用户用英文逗号分隔，如 user-001, user-002"
        />
      </el-form-item>
      <el-form-item label="提示">
        <el-alert type="info" :closable="false" show-icon>
          OQ-12 角色变更下次登录生效（旧 session 保持原权限直至重新登录）
        </el-alert>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" @click="onSubmit">分配</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  roleName: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'submit', userSubs: string[]): void
}>()

const userSubs = ref('')

watch(
  () => props.visible,
  (v) => {
    if (v) userSubs.value = ''
  }
)

function onCancel() {
  emit('update:visible', false)
}

function onSubmit() {
  const subs = userSubs.value
    .split(',')
    .map(s => s.trim())
    .filter(s => s.length > 0)
  if (subs.length === 0) return
  emit('submit', subs)
  emit('update:visible', false)
}
</script>

<style scoped>
.el-alert { margin: 0; }
</style>