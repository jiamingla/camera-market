<template>
  <main>
    <h1>歡迎來到首頁</h1>
    <LogoutButton v-if="isLoggedIn" />
    <!-- Only show LogoutButton if isLoggedIn is true -->
  </main>
</template>

<script setup>
import LogoutButton from '../components/LogoutButton.vue';
import { computed } from 'vue';

// Check if a token exists in localStorage
const isLoggedIn = computed(() => {
  return localStorage.getItem('token') !== null;
});
</script>
