import './assets/main.css';

import { createApp, computed, ref } from 'vue'; // Import ref
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router'; // Import the router

const app = createApp(App);

app.use(createPinia());
app.use(router); // Use the router

// Create a reactive variable to hold the login status
const isLoggedInRef = ref(localStorage.getItem('token') !== null);

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
