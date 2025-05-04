<template>
  <div class="login-container">
    <h2>{{ $t('common.login') }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="username">{{ $t('login.usernameLabel') }}:</label>
        <input type="text" id="username" v-model="username" required :disabled="isSubmitting" />
        <!-- 可以根據後端響應添加更具體的驗證錯誤顯示 -->
        <!-- <div v-if="validationErrors.username" class="error-message">{{ validationErrors.username }}</div> -->
      </div>
      <div class="form-group">
        <label for="password">{{ $t('login.passwordLabel') }}:</label>
        <input type="password" id="password" v-model="password" required :disabled="isSubmitting" />
        <!-- <div v-if="validationErrors.password" class="error-message">{{ validationErrors.password }}</div> -->
      </div>
      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? $t('login.loggingIn') : $t('common.login') }}
      </button>
    </form>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <!-- Optional: Link to registration page -->
    <p class="register-link">
      {{ $t('login.noAccount') }} <router-link :to="{ name: 'register' }">{{ $t('common.register') }}</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
// --- 導入 useRoute ---
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useLoginStatus } from '../main'; // Import the reactive login status handler
import apiClient from '@/services/apiClient'; // Import the configured apiClient

const { t } = useI18n(); // Initialize i18n
const router = useRouter();
// --- 實例化 route ---
const route = useRoute(); // Get access to the current route object (for query params)
const { setLoggedIn } = useLoginStatus(); // Get the function to update login status

// Reactive state for form inputs and UI
const username = ref('');
const password = ref('');
const errorMessage = ref(''); // For general login errors
const isSubmitting = ref(false); // To disable form during submission
// const validationErrors = ref({}); // 如果後端會返回欄位錯誤，可以啟用

// Handle form submission
const handleSubmit = async () => {
  errorMessage.value = ''; // Clear previous error
  // validationErrors.value = {}; // Clear validation errors if using
  isSubmitting.value = true; // Disable form

  try {
    const response = await apiClient.post('/members/login', {
      username: username.value,
      password: password.value,
    }, { requiresAuth: false }); // No token needed for login itself

    const data = response.data;
    if (data && data.token) {
      // Login successful
      localStorage.setItem('token', data.token);
      console.log('Token set in localStorage:', data.token);

      setLoggedIn(true);
      console.log('useLoginStatus updated to true');

      // Wait for state updates before navigating
      await nextTick();
      console.log('nextTick completed');

      // --- 修改這裡：處理重定向 ---
      // 檢查路由查詢參數中是否有 'redirect'，這是由 requireAuth 守衛設置的
      const redirectPath = route.query.redirect || '/'; // Get redirect path or default to home
      console.log('Redirecting to:', redirectPath);
      router.push(redirectPath); // Navigate to the intended page or home
      // --- 重定向處理結束 ---

    } else {
      errorMessage.value = t('login.failureUnexpected');
      console.error('Login successful but no token received:', response);
    }

  } catch (error) {
    console.error('登入請求錯誤:', error);
    if (error.response) {
      const status = error.response.status;
      const errorData = error.response.data;

      // 後端可能會返回 400 (Bad Request) 或 401 (Unauthorized) 表示憑證錯誤
      if (status === 401 || status === 400) {
        // 嘗試獲取後端提供的具體錯誤訊息，否則顯示通用憑證錯誤
        errorMessage.value = (errorData && errorData.message) || t('login.failureCredentials');
        // 如果後端返回了欄位驗證錯誤 (雖然登入較少見)
        // if (typeof errorData === 'object' && errorData !== null) {
        //   validationErrors.value = errorData;
        // }
      } else {
        errorMessage.value = t('login.failureGeneric');
      }
    } else if (error.request) {
      errorMessage.value = t('common.networkError');
    } else {
      errorMessage.value = t('common.requestError');
    }
  } finally {
    isSubmitting.value = false; // Re-enable form
  }
};
</script>

<style scoped>
/* Styles remain the same */
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  max-width: 400px;
  margin: 40px auto;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
  width: 100%;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #555;
}

input[type="text"],
input[type="password"] {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 1rem;
}

input:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
}

button[type="submit"] {
  width: 100%;
  padding: 0.75rem 1.5rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
  margin-top: 10px;
}

button[type="submit"]:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button[type="submit"]:hover:not(:disabled) {
  background-color: #36a374;
}

.error-message {
  color: #d9534f;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  padding: 10px;
  border-radius: 4px;
  margin-top: 1rem;
  width: 100%;
  text-align: center;
  box-sizing: border-box;
}

.register-link {
    margin-top: 1.5rem;
    font-size: 0.9em;
    color: #555;
}

.register-link a {
    color: #007bff;
    text-decoration: none;
}

.register-link a:hover {
    text-decoration: underline;
}
</style>
