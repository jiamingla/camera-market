<template>
  <div class="app-container">
    <header class="app-header">
      <NavigationBar />
    </header>
    <main class="app-main">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import NavigationBar from './components/NavigationBar.vue';
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.app-main {
  flex-grow: 1;
  display: flex; /* Add flex display to center the content-wrapper */
  justify-content: center; /* Center the content-wrapper horizontally */
  align-items: center; /* Center the content-wrapper vertically */
  padding: 20px;
}

.content-wrapper {
  width: 100%; /* Take up full width */
  max-width: 1200px; /* Set a maximum width for the content */
  margin: 0 auto; /* Center the content horizontally */
}
</style>
