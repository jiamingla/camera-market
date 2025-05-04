import './assets/main.css';

import { createApp, ref, readonly } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { createI18n } from 'vue-i18n';
import zhTW from './locales/zh-TW.json';
import en from './locales/en.json';
import apiClient from '@/services/apiClient';

console.log('Imported zhTW object:', JSON.stringify(zhTW, null, 2)); // 你確認這個是完整的

// --- i18n Setup ---
const i18n = createI18n({
  locale: 'zh-TW',
  fallbackLocale: 'en',
  messages: { 'zh-TW': zhTW, 'en': en }, // 傳入導入的物件
  legacy: false,
  datetimeFormats: {
    'zh-TW': {
      short: { year: 'numeric', month: 'numeric', day: 'numeric' },
      long: { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric' }
    },
    'en': {
      short: { year: 'numeric', month: 'numeric', day: 'numeric' },
      long: { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric' }
    }
  }
});

// --- Vue App Initialization ---
const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(i18n); // 使用 i18n 實例

// --- 新增的檢查 ---
console.log('i18n instance locale:', i18n.global.locale.value); // 再次確認 locale
// 檢查 i18n 實例內部實際加載的 messages
// 注意：訪問內部 messages 可能因 vue-i18n 版本而異，但 .value 通常適用於 Composition API
console.log('i18n internal messages for zh-TW:', JSON.stringify(i18n.global.messages.value['zh-TW'], null, 2));
// 嘗試直接從實例的 messages 中獲取值
const appNameFromMessages = i18n.global.messages.value['zh-TW']?.common?.appName;
console.log('Value directly from i18n messages:', appNameFromMessages);
// --------------------

console.log('Result of t("common.appName"):', i18n.global.t('common.appName')); // 你看到這個輸出鍵名

// --- Reactive Authentication State ---
// ... (後續代碼不變) ...
const isLoggedInRef = ref(false);
const currentUserIdRef = ref(null);

function setAuthState(loggedIn, userId = null) {
  isLoggedInRef.value = loggedIn;
  currentUserIdRef.value = loggedIn ? userId : null;
  console.log(`Auth state updated: isLoggedIn=${isLoggedInRef.value}, userId=${currentUserIdRef.value}`);
}

async function checkInitialAuthStatus() {
  // ... (代碼不變) ...
  const token = localStorage.getItem('token');
  if (!token) {
    console.log('No token found on startup.');
    setAuthState(false);
    return;
  }
  console.log('Token found, validating...');
  try {
    const response = await apiClient.get('/members/profile'); // Changed to /profile
    if (response.data && response.data.id) {
      console.log('Token validation successful. User data:', response.data);
      setAuthState(true, response.data.id);
    } else {
      console.warn('Token validation endpoint returned success, but user data (ID) is missing.');
      localStorage.removeItem('token');
      setAuthState(false);
    }
  } catch (error) {
    console.error('Token validation failed:', error.response?.status || error.message);
    localStorage.removeItem('token');
    setAuthState(false);
  }
}

export function useLoginStatus() {
  return {
    isLoggedIn: readonly(isLoggedInRef),
    currentUserId: readonly(currentUserIdRef),
    setLoggedIn: setAuthState,
  };
}

// --- Mount App AFTER Auth Check AND Router is Ready ---
checkInitialAuthStatus().then(() => {
  router.isReady().then(() => { // Wait for router to be ready
  app.mount('#app');
  console.log('Application mounted after initial authentication check.');
  });
});
