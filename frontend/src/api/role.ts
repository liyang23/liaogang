/**
 * 角色 API 客户端（T206）
 *
 * <p>封装后端 RoleController 4 端点 + 权限矩阵 UI 数据结构
 * <p>13 菜单清单（plan §4.1.5 权限查找指南）
 */
import { http } from './client'

// ===== 5 操作枚举（与后端 role_permission.operation 一致） =====
export type Operation = 'READ' | 'CREATE' | 'UPDATE' | 'DELETE' | 'REVIEW'
export const OPERATIONS: { key: Operation; label: string }[] = [
  { key: 'READ', label: '查阅' },
  { key: 'CREATE', label: '新增' },
  { key: 'UPDATE', label: '更新' },
  { key: 'DELETE', label: '删除' },
  { key: 'REVIEW', label: '审核' }
]

// ===== 13 菜单清单（3 大组：主功能 5 / 治理 4 / 配置 4） =====
export interface MenuItem {
  id: string
  name: string
  group: '主功能' | '治理' | '配置'
}
export const MENU_ITEMS: MenuItem[] = [
  // 主功能（5）
  { id: 'ko-library',   name: '知识库',         group: '主功能' },
  { id: 'prompts',      name: '提示词',         group: '主功能' },
  { id: 'governance',   name: '知识治理',       group: '主功能' },
  { id: 'audit-log',    name: '审计日志',       group: '主功能' },
  { id: 'project-mgmt', name: '项目管理',       group: '主功能' },
  // 治理（4）
  { id: 'conflicts',    name: '冲突检测',       group: '治理' },
  { id: 'conflicts-resolve', name: '冲突处置',   group: '治理' },
  { id: 'governance-report', name: '治理报告',   group: '治理' },
  { id: 'snapshots',    name: '版本快照',       group: '治理' },
  // 配置（4）
  { id: 'dict-mgmt',    name: '字典管理',       group: '配置' },
  { id: 'permissions',  name: '权限与角色',     group: '配置' },
  { id: 'quant-config', name: '量化配置',       group: '配置' },
  { id: 'system',       name: '系统设置',       group: '配置' }
]

// ===== 后端统一响应包装 =====
export interface Result<T> {
  code: number
  msg: string
  data: T
}

// ===== 角色实体 =====
export interface RoleEntity {
  id: string
  code: string
  name: string
  description?: string
  isBuiltin?: number
  isDeleted?: number
  createdAt?: string
  updatedAt?: string
}

// ===== 权限矩阵 cell =====
export interface PermissionCell {
  id?: number
  roleId: string
  menuId: string
  operation: string
  allowed: boolean
}

// ===== 4 个 API 调用 =====

/** 角色列表 */
export function listRoles() {
  return http.get<Result<RoleEntity[]>>('/role')
}

/** 角色详情 */
export function getRole(code: string) {
  return http.get<Result<RoleEntity>>(`/role/${code}`)
}

/** 创建自定义角色 */
export function createRole(name: string, description?: string) {
  return http.post<Result<RoleEntity>>('/role', { name, description })
}

/** 删除角色 */
export function deleteRole(code: string) {
  return http.delete<Result<void>>(`/role/${code}`)
}

/** 查询角色的权限矩阵（按 menuId+operation 二维表） */
export function getRolePermissions(roleId: string) {
  return http.get<Result<PermissionCell[]>>(`/role/${roleId}/permissions`)
}

/** 保存角色权限矩阵（批量更新） */
export function saveRolePermissions(roleId: string, cells: PermissionCell[]) {
  return http.put<Result<void>>(`/role/${roleId}/permissions`, { cells })
}
