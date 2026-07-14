import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
// F-15 修复：通过 router.push 跳转而非 window.location.href（保留 Pinia 状态）
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

/**
 * Axios HTTP 客户端（v0.32 OQ-23：辽港统一认证取代 OIDC 后端）
 *
 * <p>请求拦截器：自动附加 Authorization Bearer token
 * <p>响应拦截器：401 跳转登录 + Toast 错误提示
 */

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const http: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动附加 Authorization
http.interceptors.request.use(
  (config) => {
    const auth = useAuthStore()
    if (auth.token) {
      config.headers.Authorization = `Bearer ${auth.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：401 跳转登录 + 错误提示
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      const msg = error.response?.data?.msg || error.message || '请求失败'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

// 便捷方法
export function get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return http.get<T, T>(url, config) as unknown as Promise<T>
}

export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return http.post<T, T>(url, data, config) as unknown as Promise<T>
}

export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return http.put<T, T>(url, data, config) as unknown as Promise<T>
}

export function del<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return http.delete<T, T>(url, config) as unknown as Promise<T>
}
