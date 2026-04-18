import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 19923,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:19921',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:19921',
        ws: true,
        changeOrigin: true
      }
    }
  },
  preview: {
    port: 19923,
    strictPort: true
  }
})
