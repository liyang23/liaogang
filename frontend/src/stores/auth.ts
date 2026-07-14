import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 鉴权 Pinia store（OQ-23：辽港统一认证取代 OIDC）
 *
 * <p>流程：
 * 1. 慧应用 SSO 跳转：URL 携带 code + codeVerifier + platform
 * 2. 前端调用 /api/v1/auth/liaogong-token 换取本地 JWT
 * 3. 缓存 JWT + sub + preferred_username + 角色列表（来自本地 user_role 表）
 * 4. 角色变更下次登录生效（OQ-12 修订）
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('km_token') || '')
  const userSub = ref<string>(localStorage.getItem('km_user_sub') || '')
  const preferredUsername = ref<string>(localStorage.getItem('km_preferred_username') || '')
  const displayName = ref<string>(localStorage.getItem('km_display_name') || '')
  const currentRole = ref<string>(localStorage.getItem('km_current_role') || '')
  const organizationCode = ref<string>(localStorage.getItem('km_org_code') || '')

  // 派生
  const isAuthenticated = computed(() => !!token.value)
  const userInfo = computed(() => ({
    sub: userSub.value,
    preferredUsername: preferredUsername.value,
    displayName: displayName.value,
    organizationCode: organizationCode.value
  }))

  // 动作
  function setAuth(payload: {
    token: string
    sub: string
    preferredUsername: string
    displayName: string
    role: string
    organizationCode: string
  }) {
    token.value = payload.token
    userSub.value = payload.sub
    preferredUsername.value = payload.preferredUsername
    displayName.value = payload.displayName
    currentRole.value = payload.role
    organizationCode.value = payload.organizationCode

    localStorage.setItem('km_token', payload.token)
    localStorage.setItem('km_user_sub', payload.sub)
    localStorage.setItem('km_preferred_username', payload.preferredUsername)
    localStorage.setItem('km_display_name', payload.displayName)
    localStorage.setItem('km_current_role', payload.role)
    localStorage.setItem('km_org_code', payload.organizationCode)
  }

  function logout() {
    token.value = ''
    userSub.value = ''
    preferredUsername.value = ''
    displayName.value = ''
    currentRole.value = ''
    organizationCode.value = ''
    localStorage.removeItem('km_token')
    localStorage.removeItem('km_user_sub')
    localStorage.removeItem('km_preferred_username')
    localStorage.removeItem('km_display_name')
    localStorage.removeItem('km_current_role')
    localStorage.removeItem('km_org_code')
  }

  // Sprint 1 mock 模式：未配置时以指定角色登录
  function mockLoginAs(role: string) {
    setAuth({
      token: 'mock-jwt-' + role,
      sub: 'mock-sub-' + role,
      preferredUsername: 'mock_' + role.toLowerCase(),
      displayName: 'Mock ' + role,
      role,
      organizationCode: 'MOCK-ORG'
    })
  }

  return {
    token,
    userSub,
    preferredUsername,
    displayName,
    currentRole,
    organizationCode,
    isAuthenticated,
    userInfo,
    setAuth,
    logout,
    mockLoginAs
  }
})
