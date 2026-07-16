<!--
  PRM 三栏组装器页（T210）
  顶部：PRM 模板选择栏（3 预置模板下拉）
  中栏：Section 编排列表（SectionCard × N + 变量绑定弹窗）
  右栏：实时预览（Handlebars 渲染 + Markdown → HTML）+ 字符数 + token 数 + 装配数（OQ-16）
-->
<template>
  <div class="composer">
    <h2 class="page-title">PRM 三栏组装器</h2>

    <!-- 顶部：PRM 模板选择 -->
    <el-card shadow="never" class="template-card">
      <el-select
        v-model="selectedTemplateId"
        placeholder="选择 PRM 模板"
        style="width: 320px"
        @change="onTemplateChange"
      >
        <el-option
          v-for="t in templates"
          :key="t.id"
          :label="`${t.id} - ${t.name}`"
          :value="t.id"
        />
      </el-select>
      <el-tag v-if="currentTemplate" style="margin-left: 12px" type="info">
        {{ currentTemplate.description }}
      </el-tag>
    </el-card>

    <div class="dashboard-row" v-if="currentTemplate">
      <!-- 中栏：Section 编排 -->
      <div>
        <el-card shadow="never" class="middle-card">
          <template #header>
            <span>Sections（{{ currentTemplate.sections?.length || 0 }} 段）</span>
          </template>
          <SectionCard
            v-for="section in currentTemplate.sections || []"
            :key="section.id || section.sectionIndex"
            :section="section"
            :var-bindings="varBindings"
            :selected-k-o-s="selectedKOs"
            :manual-sub-items="manualSubItems"
            :available-k-o-s="[]"
            @update:var-bindings="onVarBindingsUpdate"
            @update:selected-k-o-s="onSelectedKOsUpdate"
            @update:manual-sub-items="onManualSubItemsUpdate"
            @bind-var="onBindVarClick"
          />
        </el-card>
      </div>

      <!-- 右栏：实时预览 -->
      <div>
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="preview-header">
              <span>实时预览（OQ-15 Handlebars + Markdown）</span>
              <el-tag :type="previewTokenColor" size="small">
                {{ charCount }} 字 / {{ tokenCount }} tokens
              </el-tag>
            </div>
          </template>

          <div class="preview-stats">
            <el-statistic title="装配数（OQ-16）" :value="assemblyCount" />
            <el-statistic title="Section 数" :value="currentTemplate.sections?.length || 0" />
            <el-statistic title="已绑定变量" :value="totalBindingCount" />
          </div>

          <el-divider />

          <div class="preview-content" v-html="renderedHtml" />
        </el-card>
      </div>
    </div>

    <!-- 变量绑定弹窗 -->
    <VariableBindingModal
      v-model:visible="bindingModalVisible"
      :section-index="bindingSectionIndex"
      :section-title="bindingSectionTitle"
      :section-content="bindingSectionContent"
      :current-bindings="bindingCurrent"
      :available-pars="[]"
      @update:bindings="onBindingsUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SectionCard from '@/components/SectionCard.vue'
import VariableBindingModal from '@/components/VariableBindingModal.vue'
import { listPrmTemplates, estimateTokens, type PrmTemplate, type PrmSection, type VarBindings, type SelectedKOs, type ManualSubItems } from '@/api/composer'
import { render as renderMarkdown } from '@/utils/markdown-renderer'
import { render as renderHandlebars } from '@/utils/handlebars'

const templates = ref<PrmTemplate[]>([])
const selectedTemplateId = ref<string>('')
const currentTemplateDetail = ref<{ template: PrmTemplate; sections: PrmSection[] } | null>(null)
const varBindings = ref<VarBindings>({})
const selectedKOs = ref<SelectedKOs>({})
const manualSubItems = ref<ManualSubItems>({})

const bindingModalVisible = ref(false)
const bindingSectionIndex = ref(0)
const bindingSectionTitle = ref('')
const bindingSectionContent = ref('')
const bindingCurrent = ref<Record<string, string>>({})

/** 选中的模板（响应式计算） */
const currentTemplate = computed(() => currentTemplateDetail.value)

/** 渲染结果 */
const renderedHtml = computed(() => {
  if (!currentTemplateDetail.value) return '<p>请选择 PRM 模板</p>'
  const sections = currentTemplateDetail.value.sections || []
  const joined = sections
    .map(s => `## ${s.title}\n\n${s.content}`)
    .join('\n\n')
  // OQ-15：先 handlebars 替换 {{var}}，再 markdown 渲染
  const expanded = renderHandlebars(joined, {
    ...varBindings.value,
    ...selectedKOs.value,
    ...manualSubItems.value
  } as Record<string, unknown>)
  return renderMarkdown(expanded)
})

