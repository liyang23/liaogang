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

  <!-- R11 重复检测命中弹窗（AE5 覆盖）：替换 / 取消 二选一 -->
  <el-dialog
    v-model="duplicateDialogOpen"
    title="已存在同名子项"
    width="420px"
    append-to-body
  >
    <p>「{{ duplicateDialog?.pendingTitle }}」与已存在条目同名，是否替换？</p>
    <template #footer>
      <el-button @click="onDuplicateCancel">取消</el-button>
      <el-button type="primary" @click="onDuplicateReplace">替换</el-button>
    </template>
  </el-dialog>

  <!-- R4b dirty 三按钮互斥（Save Draft & Close / Discard & Close / Cancel） -->
  <el-dialog
    v-model="dirtyDialogOpen"
    title="存在未保存修改"
    width="420px"
    append-to-body
  >
    <p>已修改 {{ dirtyCount }} 条子项。是否保存草稿 / 丢弃 / 留在弹层？</p>
    <template #footer>
      <el-button @click="onDirtyKeep">取消</el-button>
      <el-button type="danger" plain @click="onDirtyDiscard">丢弃并关闭</el-button>
      <el-button type="primary" @click="onDirtySaveDraft">保存草稿并关闭</el-button>
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

/**
 * U4 阶段：§3 子树内部 renderer 函数（不修改共享 markdown-renderer.ts）
 *
 * <p>R15b 命名空间约定：`{section.sectionIndex}.{varName}`（不反向约束 §4-§7）
 * <p>R7a 视觉一致性：高亮色 / 字号完全复用 §4-§7 已选 PAR；§7 解除后断引用视觉信号 = 黄色提示
 *
 * <p>Plan 路径 A：R15b 拍板为局部命名空间 → §3 内嵌 {{var}} 用 `{sectionIdx}.{varKey}` 命中
 * <p>Plan 路径 B fallback：R15b 拍板为全局命名空间 → 改走全局 reverse-index（占位骨架未实现）
 */
function resolveVarBinding(
  content: string,
  sectionIdx: number,
  varBindings: Record<string, string> | undefined,
): { hits: Array<{ varKey: string; koId: string | null; isBroken: boolean }> } {
  if (!content) return { hits: [] }
  const re = /\{\{\s*([^}]+?)\s*\}\}/g
  const hits: Array<{ varKey: string; koId: string | null; isBroken: boolean }> = []
  let m: RegExpExecArray | null
  while ((m = re.exec(content)) !== null) {
    const rawKey = (m[1] || '').trim()
    // R15b 局部命名空间：先查 {sectionIdx}.{varKey}，再退化查 {varKey} 全局
    const namespacedKey = `${sectionIdx}.${rawKey}`
    const koId =
      varBindings?.[namespacedKey] ??
      varBindings?.[rawKey] ??
      null
    hits.push({
      varKey: rawKey,
      koId,
      isBroken: koId === null,
    })
  }
  return { hits }
}

const props = defineProps<{
  modelValue: boolean
  /** Q-I4 U2 过渡期：接收 string（旧形态）或 ManualSubItem[]（新形态） */
  sectionIndex: number
  current: ManualSubItems[string]
  /** U4 阶段：跨段 varBindings 字典（不修改共享 markdown-renderer.ts + R15b 局部约定） */
  varBindings?: Record<string, string>
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

/** U5 阶段：重复检测（R11）+ 命中后弹窗二选一（AE5 覆盖）
 *  触发时机 = onBlur 且 title 字段失焦后去抖 300ms 检测（占位骨架：onBlur 同步触发）
 *  命中处置：重复 title 时弹出确认；用户选「替换」= 覆盖旧条目，「取消」= 抛弃新条目
 */
const duplicateDialog = ref<{ open: boolean; pendingTitle: string; pendingIdx: number; existingIdx: number } | null>(null)

const duplicateDialogOpen = computed({
  get: () => duplicateDialog.value?.open ?? false,
  set: (v) => {
    if (!v) duplicateDialog.value = null
  },
})

const dirtyCount = computed(() => {
  if (!lastSnapshot.value) return 0
  const before: ManualSubItem[] = JSON.parse(lastSnapshot.value)
  // 简化：长度变化 or 任一标题不同即计入 dirty（占位判定，U5 阶段接入深比较）
  if (before.length !== items.value.length) return items.value.length
  let diff = 0
  for (let i = 0; i < items.value.length; i++) {
    if (JSON.stringify(before[i]) !== JSON.stringify(items.value[i])) diff += 1
  }
  return diff
})

const dirtyDialogOpen = computed({
  get: () => dirtyDialog.value,
  set: (v) => {
    dirtyDialog.value = v
  },
})

function onRowBlur(idx: number) {
  const item = items.value[idx]
  if (!item.title || !item.title.trim()) return
  // 查重（自身之外的条目）
  const existingIdx = items.value.findIndex(
    (it, i) => i !== idx && it.title && it.title.trim() === item.title.trim(),
  )
  if (existingIdx >= 0) {
    duplicateDialog.value = {
      open: true,
      pendingTitle: item.title.trim(),
      pendingIdx: idx,
      existingIdx,
    }
  }
  // R10 数值字段校验（占位：value 字段启用后跑）
  onValidate(idx)
}

function onDuplicateReplace() {
  if (!duplicateDialog.value) return
  const { pendingIdx, existingIdx } = duplicateDialog.value
  // 替换：以新条目内容覆盖旧条目，移除新条目
  items.value[existingIdx] = { ...items.value[pendingIdx] }
  items.value.splice(pendingIdx, 1)
  duplicateDialog.value = null
}

function onDuplicateCancel() {
  if (!duplicateDialog.value) return
  const { pendingIdx } = duplicateDialog.value
  items.value.splice(pendingIdx, 1)
  duplicateDialog.value = null
}

/** U5 阶段：R4b dirty 检测（last-snapshot 与 current-snapshot 整对象深比较）
 *  占位骨架：开弹层时记录 lastSnapshot，关闭时比对；未变更时直接关，已变更时弹三按钮
 */
const lastSnapshot = ref<string>('')
const dirtyDialog = ref<boolean>(false)

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      // 记录入栈时子项集合的字符串快照
      lastSnapshot.value = JSON.stringify(items.value)
      dirtyDialog.value = false
    }
  },
)

function isDirty(): boolean {
  return JSON.stringify(items.value) !== lastSnapshot.value
}

function onCancel() {
  if (isDirty()) {
    dirtyDialog.value = true
    return
  }
  visible.value = false
}

function onDirtySaveDraft() {
  // 草稿保留 — emit 当前 items（占位：与 onConfirm 同路径；U5 阶段接入 IndexedDB 草稿）
  visible.value = false
}

function onDirtyDiscard() {
  // 丢弃 dirty 修改
  visible.value = false
}

function onDirtyKeep() {
  dirtyDialog.value = false
}

/** U5 阶段：R10 数值字段上下界校验 + R17a 后端落库 5xx 兜底
 *  占位骨架：R10 在 value/unit 字段启用后接入 async-validator；R17a 监听 onError 事件
 */
const validationError = ref<string>('')
function onValidate(idx: number) {
  const item = items.value[idx]
  if (item.value === undefined || item.value === null) return
  // 占位判定；R5 阶段 1 拍板后接入 [lower_bound, upper_bound] 校验
  if (item.value < 0) {
    validationError.value = `${item.title || `条目 ${idx + 1}`}：数值必须 ≥ 0`
  } else {
    validationError.value = ''
  }
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
