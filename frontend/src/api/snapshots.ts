/**
 * U8 提示词快照 API 客户端 (T306)
 * 与 backend SnapshotController (T305+) 对齐
 */
import { http } from './client'

export interface PromptSnapshot {
  id: number
  hash: string
  prmId: string
  prmVersion: string
  renderedTextCanonical: string
  koIds: string[]
  koVersions: string[]
  koFieldValues: Record<string, string>
  manualSubitemsHash: string
  varBindings: Record<string, string>
  stale: boolean
  createdAt: string
  updatedAt: string
}

export interface PromptRecord {
  id: number
  snapshotHash: string
  renderedText: string
  renderTime: string
  charCount: number
  tokenCount: number
  userId: string
  reason: string | null
  forceRendered: boolean
  createdAt: string
}

/** 列出快照 (按 stale 状态筛选) */
export function listSnapshots(stale?: boolean) {
  const q = stale !== undefined ? `?stale=${stale}` : ''
  return http.get<{ code: number; msg: string; data: PromptSnapshot[] }>(`/snapshots${q}`)
}

/** 获取单个快照详情 */
export function getSnapshot(snapshotId: number) {
  return http.get<{ code: number; msg: string; data: PromptSnapshot }>(`/snapshots/${snapshotId}`)
}

/** 标记陈旧快照 (PAR 变更事件触发) */
export function markSnapshotStale(snapshotId: number) {
  return http.post<{ code: number; msg: string; data: PromptSnapshot }>(`/snapshots/${snapshotId}/stale`)
}

/** 113 KO 装配演示值重放 (OQ-16) */
export function demo113koAssemblyHash() {
  return http.get<{ code: number; msg: string; data: { hash: string } }>(`/snapshots/demo/113ko`)
}
