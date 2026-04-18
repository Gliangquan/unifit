import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue'

export default defineConfig({
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
        target: 'http://localhost:19921',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
      '/ws': {
        target: 'ws://localhost:19921',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});