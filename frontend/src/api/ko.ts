/**
 * KO 库 API 客户端（T203）
 *
 * <p>封装后端 6 个端点（POST /api/ko / GET /api/ko/{id} / GET /api/ko / GET /api/ko/search / PUT /api/ko/{id} / DELETE /api/ko/{id}）
 * <p>类型定义（TypeScript interface）与后端 ko/dto/*.java DTO 字段一一对应
 */
import { http } from './client'

// ===== 6 类型 KO 中文名映射（与后端 KoService.TYPE_NAMES 一致） =====
export const KO_TYPE_NAMES: Record<string, string> = {
  CON: '约束',
  RUL: '规则',
  PAR: '参数',
  SCH: '数据结构',
  PRM: '提示词模板',
  DOC: '文档'
}

export const ALL_KO_TYPES = Object.keys(KO_TYPE_NAMES)

// ===== 后端统一响应包装 =====
export interface Result<T> {
  code: number
  msg: string
  data: T
}

// ===== KO Entity（与后端 KoEntity 一致） =====
export interface KoEntity {
  id?: string
  type: string
  title: string
  code: string
  projectId: string
  definition?: string
  effect?: string
  level?: string
  organization?: string
  status?: string
  version?: string
  isDeleted?: number
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

// ===== 列表项 DTO =====
export interface KoListItem {
  id: string
  type: string
  typeName: string
  title: string
  version: string
  status: string
  projectId: string
  updatedAt: string
}

// ===== 详情 DTO（含 references） =====
export interface KoReference {
  id: number
  targetKoId: string
  relationType: string
  description?: string
}

export interface KoDetail extends KoListItem {
  code: string
  definition?: string
  effect?: string
  level?: string
  organization?: string
  createdBy?: string
  createdAt: string
  references?: KoReference[]
}

// ===== 搜索结果 DTO =====
export interface KoSearchResult {
  id: string
  type: string
  typeName: string
  title: string
  projectId: string
  matchedField: string
}

// ===== 分页响应 =====
export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
}

// ===== 6 个 API 调用 =====

/** 创建 KO（POST /api/ko） */
export function createKo(ko: Partial<KoEntity>) {
  return http.post<Result<KoEntity>>('/ko', ko)
}

/** 按 ID 查询（GET /api/ko/{id}） */
export function getKo(id: string, projectId?: string) {
  return http.get<Result<KoDetail>>(`/ko/${id}`, {
    headers: projectId ? { 'X-Project-Id': projectId } : {}
  })
}

/** 列表（GET /api/ko） */
export function listKo(params: {
  type?: string
  projectId?: string
  status?: string
  page?: number
  size?: number
}) {
  return http.get<Result<Page<KoListItem>>>('/ko', { params })
}

/** 跨类搜索（GET /api/ko/search） */
export function searchKo(params: {
  query: string
  types?: string[]
  projectId?: string
}) {
  return http.get<Result<KoSearchResult[]>>('/ko/search', {
    params: { query: params.query, types: params.types, projectId: params.projectId }
  })
}

/** 更新 KO（PUT /api/ko/{id}） */
export function updateKo(id: string, ko: Partial<KoEntity>, projectId?: string) {
  return http.put<Result<KoEntity>>(`/ko/${id}`, ko, {
    headers: projectId ? { 'X-Project-Id': projectId } : {}
  })
}

/** 软删除（DELETE /api/ko/{id}） */
export function deleteKo(id: string, projectId?: string) {
  return http.delete<Result<void>>(`/ko/${id}`, {
    headers: projectId ? { 'X-Project-Id': projectId } : {}
  })
}
