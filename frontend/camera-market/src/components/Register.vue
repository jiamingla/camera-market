<template>
  <div class="register-container">
    <h2>會員註冊</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="username">使用者名稱:</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div class="form-group">
        <label for="email">電子郵件:</label>
        <input type="email" id="email" v-model="email" required />
      </div>
      <div class="form-group">
        <label for="password">密碼:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit">註冊</button>
    </form>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const username = ref('');
const email = ref('');
const password = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const router = useRouter();

const handleSubmit = async () => {
  errorMessage.value = '';
  successMessage.value = '';
  try {
    const response = await fetch('/api/members/register', { // Replace with your actual API endpoint
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username: username.value, password: password.value, email: email.value }),
    });

    if (response.ok) {
      // Registration successful
      const data = await response.json();
      successMessage.value = data.message || '註冊成功！';
      // Optionally, redirect to login page after a delay
      setTimeout(() => {
        router.push('/login');
      }, 2000); // Redirect after 2 seconds
    } else {
      // Registration failed
      const errorData = await response.json();
      errorMessage.value = errorData.message || '註冊失敗，請檢查您的資料。';
    }
  } catch (error) {
    console.error('註冊請求錯誤:', error);
    errorMessage.value = '註冊請求發生錯誤，請稍後再試。';
  }
};
</script>

<style scoped>
.register-container {
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

.success-message {
  color: green;
  margin-top: 1rem;
}
</style>
