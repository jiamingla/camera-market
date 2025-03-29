import './assets/main.css';

import { createApp, computed, ref } from 'vue'; // Import ref
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router'; // Import the router

const app = createApp(App);

app.use(createPinia());
app.use(router); // Use the router

// Create a reactive variable to hold the login status
const isLoggedInRef = ref(false); // Initialize to false

// Function to check token validity
const checkTokenValidity = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    isLoggedInRef.value = false;
    return;
  }

  try {
    // Send a request to the server to validate the token
    const response = await fetch('/api/members/validate', { // Use the new validation endpoint
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    if (response.ok) {
      isLoggedInRef.value = true;
    } else {
      // Token is invalid, remove it from localStorage
      localStorage.removeItem('token');
      isLoggedInRef.value = false;
    }
  } catch (error) {
    console.error('Error validating token:', error);
    // Handle network errors or other issues
    localStorage.removeItem('token');
    isLoggedInRef.value = false;
  }
};

// Call the function to check token validity on app startup
checkTokenValidity();

// Create a computed property to derive the login status
const loginStatus = computed(() => {
  console.log('loginStatus computed:', isLoggedInRef.value); // Add this line
  return isLoggedInRef.value;
});

// Export a function to access the computed property and the ref
export function useLoginStatus() {
  return {
    isLoggedIn: loginStatus,
    setLoggedIn: (value) => {
      isLoggedInRef.value = value;
    },
  };
}

app.mount('#app');
