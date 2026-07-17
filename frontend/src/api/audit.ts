/**
 * U9 审计日志 API 客户端 (T311)
 * 与 backend AuditController (T308) 对齐
 */
import { http } from './client'

export interface AuditLog {
  id: string  // AUDIT-{YYYYMMDD}-{NNNNNN}
  action: string  // AuditAction enum 字符串
  userId: string
  targetKo: string | null
  detail: string | null
  reason: string | null
  createdAt: string
}

/** 列出审计日志 (按 userId 过滤 + 限制条数) */
export function listAuditLogs(userId?: string, limit = 50) {
  const q = userId ? `?userId=${encodeURIComponent(userId)}&limit=${limit}` : `?limit=${limit}`
  return http.get<{ code: number; msg: string; data: AuditLog[] }>(`/audit-log${q}`)
}

/** 单条详情 (OQ-11 ID 三重暴露 Modal) */
export function getAuditLog(id: string) {
  return http.get<{ code: number; msg: string; data: AuditLog }>(`/audit-log/${id}`)
}

/** 按目标 KO 查询 */
export function getAuditLogsByTargetKo(koId: string) {
  return http.get<{ code: number; msg: string; data: AuditLog[] }>(`/audit-log/by-target/${koId}`)
}
