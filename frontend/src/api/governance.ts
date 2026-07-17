/**
 * U7 知识治理 API 客户端 (T303)
 * 与 backend GovernanceController 对齐
 */
import { http } from './client'

export interface ConflictEntity {
  id: number
  fingerprint: string
  conflictType: 'C1' | 'C2' | 'C3' | 'C4' | 'C5' | 'C6' | 'H2'
  koAId: string
  koBId: string
  scopeKey: string
  fieldKey: string
  status: 'pending' | 'reviewing' | 'resolved' | 'auto_resolved'
  confidence: number | null
  llmSuggestion: string | null
  llmRationale: string | null
  resolutionState: 'draft' | 'review' | 'approved' | 'published'
  createdAt: string
  updatedAt: string
  resolvedAt: string | null
}

export interface LlmSuggestion {
  suggestion: string
  confidence: number
  rationale: string
  reasoning?: string
}

export interface QuotaStatus {
  platformUsed: number
  platformLimit: number
  userUsed: number
  userLimit: number
}

// 列表冲突 (按状态筛选)
export function listConflicts(status?: string) {
  const q = status ? `?status=${status}` : ''
  return http.get<{ code: number; msg: string; data: ConflictEntity[] }>(`/governance/conflict${q}`)
}

// 调 LLM 建议
export function suggestConflict(conflictId: number, userSub: string) {
  return http.post<{ code: number; msg: string; data: LlmSuggestion }>(
    `/governance/conflict/${conflictId}/suggest?userSub=${encodeURIComponent(userSub)}`,
  )
}

// 仲裁 (OQ-8 4 状态一次性完成)
export function arbitrateConflict(conflictId: number, userSub: string) {
  return http.post<{ code: number; msg: string; data: ConflictEntity }>(
    `/governance/conflict/${conflictId}/arbitrate?userSub=${encodeURIComponent(userSub)}`,
  )
}

// 治理报告
export function getReport() {
  return http.get<{ code: number; msg: string; data: { format: string; rows: number } }>(
    '/governance/report',
  )
}
