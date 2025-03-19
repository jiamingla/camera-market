<template>
  <div class="login-container">
    <h2>會員登入</h2>
    <form @submit.prevent="handleSubmit">
      <!-- 使用 @submit.prevent 防止頁面刷新 -->
      <div class="form-group">
        <label for="username">使用者名稱/電子郵件:</label>
        <input type="text" id="username" v-model="username" required />
        <!-- v-model 雙向綁定 username 資料 -->
      </div>
      <div class="form-group">
        <label for="password">密碼:</label>
        <input type="password" id="password" v-model="password" required />
        <!-- v-model 雙向綁定 password 資料 -->
      </div>
      <button type="submit">登入</button>
    </form>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'; // Import nextTick
import { useRouter } from 'vue-router';
import { useLoginStatus } from '../main'; // Import the reactive variable

// 使用 ref 建立響應式變數
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

// 處理表單提交
const handleSubmit = async () => {
  errorMessage.value = ''; // 清空錯誤訊息
  // 在這裡，我們將模擬一個登入請求
  try {
    // 假設後端 API 端點是 /api/login
    const response = await fetch('/api/members/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username: username.value, password: password.value }),
    });

    if (response.ok) {
      // 登入成功
      const data = await response.json();
      // 儲存 token 到 localStorage 或 cookie
      localStorage.setItem('token', data.token);
      console.log('Token set in localStorage:', data.token); // Add this line

      // Update the reactive variable to trigger a re-render
      useLoginStatus().setLoggedIn(true); // Update the ref
      console.log('useLoginStatus updated to true'); // Add this line

      // Wait for the next tick before navigating
      await nextTick();
      console.log('nextTick completed'); // Add this line
      // 導航到首頁或其他頁面
      router.push('/');
    } else {
      // 登入失敗
      const errorData = await response.json();
      errorMessage.value = errorData.message || '登入失敗，請檢查您的使用者名稱和密碼。';
    }
  } catch (error) {
    console.error('登入請求錯誤:', error);
    errorMessage.value = '登入請求發生錯誤，請稍後再試。';
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100%;
}

.form-group {
  margin-bottom: 1rem;
  width: 300px;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  padding: 0.5rem 1rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.error-message {
  color: red;
  margin-top: 1rem;
}
</style>
