import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    allowedHosts: ['.ngrok-free.app', '.ngrok-free.dev'],
    proxy: {
      '/api': {
        target: 'http://localhost:8887',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8887',  
        changeOrigin: true,
        rewrite: (path) => '/api' + path,  
      },
    },
  }
})
