/**
 * U9 项目管理 API 客户端 (T311)
 * 与 backend ProjectController (T309) 对齐
 */
import { http } from './client'

export interface Project {
  id: string
  code: string
  name: string
  description: string | null
  organizationCode: string | null
  status: 'active' | 'archived'
  isDeleted: number
  createdAt: string
  updatedAt: string
  archivedAt: string | null
  archivedBy: string | null
}

/** 列出所有 active 项目 (ProjectSwitcher 默认列表) */
export function listActiveProjects() {
  return http.get<{ code: number; msg: string; data: Project[] }>('/project')
}

/** 列出所有项目 (含 archived, ProjectSwitcher 折叠态) */
export function listAllProjects() {
  return http.get<{ code: number; msg: string; data: Project[] }>('/project?includeArchived=true')
}

/** 归档项目 (OQ-7) */
export function archiveProject(id: string) {
  return http.post<{ code: number; msg: string; data: string }>(`/project/${id}/archive`)
}

/** 激活项目 */
export function activateProject(id: string) {
  return http.post<{ code: number; msg: string; data: string }>(`/project/${id}/activate`)
}
