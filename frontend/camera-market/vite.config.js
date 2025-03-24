import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 後端 API 的地址
        changeOrigin: true, // 必須設定為 true，才能正確地跨域
        rewrite: (path) => path.replace(/^\/api/, '/api'), // 將 /api 重寫為 /api
      },
    },
  },
});
