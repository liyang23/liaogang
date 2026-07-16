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

/** 手动子项：sectionIndex → 内容 */
export interface ManualSubItems {
  [sectionIndex: number]: string
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