/** 字符数 + token 数（OQ-15 估算 2 chars/token） */
const charCount = computed(() => renderedHtml.value.length)
const tokenCount = computed(() => Math.ceil(charCount.value / 2))

const previewTokenColor = computed(() => {
  if (tokenCount.value > 1000) return 'danger'
  if (tokenCount.value > 500) return 'warning'
  return 'success'
})

/** OQ-16 实际装配数 = selectedKOs.length + varBindings.size + manualSubItems.size */
const assemblyCount = computed(() => {
  let count = 0
  for (const k of Object.keys(selectedKOs.value)) {
    count += selectedKOs.value[k].length
  }
  for (const k of Object.keys(varBindings.value)) {
    count += Object.keys(varBindings.value[k]).length
  }
  for (const k of Object.keys(manualSubItems.value)) {
    if (manualSubItems.value[k]) count += 1
  }
  return count
})

const totalBindingCount = computed(() => {
  let count = 0
  for (const k of Object.keys(varBindings.value)) {
    count += Object.keys(varBindings.value[k]).length
  }
  return count
})

onMounted(async () => {
  const res = await listPrmTemplates()
  if (res.code === 0) {
    templates.value = res.data
    if (templates.value.length > 0) {
      selectedTemplateId.value = templates.value[0].id
      await loadTemplate(templates.value[0].id)
    }
  } else {
    ElMessage.warning('PRM 模板加载失败（后端 API 待 T208+ 实施），使用 mock 数据')
    // mock fallback
    templates.value = [{
      id: 'KO-PRM-0001', name: '大窑湾统筹优化 PRM 模板',
      description: 'mock 数据', version: 'v1.0.0'
    } as PrmTemplate]
    selectedTemplateId.value = 'KO-PRM-0001'
  }
})

async function onTemplateChange(templateId: string) {
  await loadTemplate(templateId)
  // 清空旧绑定
  varBindings.value = {}
  selectedKOs.value = {}
  manualSubItems.value = {}
}

async function loadTemplate(templateId: string) {
  // TODO T208+ 后端支持后用 getPrmTemplate
  // 现在 mock：直接从前端硬编码（与 seed/prm-templates.yaml 一致）
  currentTemplateDetail.value = MOCK_TEMPLATES[templateId] || null
}

function onVarBindingsUpdate(sectionIndex: number, bindings: Record<string, string>) {
  varBindings.value = { ...varBindings.value, [sectionIndex]: bindings }
}

function onSelectedKOsUpdate(sectionIndex: number, koIds: string[]) {
  selectedKOs.value = { ...selectedKOs.value, [sectionIndex]: koIds }
}

function onManualSubItemsUpdate(sectionIndex: number, content: string) {
  manualSubItems.value = { ...manualSubItems.value, [sectionIndex]: content }
}

function onBindVarClick(sectionIndex: number) {
  const sections = currentTemplateDetail.value?.sections || []
  const section = sections.find(s => s.sectionIndex === sectionIndex)
  if (!section) return
  bindingSectionIndex.value = sectionIndex
  bindingSectionTitle.value = section.title
  bindingSectionContent.value = section.content
  bindingCurrent.value = { ...(varBindings.value[sectionIndex] || {}) }
  bindingModalVisible.value = true
}

function onBindingsUpdate(sectionIndex: number, bindings: Record<string, string>) {
  varBindings.value = { ...varBindings.value, [sectionIndex]: bindings }
}

// === Mock 数据（与 seed/prm-templates.yaml 对齐；T208+ 后端支持后删除） ===
const MOCK_TEMPLATES: Record<string, { template: PrmTemplate; sections: PrmSection[] }> = {
  'KO-PRM-0001': {
    template: { id: 'KO-PRM-0001', name: '大窑湾统筹优化', description: '9 段示例', version: 'v1.0.0' },
    sections: [
      { id: 1, templateId: 'KO-PRM-0001', sectionIndex: 1, title: '任务背景', sectionType: 'FIXED', content: '你是大窑湾集装箱码头的智能调度助手。' },
      { id: 2, templateId: 'KO-PRM-0001', sectionIndex: 2, title: '输入参数', sectionType: 'FIXED', content: '船舶到港 {{arrival_time}}。' },
      { id: 3, templateId: 'KO-PRM-0001', sectionIndex: 3, title: '硬约束', sectionType: 'FIXED', content: '1. 即到即靠\n2. 安全距离' }
    ]
  }
}
</script>

<style scoped>
.composer { padding: 16px 24px; }
.page-title { margin: 0 0 16px 0; font-size: 22px; }
.middle-card, .preview-card { margin-bottom: 16px; }
.preview-header { display: flex; align-items: center; justify-content: space-between; }
.preview-stats { display: flex; gap: 24px; margin-bottom: 12px; }
.preview-content {
  background: var(--bg-canvas);
  padding: 16px;
  border-radius: 2px;
  max-height: 600px;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 14px;
}
</style>