<template>
  <button @click="handleLogout">登出</button>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useLoginStatus } from '../main'; // Import the reactive variable

const router = useRouter();

const handleLogout = () => {
  // 清除 localStorage 中的 token
  localStorage.removeItem('token');
  console.log('Token removed from localStorage'); // Add this line

  // Update the reactive variable to trigger a re-render
  useLoginStatus().setLoggedIn(false); // Update the ref
  console.log('useLoginStatus updated to false'); // Add this line

  // 重新導向到首頁
  router.push('/');
};
</script>
