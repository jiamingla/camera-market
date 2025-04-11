// src/services/apiClient.js
import axios from 'axios';
// 可以在這裡導入你的 router 實例，用於 401 跳轉 (如果需要)
// import router from '@/router'; // 確保路徑正確

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // 可以設定超時時間 (毫秒)
  // timeout: 10000,
});

apiClient.interceptors.request.use(
  (config) => {
    // 檢查請求配置中是否有自訂的 'requiresAuth' 標記
    // 預設情況下，我們認為請求需要認證 (requiresAuth !== false)
    const requiresAuth = config.requiresAuth !== false; // 如果沒設定或設為 true，則需要認證

    if (requiresAuth) {
      const token = localStorage.getItem('token'); // 從 localStorage 獲取 token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      } else {
        // 如果需要認證但沒有 token，可以選擇在這裡處理
        // 例如：取消請求或拋出錯誤
        console.warn('請求需要認證，但未找到 token');
        // return Promise.reject(new Error('請求需要認證，但未找到 token'));
      }
    }
    // 如果 requiresAuth 為 false，則不添加 Authorization 標頭

    return config; // 返回修改後的配置
  },
  (error) => {
    console.error('Request Interceptor Error:', error);
    return Promise.reject(error);
  }
);

// 4. (可選) 添加回應攔截器 (Response Interceptor)
//    例如：統一處理某些錯誤狀態碼
apiClient.interceptors.response.use(
  (response) => {
    // 狀態碼在 2xx 範圍內會觸發此函數
    // 直接返回回應數據部分
    return response; // 或者 return response.data; 如果你總是只需要 data
  },
  (error) => {
    // 超出 2xx 範圍的狀態碼都會觸發此函數
    console.error('Response Interceptor Error:', error.response || error.message);

    // 在這裡可以做全局錯誤處理，例如：
    if (error.response) {
      // 伺服器返回了錯誤狀態碼
      const status = error.response.status;
      if (status === 401) {
        console.error('未授權 (401)，可能需要重新登入或刷新 token');
        // 清除過期的 token
        localStorage.removeItem('token');
        // 更新登入狀態 (如果你的 useLoginStatus 是全局響應式的)
        // import { useLoginStatus } from '../main'; // 可能需要在頂部導入
        // const { updateLoginStatus } = useLoginStatus();
        // updateLoginStatus(false);
        // 跳轉到登入頁面
        // router.push('/login'); // 需要導入 router
      } else if (status === 403) {
        console.error('權限不足 (403)');
      } else if (status === 404) {
        console.error('找不到資源 (404)');
      }
    } else if (error.request) {
      console.error('網路錯誤，無回應');
    } else {
      console.error('請求設置錯誤:', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
