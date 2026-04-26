import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_DEV_API_TARGET || 'http://127.0.0.1:19921'
  const wsTarget = env.VITE_DEV_WS_TARGET || 'ws://127.0.0.1:19921'

  return {
    plugins: [uni()],
    server: {
      port: 19923,
      strictPort: true,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true
        },
        '/ws': {
          target: wsTarget,
          ws: true,
          changeOrigin: true
        }
      }
    },
    preview: {
      port: 19923,
      strictPort: true
    }
  }
})
