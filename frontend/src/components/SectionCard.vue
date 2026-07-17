<!--
  PRM Section 卡片（T210）
  FIXED（变量赋值型）：显示 content 中的 {{var}} 占位
  DYNAMIC（动态选择型）：KO 选择 + 手动子项编辑器
  触发变量绑定弹窗（VariableBindingModal）
-->
<template>
  <el-card class="section-card" shadow="hover">
    <template #header>
      <div class="section-header">
        <div class="section-title">
          <el-tag :type="section.sectionType === 'FIXED' ? 'primary' : 'success'" size="small">
            {{ section.sectionType }}
          </el-tag>
          <span class="section-index">#{{ section.sectionIndex }}</span>
          <span class="section-name">{{ section.title }}</span>
        </div>
        <div class="section-actions">
          <el-button
            v-if="section.sectionType === 'FIXED' && hasVarBinding"
            size="small"
            type="primary"
            plain
            @click="onBindVar"
          >
            ⚡ 绑定变量
          </el-button>
          <el-button size="small" @click="expanded = !expanded">
            {{ expanded ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>
    </template>

    <div v-show="expanded">
      <pre class="content-preview">{{ section.content }}</pre>

      <!-- DYNAMIC Section：KO 选择 + 手动子项 -->
      <div v-if="section.sectionType === 'DYNAMIC'" class="dynamic-section">
        <el-form label-width="100px" size="small">
          <el-form-item label="KO 选择">
            <el-select
              v-model="selectedKOIds"
              multiple
              filterable
              placeholder="从 KO 库选 KO"
              style="width: 100%"
              @change="onSelectedKOsChange"
            >
              <el-option
                v-for="ko in availableKOs"
                :key="ko.id"
                :label="`${ko.id} - ${ko.title}`"
                :value="ko.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="手动子项">
            <div class="manual-subitem-entry">
              <el-button size="small" type="primary" plain @click="openManualSubItemModal">
                手动子项（{{ manualSubItemCount }} 项）
              </el-button>
              <ManualSubItemModal
                v-model="manualSubItemModalVisible"
                :section-index="section.sectionIndex"
                :current="manualSubItems[section.sectionIndex] ?? ''"
                @update="onManualSubItemsUpdated"
              />
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 当前变量绑定状态显示 -->
      <div v-if="bindingCount > 0" class="binding-info">
        <el-alert type="success" :closable="false" show-icon>
          ✓ 已绑定 {{ bindingCount }} 个变量（OQ-16 装配数动态计算）
        </el-alert>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  type PrmSection,
  type VarBindings,
  type SelectedKOs,
  type ManualSubItems,
  type ManualSubItem,
  getManualSubItemList,
} from '@/api/composer'
import ManualSubItemModal from './ManualSubItemModal.vue'

const props = defineProps<{
  section: PrmSection
  varBindings: VarBindings
  selectedKOs: SelectedKOs
  manualSubItems: ManualSubItems
  availableKOs: Array<{ id: string; title: string }>
}>()

const emit = defineEmits<{
  (e: 'update:varBindings', sectionIndex: number, bindings: { [k: string]: string }): void
  (e: 'update:selectedKOs', sectionIndex: number, koIds: string[]): void
  (e: 'update:manualSubItems', sectionIndex: number, content: ManualSubItem[]): void
  (e: 'bind-var', sectionIndex: number): void
}>()

const expanded = ref(true)
const selectedKOIds = ref<string[]>(props.selectedKOs[props.section.sectionIndex] || [])
const manualSubItemModalVisible = ref(false)

/** 当前 section 手动子项条目数（取自父组件 current prop，过渡期兼容 string 形态） */
const manualSubItemCount = computed(() =>
  getManualSubItemList(props.manualSubItems[props.section.sectionIndex] ?? '', props.section.sectionIndex).length,
)

/** 检测 content 是否含 {{var}} 占位 */
const hasVarBinding = computed(() => /\{\{[^}]+\}\}/.test(props.section.content))

/** 当前 section 绑定变量数 */
const bindingCount = computed(() => {
  return Object.keys(props.varBindings[props.section.sectionIndex] || {}).length
})

function onBindVar() {
  emit('bind-var', props.section.sectionIndex)
}

function onSelectedKOsChange(value: string[]) {
  selectedKOIds.value = value
  emit('update:selectedKOs', props.section.sectionIndex, value)
}

function openManualSubItemModal() {
  manualSubItemModalVisible.value = true
}

/** 弹层内 Confirmed 后回调（占位：直接 emit array；U5 阶段补 dirty 检测 / 重复检测） */
function onManualSubItemsUpdated(sectionIndex: number, items: ManualSubItem[]) {
  emit('update:manualSubItems', sectionIndex, items)
}
</script>

<style scoped>
.section-card { margin-bottom: 12px; }
.section-header { display: flex; align-items: center; justify-content: space-between; }
.section-title { display: flex; align-items: center; gap: 8px; }
.section-index { color: #909399; font-family: monospace; }
.section-name { font-weight: 500; }
.content-preview {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0 0 12px 0;
  max-height: 200px;
  overflow-y: auto;
}
.dynamic-section { margin-top: 12px; }
.manual-subitem-entry { display: flex; align-items: center; gap: 12px; }
.binding-info { margin-top: 12px; }
</style>