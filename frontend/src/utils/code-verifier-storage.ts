/**
 * codeVerifier storage utility（v0.32 PRD OQ-21 #11 + OQ-23 集成）。
 *
 * <p>codeVerifier 兜底逻辑：URL 参数缺失时从 sessionStorage 缓存读取，
 * 带过期时间（默认 5 分钟 = 300 秒，OQ-23 修订）。
 */
const STORAGE_KEY = 'hyy_codeVerifier'
const STORAGE_EXPIRY_KEY = 'hyy_codeVerifier_expiry'
const TTL_SECONDS = 300

export function getCodeVerifierFromUrl(): string | null {
  if (typeof window === 'undefined') return null

  // 1. 优先从 URL 参数读取
  const url = new URL(window.location.href)
  const urlCodeVerifier = url.searchParams.get('codeVerifier')
  if (urlCodeVerifier) {
    // 同时写入 sessionStorage（带过期时间）
    saveCodeVerifier(urlCodeVerifier)
    return urlCodeVerifier
  }

  // 2. 兜底从 sessionStorage 缓存读取
  return loadCodeVerifier()
}

export function saveCodeVerifier(codeVerifier: string): void {
  if (typeof window === 'undefined') return
  // F-10 修复：先 setItem expiry 再 setItem codeVerifier（避免崩溃时缓存泄露无过期）
  sessionStorage.setItem(
    STORAGE_EXPIRY_KEY,
    String(Date.now() + TTL_SECONDS * 1000)
  )
  sessionStorage.setItem(STORAGE_KEY, codeVerifier)
}

export function loadCodeVerifier(): string | null {
  if (typeof window === 'undefined') return null
  const stored = sessionStorage.getItem(STORAGE_KEY)
  const expiry = sessionStorage.getItem(STORAGE_EXPIRY_KEY)
  if (!stored || !expiry) return null
  if (Date.now() > parseInt(expiry)) {
    clearCodeVerifier()
    return null
  }
  return stored
}

export function clearCodeVerifier(): void {
  if (typeof window === 'undefined') return
  sessionStorage.removeItem(STORAGE_KEY)
  sessionStorage.removeItem(STORAGE_EXPIRY_KEY)
}
