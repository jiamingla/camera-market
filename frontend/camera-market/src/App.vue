<template>
  <header>
    <img alt="Vue logo" class="logo" src="./assets/logo.svg" width="125" height="125" />
    <div class="wrapper">
      <RouterLink to="/">Home</RouterLink>
      <!-- 如果沒有登入，顯示登入和註冊按鈕 -->
      <template v-if="!isLoggedIn">
        <RouterLink to="/login">登入</RouterLink>
        <RouterLink to="/register">註冊</RouterLink>
      </template>
      <!-- 如果已登入，顯示登出按鈕 -->
      <LogoutButton v-else />
    </div>
  </header>
  <RouterView />
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router';
import { computed, watch } from 'vue';
import LogoutButton from './components/LogoutButton.vue';
import { useLoginStatus } from './main'; // Import the reactive variable

// 檢查是否有 token，判斷是否已登入
const isLoggedIn = computed(() => {
  console.log('isLoggedIn computed:', useLoginStatus().isLoggedIn.value); // Add this line
  return useLoginStatus().isLoggedIn.value;
});

// Watch for changes in the global login status
watch(useLoginStatus().isLoggedIn, () => {
  // This will trigger a re-render when the login status changes
  console.log('Login status changed:', isLoggedIn.value);
});
</script>
