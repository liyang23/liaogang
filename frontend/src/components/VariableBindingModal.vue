<!--
  变量绑定弹窗（T210）
  OQ-16 变量绑定 PAR 单选弹层：⚡自动匹配 + ✎当前绑定 + ⊕重选 + ×解除
  输入：当前 section content 的 {{var}} 列表
  输出：varKey → koId 映射
-->
<template>
  <el-dialog
    :model-value="visible"
    title="变量绑定"
    width="520px"
    @update:model-value="(v) => emit('update:visible', v)"
  >
    <el-form label-width="100px">
      <el-form-item label="Section">
        <el-tag size="small">#{{ sectionIndex }} {{ sectionTitle }}</el-tag>
      </el-form-item>

      <el-form-item
        v-for="varKey in varKeys"
        :key="varKey"
        :label="varKey"
      >
        <el-row :gutter="8" align="middle">
          <el-col :span="14">
            <el-select
              v-model="bindings[varKey]"
              filterable
              placeholder="从 KO 库选 PAR（参数）"
              style="width: 100%"
              @change="(v) => onBindChange(varKey, v)"
            >
              <el-option
                v-for="par in availablePars"
                :key="par.id"
                :label="`${par.id} - ${par.title}`"
                :value="par.id"
              />
            </el-select>
          </el-col>
          <el-col :span="10">
            <el-button
              v-if="bindings[varKey]"
              size="small"
              type="warning"
              plain
              @click="onUnbind(varKey)"
            >
              × 解除
            </el-button>
            <el-button
              v-if="canAutoMatch(varKey) && !bindings[varKey]"
              size="small"
              type="success"
              plain
              @click="onAutoMatch(varKey)"
            >
              ⚡ 自动匹配
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>

      <el-alert v-if="varKeys.length === 0" type="info" :closable="false" show-icon>
        当前 Section content 无 {{var}} 占位符
      </el-alert>
    </el-form>

    <template #footer>
      <el-button @click="onClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  sectionIndex: number
  sectionTitle: string
  sectionContent: string
  currentBindings: Record<string, string>
  availablePars: Array<{ id: string; title: string; symbol?: string }>
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'update:bindings', sectionIndex: number, bindings: Record<string, string>): void
}>()

/** 从 content 提取所有 {{var}} 占位符 */
const varKeys = computed(() => {
  const matches = props.sectionContent.match(/\{\{([^}]+)\}\}/g) || []
  return [...new Set(matches.map(m => m.replace(/[{}\s]/g, '')))]
})

/** 当前绑定（局部状态，确认后 emit） */
const bindings = ref<Record<string, string>>({ ...props.currentBindings })

watch(
  () => props.visible,
  (v) => {
    if (v) {
      // 每次打开时重置
      bindings.value = { ...props.currentBindings }
    }
  }
)

/** 检测 ⚡自动匹配：varKey 名与 PAR.symbol 匹配（OQ-16） */
function canAutoMatch(varKey: string): boolean {
  return props.availablePars.some(
    p => p.symbol?.toLowerCase() === varKey.toLowerCase() ||
         p.id.toLowerCase() === varKey.toLowerCase()
  )
}

function onAutoMatch(varKey: string) {
  const matched = props.availablePars.find(
    p => p.symbol?.toLowerCase() === varKey.toLowerCase() ||
         p.id.toLowerCase() === varKey.toLowerCase()
  )
  if (matched) {
    bindings.value = { ...bindings.value, [varKey]: matched.id }
  }
}

function onBindChange(varKey: string, value: string) {
  bindings.value = { ...bindings.value, [varKey]: value }
}

function onUnbind(varKey: string) {
  const newBindings = { ...bindings.value }
  delete newBindings[varKey]
  bindings.value = newBindings
  emit('update:bindings', props.sectionIndex, newBindings)
}

function onClose() {
  emit('update:bindings', props.sectionIndex, bindings.value)
  emit('update:visible', false)
}
</script>

<style scoped>
.el-alert { margin-top: 8px; }
</style>