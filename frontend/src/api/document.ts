/**
 * U10 文档预览 API 客户端 (T314)
 * 与 backend DocPreviewController (T313) 对齐
 */
import { http } from './client'

export interface DocPreview {
  url: string  // 30 分钟签名 token URL
  strategy: 'PDFJS_DIRECT' | 'PDFJS_CONVERTED' | 'TEXT_DIRECT' | 'IMAGE_DIRECT' | 'UNSUPPORTED'
}

/** 获取文档预览 (6 格式分发) */
export function getDocPreview(koId: string) {
  return http.get<{ code: number; msg: string; data: DocPreview }>(`/doc/${koId}/preview`)
}

/** 获取文件 (签名 token URL) */
export function getDocFile(koId: string, token: string) {
  return http.get<{ code: number; msg: string; data: string }>(
    `/doc/${koId}/file?token=${encodeURIComponent(token)}`,
  )
}
