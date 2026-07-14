import { post, get } from './client'
import {
  saveCodeVerifier,
  getCodeVerifierFromUrl,
  clearCodeVerifier
} from '@/utils/code-verifier-storage'

/**
 * 辽港统一认证 API 客户端（v0.32 PRD OQ-23：取代原 OIDC）。
 *
 * <p>流程：
 * 1. 慧应用 SSO code + codeVerifier 提交 → POST /auth/liaogong-token
 * 2. 后端调招商云 PAAS getUserInfoByCode → 本地 user + user_role + JWT 签发
 * 3. 缓存 token + 跳转 dashboard
 *
 * <p>Mock 模式（Q-I2 未提供时）：POST /auth/mock-login
 */

export interface LiaogongTokenRequest {
  code: string
  codeVerifier: string
  platform: string // 如 'hyy'
}

export interface LiaogongTokenResponse {
  token: string
  user: {
    sub: string
    preferredUsername: string
    displayName: string
    organizationCode: string
  }
  roles: string[]
  expiresIn: number
}

export interface MockLoginRequest {
  role: string
}

export async function exchangeLiaogongToken(
  code: string,
  platform: string
): Promise<LiaogongTokenResponse> {
  const codeVerifier = getCodeVerifierFromUrl()
  if (!codeVerifier) {
    throw new Error('codeVerifier 未提供（URL + sessionStorage 兜底均失败）')
  }

  const response = await post<LiaogongTokenResponse>('/auth/liaogong-token', {
    code,
    codeVerifier,
    platform
  } as LiaogongTokenRequest)
  return response
}

export async function mockLogin(role: string): Promise<LiaogongTokenResponse> {
  return post<LiaogongTokenResponse>('/auth/mock-login', { role } as MockLoginRequest)
}

export async function logout(): Promise<void> {
  await post('/auth/logout')
  clearCodeVerifier()
}
