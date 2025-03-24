<template>
  <nav>
    <ul>
      <li><router-link to="/">首頁</router-link></li>
      <li v-if="isLoggedIn"><router-link to="/add-listing">刊登</router-link></li>
      <li v-if="!isLoggedIn"><router-link to="/login">登入</router-link></li>
      <li v-if="!isLoggedIn"><router-link to="/register">註冊</router-link></li>
      <li v-if="isLoggedIn"><button @click="logout">登出</button></li>
    </ul>
  </nav>
</template>

<script setup>
import { useLoginStatus } from '../main';
import { useRouter } from 'vue-router';

const router = useRouter();
const { isLoggedIn, setLoggedIn } = useLoginStatus();

const logout = () => {
  localStorage.removeItem('token');
  setLoggedIn(false);
  router.push('/login');
};
</script>

<style scoped>
nav {
  background-color: #f0f0f0;
  padding: 10px;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
}

li {
  margin-right: 20px;
}

a {
  text-decoration: none;
  color: #333;
}
</style>
