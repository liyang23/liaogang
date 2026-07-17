/**
 * PRM 三栏组装器 API 客户端（T210）
 *
 * <p>封装后端 PrmController 3 端点 + 组装器上下文数据结构
 * <p>PRP 装配数 OQ-16：selectedKOs + varBindings + manualSubItems
 */
import { http } from './client'

// ===== 后端统一响应包装 =====
export interface Result<T> {
  code: number
  msg: string
  data: T
}

// ===== PRM 模板 + Section（与后端 PrmTemplateEntity / PrmSectionEntity 对应） =====
export interface PrmTemplate {
  id: string
  name: string
  description?: string
  version?: string
  createdAt?: string
  updatedAt?: string
}

export interface PrmSection {
  id?: number
  templateId: string
  sectionIndex: number
  title: string
  /** FIXED（变量赋值型）/ DYNAMIC（动态选择型） */
  sectionType: 'FIXED' | 'DYNAMIC'
  content: string
  createdAt?: string
}

// ===== 装配器上下文 =====
/** 变量绑定：sectionIndex → { varKey → koId } */
export interface VarBindings {
  [sectionIndex: number]: { [varKey: string]: string }
}

/** 手动子项单条结构（Q-I4 §3 弹层 + ManualSubItems 类型迁移落地后形态）*/
export interface ManualSubItem {
  title: string
  content: string
  value?: number
  unit?: string
  lower_bound?: number
  upper_bound?: number
  range_type?: string
}

/** 手动子项：sectionIndex → 内容（U3 完成后统一为 List 形态；U2/U3 过渡期 union 形态）*/
export type ManualSubItems = {
  [sectionIndex: number]: string | ManualSubItem[]
}

/** 把单个手动子项数组扁平化拼接为字符串（U3 完成后可移除该过渡工具）*/
export function manualSubItemsToString(items: ManualSubItem[]): string {
  return items.map((it) => `${it.title}\n${it.content}`).join('\n---\n')
}

/** 把字符串拆为单条结构（仅在 U2/U3 过渡期使用；U3 完成后移除）*/
export function manualSubItemsFromString(content: string): ManualSubItem[] {
  if (!content.trim()) return []
  return content
    .split(/\n---\n/)
    .map((block) => {
      const [title, ...rest] = block.split('\n')
      return { title: (title || '').trim(), content: rest.join('\n').trim() }
    })
    .filter((it) => it.title || it.content)
}

/** 取某段手动子项的数组形态（统一在 ManualSubItem[] 范型）*/
export function getManualSubItemList(items: ManualSubItems[string], sectionIndex: number): ManualSubItem[] {
  if (Array.isArray(items)) return items
  if (typeof items === 'string') return manualSubItemsFromString(items)
  return []
}

/** 选中的 KO：sectionIndex → [koId, koId, ...] */
export interface SelectedKOs {
  [sectionIndex: number]: string[]
}

export interface ComposerContext {
  templateId: string
  varBindings: VarBindings
  manualSubItems: ManualSubItems
  selectedKOs: SelectedKOs
}

// ===== 渲染结果（含字符数 + token 估算） =====
export interface RenderResult {
  /** 渲染后内容（处理后含 {{var}} 替换或字面量） */
  rendered: string
  /** 字符数（g(M) 函数估算） */
  charCount: number
  /** token 数（g(M) 函数估算，约 2 chars/token） */
  tokenCount: number
  /** 实际装配数 OQ-16 = selectedKOs.length + varBindings.size + manualSubItems.size */
  assemblyCount: number
}

// ===== 4 个 API 调用 =====

/** PRM 模板列表（GET /api/prm）*/
export function listPrmTemplates() {
  return http.get<Result<PrmTemplate[]>>('/prm')
}

/** PRM 模板详情（含 sections，GET /api/prm/{id}）*/
export function getPrmTemplate(id: string) {
  return http.get<Result<{ template: PrmTemplate; sections: PrmSection[] }>>(`/prm/${id}`)
}

/** 渲染组装结果（GET /api/composer/render 或 POST，T210+ 实施） */
export function renderComposer(context: ComposerContext) {
  return http.post<Result<RenderResult>>('/composer/render', context)
}

/** 字符数 + token 数本地估算（前端快速反馈，NFR-03 ≤ g(M) ms） */
export function estimateTokens(text: string): { charCount: number; tokenCount: number } {
  const charCount = text.length
  // 2 chars/token 保守估算（含中英文混合）
  const tokenCount = Math.ceil(charCount / 2)
  return { charCount, tokenCount }
}
