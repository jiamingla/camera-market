<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="nav-logo">{{ $t('common.appName') }}</router-link> <!-- Added App Name -->
      <ul class="nav-menu">
        <li class="nav-item">
          <router-link to="/" class="nav-link">{{ $t('common.home') }}</router-link>
        </li>
        <li class="nav-item" v-if="isLoggedIn">
          <router-link to="/add-listing" class="nav-link">{{ $t('common.addListing') }}</router-link> <!-- Changed key -->
        </li>
        <!-- Add Profile Link if logged in -->
        <li class="nav-item" v-if="isLoggedIn">
           <router-link to="/profile" class="nav-link">{{ $t('common.profile') }}</router-link> <!-- Added Profile Link -->
        </li>
        <li class="nav-item" v-if="!isLoggedIn">
          <router-link to="/login" class="nav-link">{{ $t('common.login') }}</router-link>
        </li>
        <li class="nav-item" v-if="!isLoggedIn">
          <router-link to="/register" class="nav-link">{{ $t('common.register') }}</router-link>
        </li>
        <li class="nav-item" v-if="isLoggedIn">
          <!-- Using a button styled as a link for semantic correctness -->
          <button @click="handleLogout" class="nav-link logout-button">{{ $t('common.logout') }}</button>
        </li>
      </ul>
      <!-- Optional: Language Switcher -->
      <!-- <LocaleSwitcher /> -->
    </div>
  </nav>
</template>

<script setup>
import { useI18n } from 'vue-i18n';
import { useLoginStatus } from '../main'; // Import the reactive login status handler
import { useRouter } from 'vue-router';
// Optional: Import LocaleSwitcher if you have one
// import LocaleSwitcher from './LocaleSwitcher.vue';

const { t } = useI18n(); // Initialize i18n
const router = useRouter();
const { isLoggedIn, setLoggedIn } = useLoginStatus(); // Get reactive state and setter

// Logout handler - consistent with LogoutButton.vue
const handleLogout = () => {
  localStorage.removeItem('token');
  setLoggedIn(false);
  // Redirect to home page after logout
  router.push('/');
};
</script>

<style scoped>
.navbar {
  background-color: #f8f9fa; /* Light grey background */
  padding: 0.5rem 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #e7e7e7;
}

.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px; /* Limit width */
  margin: 0 auto; /* Center container */
}

.nav-logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-menu {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center; /* Align items vertically */
}

.nav-item {
  margin-left: 1.5rem; /* Spacing between items */
}

.nav-link {
  text-decoration: none;
  color: #555;
  padding: 0.5rem 0; /* Add some padding for easier clicking */
  transition: color 0.3s ease;
  font-size: 1rem;
}

.nav-link:hover,
.nav-link.router-link-active, /* Style for active link */
.nav-link.router-link-exact-active {
  color: #007bff; /* Highlight color */
  border-bottom: 2px solid #007bff; /* Underline active link */
}

/* Style the logout button to look like a link */
.logout-button {
  background: none;
  border: none;
  padding: 0.5rem 0; /* Match link padding */
  margin: 0;
  color: #dc3545; /* Danger color for logout */
  cursor: pointer;
  font-size: 1rem; /* Match link font size */
  font-family: inherit; /* Inherit font */
  transition: color 0.3s ease;
}

.logout-button:hover {
  color: #c82333; /* Darker danger color */
  text-decoration: underline;
}
</style>
