import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 路由守卫：基于 5 预置角色默认权限矩阵（v0.32 §4.1）
// 实际权限校验在 U5 实施时接入后端 /api/auth/me 接口
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { requiresRole: ['ROLE-0001', 'ROLE-0002', 'ROLE-0003', 'ROLE-0004', 'ROLE-0005'] }
      },
      {
        path: 'ko-library',
        name: 'ko-library',
        component: () => import('@/views/ko-library/KoLibraryView.vue')
      },
      {
        path: 'ko-:type',
        name: 'ko-type-list',
        component: () => import('@/views/ko-library/KoTypeListView.vue'),
        props: true
      },
      {
        path: 'ko-:type/:id',
        name: 'ko-detail',
        component: () => import('@/views/ko-library/KoDetailView.vue'),
        props: true
      },
      {
        path: 'prompts',
        name: 'prompts',
        component: () => import('@/views/prompts/PromptsView.vue')
      },
      {
        path: 'composer/:id?',
        name: 'composer',
        component: () => import('@/views/prompts/ComposerView.vue'),
        props: true
      },
      {
        path: 'snapshots/:id?',
        name: 'snapshots',
        component: () => import('@/views/prompts/SnapshotsView.vue'),
        props: true
      },
      {
        path: 'conflicts',
        name: 'conflicts',
        component: () => import('@/views/conflicts/ConflictsView.vue'),
        meta: { requiresRole: ['ROLE-0001', 'ROLE-0002', 'ROLE-0003'] }
      },
      {
        path: 'audit-log',
        name: 'audit-log',
        component: () => import('@/views/audit/AuditLogView.vue'),
        meta: { requiresRole: ['ROLE-0001', 'ROLE-0002'] }
      },
      {
        path: 'project-mgmt',
        name: 'project-mgmt',
        component: () => import('@/views/project/ProjectMgmtView.vue'),
        meta: { requiresRole: ['ROLE-0001'] }
      },
      {
        path: 'dict-mgmt',
        name: 'dict-mgmt',
        component: () => import('@/views/dict/DictMgmtView.vue'),
        meta: { requiresRole: ['ROLE-0001'] }
      },
      {
        path: 'permissions',
        name: 'permissions',
        component: () => import('@/views/permissions/PermissionsView.vue'),
        meta: { requiresRole: ['ROLE-0001'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转 /login；无权限跳转 403
// 实际角色与权限校验由 OQ-12 决定：角色变更下次登录生效（OIDC/JWT 缓存中携带 roles claim）
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth !== false && !auth.isAuthenticated) {
    // Sprint 1 mock 模式：未配置时自动以 ROLE-0001（系统管理员）登录
    if (!auth.token) {
      auth.mockLoginAs('ROLE-0001')
    }
  }
  if (to.meta.requiresRole && !to.meta.requiresRole.includes(auth.currentRole)) {
    return { name: 'forbidden' }
  }
  return true
})

// 辅助函数：避免循环依赖
function useAuthStore() {
  // 动态 import 避免在 router 加载阶段触发 Pinia
  const { useAuthStore } = require('@/stores/auth')
  return useAuthStore()
}

export default router
