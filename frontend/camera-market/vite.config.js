import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';


// https://vitejs.dev/config/
// proxy 設定只影響本地開發 (npm run dev)，與 Firebase Hosting 的部署無關。
export default defineConfig({
  plugins: [vue(),tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'https://camera-api-267117213180.asia-east1.run.app', // 後端 API 的地址
        changeOrigin: true, // 必須設定為 true，才能正確地跨域
        rewrite: (path) => path.replace(/^\/api/, '/api'), // 將 /api 重寫為 /api
      },
    },
  },
});
