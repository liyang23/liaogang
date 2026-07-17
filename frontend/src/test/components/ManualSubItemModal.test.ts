/**
 * ManualSubItemModal.vue 骨架测试（Q-I4 U2 阶段）
 *
 * 完整测试覆盖在 plan `docs/plans/2026-07-17-001-feat-liaogang-section3-manual-subitem-modal-plan.md`
 * U5 实施阶段补齐：R10 校验、R11 重复检测、R4b dirty 三按钮、R17a 兜底、AE5 弹窗二选一
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ManualSubItemModal from '@/components/ManualSubItemModal.vue'
import ElementPlus from 'element-plus'
import {
  type ManualSubItem,
  manualSubItemsFromString,
  manualSubItemsToString,
} from '@/api/composer'

const factory = (props: Record<string, unknown> = {}) =>
  mount(ManualSubItemModal, {
    props: {
      modelValue: true,
      sectionIndex: 3,
      current: '',
      ...props,
    },
    global: { plugins: [ElementPlus] },
  })

describe('ManualSubItemModal · 弹层骨架', () => {
  it('R-onboarding empty 态首次显示引导文案', () => {
    const wrapper = factory()
    expect(wrapper.text()).toContain('§3 计算范围目前为空')
  })

  it('manualSubItemsFromString 把字符串拆为 ManualSubItem[]', () => {
    const result = manualSubItemsFromString('料场库存上下限\n当前最小库存 850 吨\n---\n料场编号\nKO-MAT-001')
    expect(result.length).toBe(2)
    expect(result[0].title).toBe('料场库存上下限')
    expect(result[0].content).toContain('当前最小库存 850 吨')
    expect(result[1].title).toBe('料场编号')
  })

  it('manualSubItemsToString 把数组扁平回字符串（U2/U3 过渡工具）', () => {
    const items: ManualSubItem[] = [
      { title: '料场上限', content: 'max=1000 吨' },
      { title: '料场下限', content: 'min=850 吨' },
    ]
    const result = manualSubItemsToString(items)
    expect(result).toContain('料场上限')
    expect(result).toContain('---')
  })

  it('current = 字符串格式自动拆分还原', () => {
    const wrapper = factory({
      current: '料场库存上下限\n当前最小库存 850 吨，最大 1000 吨',
    })
    // 弹层打开后 items 已初始化（watch immediate）
    expect(wrapper.text()).toContain('料场库存上下限')
  })

  // R11c 7 状态 / R10 校验 / R11 重复检测 / R4b dirty 三按钮 / R17a 兜底 / R3b 批量粘贴
  // 全部 placeholder，U5 实施阶段补 AE1-AE10 + 10 条 AE 覆盖
  it.todo('R11c 7 状态 enter/exit trigger 矩阵（A11-A17）')
  it.todo('AE1 — 中央 Dialog 行内列表模式打开 + inline 编辑态')
  it.todo('AE2 — 数值字段校验 [0, 1000] 即时报错')
  it.todo('AE5 — 重复 title 弹窗二选一')
  it.todo('AE7 — §7 PAR 解除后 §3 引用断引用视觉信号')
  it.todo('AE-empty — 首次唤起引导 + + 新增 CTA')
  it.todo('R3b 批量粘贴启用时 paste-fail / partial-import 态触发')
  it.todo('R4b dirty 三按钮 + 跨设备冲突文案')
})
