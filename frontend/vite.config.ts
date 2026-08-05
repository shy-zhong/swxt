import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8887',  // 转发到 Spring Boot
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8887',  // 图片统一由后端提供
        changeOrigin: true,
        rewrite: (path) => '/api' + path,  // 后端 context-path 为 /api
      },
    },
  }
})
