<template>
  <button @click="handleLogout">登出</button>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const handleLogout = () => {
  // 清除 token
  localStorage.removeItem('token');
  // 導航到登入頁面
  router.push('/login');
};
</script>
