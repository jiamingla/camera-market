<template>
  <div class="register-container">
    <h2>{{ $t('common.register') }}</h2> <!-- Use common key -->
    <form @submit.prevent="handleSubmit">
      <!-- Username -->
      <div class="form-group">
        <label for="username">{{ $t('register.usernameLabel') }}:</label>
        <input type="text" id="username" v-model="username" required :disabled="isSubmitting" />
        <div v-if="validationErrors.username" class="error-message">{{ validationErrors.username }}</div>
      </div>
      <!-- Email -->
      <div class="form-group">
        <label for="email">{{ $t('register.emailLabel') }}:</label>
        <input type="email" id="email" v-model="email" required :disabled="isSubmitting" />
        <div v-if="validationErrors.email" class="error-message">{{ validationErrors.email }}</div>
      </div>
      <!-- Password -->
      <div class="form-group">
        <label for="password">{{ $t('register.passwordLabel') }}:</label>
        <input type="password" id="password" v-model="password" required :disabled="isSubmitting" />
        <div v-if="validationErrors.password" class="error-message">{{ validationErrors.password }}</div>
      </div>
      <!-- Submit Button -->
      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? $t('register.submitting') : $t('common.register') }}
      </button>
    </form>
    <!-- General Submission Error -->
    <div v-if="submitError" class="error-message general-error">
      {{ submitError }}
    </div>
    <!-- Success Message -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    <!-- Link to Login -->
    <p class="login-link">
      {{ $t('register.alreadyHaveAccount') }} <router-link :to="{ name: 'login' }">{{ $t('common.login') }}</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import apiClient from '@/services/apiClient'; // Import the configured apiClient

const { t } = useI18n();
const router = useRouter();

// Form data
const username = ref('');
const email = ref('');
const password = ref('');

// UI State
const isSubmitting = ref(false);
const submitError = ref(''); // General error message for submission
const successMessage = ref('');
const validationErrors = ref({}); // Field-specific validation errors

// Handle form submission
const handleSubmit = async () => {
  submitError.value = '';
  successMessage.value = '';
  validationErrors.value = {};
  isSubmitting.value = true;

  try {
    // Use apiClient.post for registration
    // Registration usually doesn't require an existing token
    const response = await apiClient.post('/members/register', {
      username: username.value,
      email: email.value,
      password: password.value,
    }, { requiresAuth: false }); // Explicitly state no auth needed

    // Check for successful creation (HTTP 201 Created or 200 OK)
    if (response.status === 201 || response.status === 200) {
      successMessage.value = t('register.success');
      // Clear form fields
      username.value = '';
      email.value = '';
      password.value = '';
      // Redirect to login page after a delay
      setTimeout(() => {
        router.push({ name: 'Login' }); // Use named route
      }, 2000); // Redirect after 2 seconds
    } else {
      // Handle unexpected success status codes if necessary
      submitError.value = `${t('register.failureGeneric')} (Status: ${response.status})`;
    }

  } catch (error) {
    console.error('註冊請求錯誤:', error);
    if (error.response) {
      // Server responded with an error status (4xx, 5xx)
      const status = error.response.status;
      const errorData = error.response.data;

      if (status === 400 && typeof errorData === 'object' && errorData !== null) {
        // Handle validation errors from backend
        validationErrors.value = errorData;
        submitError.value = t('register.failureValidation');
      } else if (status === 409) { // 409 Conflict is common for existing username/email
        submitError.value = (errorData && errorData.message) || t('register.failureConflict'); // Add this key
      } else {
        // Other server errors
        submitError.value = (errorData && errorData.message) || t('register.failureGeneric');
      }
    } else if (error.request) {
      // Network error
      submitError.value = t('common.networkError');
    } else {
      // Request setup error
      submitError.value = t('common.requestError');
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
/* Reusing similar styles from Login.vue for consistency */
.register-container {
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
input[type="email"],
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

/* Field-specific validation error */
.form-group .error-message {
  color: #d9534f;
  font-size: 0.875em;
  margin-top: 0.25rem;
}

/* General submission error */
.error-message.general-error {
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

.success-message {
  color: green;
  background-color: #e9f7ef;
  border: 1px solid #a6d7b7;
  padding: 10px;
  border-radius: 4px;
  margin-top: 1rem;
  width: 100%;
  text-align: center;
  box-sizing: border-box;
}

.login-link {
    margin-top: 1.5rem;
    font-size: 0.9em;
    color: #555;
}

.login-link a {
    color: #007bff;
    text-decoration: none;
}

.login-link a:hover {
    text-decoration: underline;
}
</style>
