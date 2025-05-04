<template>
  <button @click="handleLogout">{{ $t('common.logout') }}</button> <!-- 使用 i18n -->
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n'; // 導入 useI18n
import { useLoginStatus } from '../main'; // Import the reactive variable

const router = useRouter();
const { t } = useI18n(); // 初始化 i18n
const { setLoggedIn } = useLoginStatus(); // 獲取更新狀態的函數

const handleLogout = () => {
  // 清除 localStorage 中的 token
  localStorage.removeItem('token');
  console.log('Token removed from localStorage');

  // 更新全局登入狀態
  setLoggedIn(false);
  console.log('useLoginStatus updated to false');

  // 重新導向到首頁
  // 可以在這裡添加一個 nextTick 確保狀態更新後再跳轉，雖然通常不是必須的
  // import { nextTick } from 'vue';
  // nextTick(() => {
  //   router.push('/');
  // });
  router.push('/');
};
</script>

<style scoped>
/* 可以添加一些按鈕樣式 */
button {
  padding: 8px 15px;
  background-color: #dc3545; /* Bootstrap danger color */
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #c82333; /* Darker danger color */
}
</style>
