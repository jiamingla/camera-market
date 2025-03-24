import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue'; // Import the Register component
import AddListing from '../components/AddListing.vue';
import { useLoginStatus } from '../main'; // Import the reactive variable

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/register', // Add a new route for registration
      name: 'register',
      component: Register, // Use the Register component
    },
    {
      path: '/add-listing',
      name: 'addListing',
      component: AddListing,
      beforeEnter: (to, from, next) => {
        // Check if the user is logged in
        if (useLoginStatus().isLoggedIn) {
          next(); // Allow access
        } else {
          next('/login'); // Redirect to login
        }
      },
    }
  ],
});

export default router;
