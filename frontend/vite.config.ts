import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端 API 前缀（v0.32 应用服务为 context-path: /api）
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    target: 'es2015',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'utils': ['axios', 'marked', 'dompurify', 'highlight.js']
        }
      }
    }
  }
})
