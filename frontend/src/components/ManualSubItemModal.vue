<!--
  ManualSubItemModal（Q-I4 §3 弹层 / 中央 Dialog 容器）
  R0a = 中央 Dialog（占位决议）+ R1b = 行内列表（占位决议）
  7 状态 + R-onboarding + R-a11y-baseline + R4b dirty + R11 重复检测 + R17a 兜底占位骨架

  本文件是 plan `docs/plans/2026-07-17-001-feat-liaogang-section3-manual-subitem-modal-plan.md`
  U2 实施的骨架（directional guidance），完整逻辑在 spec-work 后续阶段补全。
-->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="720px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="manual-sub-item-modal"
    :data-state="currentState"
  >
    <!-- R-onboarding: empty 态首次进入引导 -->
    <div v-if="currentState === 'empty'" class="onboarding">
      <p class="onboarding-tip">
        {{ items.length === 0
          ? '§3 计算范围目前为空，点击下方按钮添加第一条子项。'
          : '尚未录入子项。' }}
      </p>
      <p class="onboarding-example">
        示例：title=「料场库存上下限」，content=「当前最小库存 850 吨，最大 1000 吨」
      </p>
    </div>

    <!-- R11c high-row-scroll: 子项数 ≥ 软上限（默认 200）启用虚拟滚动 -->
    <div v-if="currentState !== 'empty'" class="subitem-list" :class="{ 'virtual-scroll': isHighRow }">
      <div
        v-for="(item, idx) in items"
        :key="idx"
        class="subitem-row"
        :data-row-idx="idx"
      >
        <el-input
          v-model="item.title"
          placeholder="子项标题"
          class="title-input"
          @blur="onRowBlur(idx)"
        />
        <el-input
          v-model="item.content"
          type="textarea"
          :rows="3"
          placeholder="子项内容（支持 Markdown）"
          @blur="onRowBlur(idx)"
        />
        <el-button
          size="small"
          type="danger"
          plain
          @click="removeItem(idx)"
        >
          删除
        </el-button>
      </div>
    </div>

    <!-- 顶部添加按钮 + R3b 批量粘贴入口（占位：R3b 启用后接入 manual-sub-item-parser.ts） -->
    <template #footer>
      <div class="modal-footer">
        <el-button @click="addItem">+ 新增子项</el-button>
        <el-button v-if="batchPasteEnabled" plain @click="onBatchPaste">批量粘贴</el-button>
        <div class="footer-spacer" />
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" @click="onConfirm">完成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 弹层骨架 — 7 状态 + dirty + 重复检测 + 批量粘贴占位
 * 实施阶段补全：
 *  - R11c 7 状态完整 enter/exit trigger 矩阵（origin R11c-trigger-condition）
 *  - R-onboarding empty 态引导文案
 *  - R-a11y-baseline aria-required / aria-describedby / aria-live=polite
 *  - R4b dirty 三按钮（Save Draft & Close / Discard & Close / Cancel）
 *  - R11 重复检测（onBlur + 去抖 300ms）
 *  - R17a 后端落库 5xx 兜底
 *  - R3b 批量粘贴（依赖 manual-sub-item-parser.ts）
 *  - 软上限强制虚拟滚动（Element Plus el-table-v2）
 */
import { computed, ref, watch } from 'vue'
import {
  type ManualSubItem,
  type ManualSubItems,
  getManualSubItemList,
  manualSubItemsToString,
} from '@/api/composer'

const props = defineProps<{
  modelValue: boolean
  /** Q-I4 U2 过渡期：接收 string（旧形态）或 ManualSubItem[]（新形态） */
  sectionIndex: number
  current: ManualSubItems[string]
  /** R3b 启用状态（占位决议：是；U2 期间默认 true 由 composable 注入） */
  batchPasteEnabled?: boolean
  /** 软上限（默认 200，origin R9 占位决议） */
  softCap?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'update', sectionIndex: number, items: ManualSubItem[]): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const title = computed(() => `手动子项 · #${props.sectionIndex}`)
const items = ref<ManualSubItem[]>([])

/** 初始化：从父组件 current prop 转换 */
watch(
  () => [props.modelValue, props.current] as const,
  ([open, current]) => {
    if (!open) return
    items.value = getManualSubItemList(current, props.sectionIndex)
  },
  { immediate: true },
)

/** R11c 7 态 — 骨架:empty / non-empty 二态（完整 trigger 矩阵在实施阶段补） */
const currentState = computed(() => (items.value.length === 0 ? 'empty' : 'normal'))

/** 软上限强制虚拟滚动（占位判定；Element Plus el-table-v2 实施阶段接入） */
const isHighRow = computed(() => items.value.length >= (props.softCap ?? 200))

function addItem() {
  items.value.push({ title: '', content: '' })
}

function removeItem(idx: number) {
  items.value.splice(idx, 1)
}

/** onBlur 占位：完整 R11 重复检测 + R4b dirty 检测在实施阶段补 */
function onRowBlur(_idx: number) {
  // deferred to plan U5
}

function onBatchPaste() {
  // R3b 启用时通过 manual-sub-item-parser.ts 接入
}

function onCancel() {
  visible.value = false
}

function onConfirm() {
  emit('update', props.sectionIndex, items.value)
  visible.value = false
}
</script>

<style scoped>
.manual-sub-item-modal :deep(.el-dialog__body) { padding: 16px 20px; }
.onboarding { padding: 12px 0; color: var(--text-secondary, #606266); }
.onboarding-tip { font-size: 14px; margin: 0 0 8px; }
.onboarding-example { font-size: 12px; color: var(--text-tertiary, #909399); margin: 0; }
.subitem-list { display: flex; flex-direction: column; gap: 12px; max-height: 60vh; overflow-y: auto; }
.subitem-list.virtual-scroll { /* 占位：Element Plus el-table-v2 接入位置 */ }
.subitem-row { display: grid; grid-template-columns: 1fr 2fr auto; gap: 8px; align-items: start; }
.title-input { width: 100%; }
.modal-footer { display: flex; align-items: center; gap: 8px; }
.footer-spacer { flex: 1; }
</style>
