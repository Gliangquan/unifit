import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_DEV_API_TARGET || 'http://127.0.0.1:19921'
  const wsTarget = env.VITE_DEV_WS_TARGET || 'ws://127.0.0.1:19921'

  return {
    plugins: [vue()],
    define: {
      global: 'window',
      process: 'window.process',
      Buffer: 'window.Buffer',
    },
    server: {
      port: 19922,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api'),
        },
        '/ws': {
          target: wsTarget,
          ws: true,
          changeOrigin: true,
        },
      },
    },
  }
});
