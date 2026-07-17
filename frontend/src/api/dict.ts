/**
 * U9 字典管理 API 客户端 (T311)
 * 与 backend DictController (T310) 对齐
 */
import { http } from './client'

export interface Dict {
  id: number
  dictType: string
  code: string
  name: string
  description: string | null
  sortOrder: number
  disabled: number  // 0=启用, 1=停用
  createdAt: string
  updatedAt: string
}

export interface Unit {
  id: number
  code: string
  name: string
  symbol: string | null
  description: string | null
  createdAt: string
}

/** 列出字典 (按 type 过滤) */
export function listDict(type?: string) {
  const q = type ? `?type=${encodeURIComponent(type)}` : ''
  return http.get<{ code: number; msg: string; data: Dict[] }>(`/dict${q}`)
}

/** 9 预制量纲 */
export function listUnits() {
  return http.get<{ code: number; msg: string; data: Unit[] }>(`/dict/unit`)
}

/** 软删除 */
export function softDeleteDict(id: number) {
  return http.post<{ code: number; msg: string; data: string }>(`/dict/${id}/soft-delete`)
}

/** 硬删除 (引用完整性校验) */
export function hardDeleteDict(id: number) {
  return http.post<{ code: number; msg: string; data: string }>(`/dict/${id}/hard-delete`)
}

/** 恢复 */
export function undeleteDict(id: number) {
  return http.post<{ code: number; msg: string; data: string }>(`/dict/${id}/undelete`)
}
