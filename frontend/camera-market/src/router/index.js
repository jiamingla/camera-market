import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import ListingDetail from '../views/ListingDetail.vue';
import EditListing from '../views/EditListing.vue';
import ProfileView from '../views/ProfileView.vue'; // 假設你創建了一個 ProfileView.vue

// 建議將這些移到 views/ 資料夾
import Login from '../views/Login.vue'; // 或 '../views/Login.vue'
import Register from '../views/Register.vue'; // 或 '../views/Register.vue'
import AddListing from '../views/AddListing.vue'; // 或 '../views/AddListing.vue'

import { useLoginStatus } from '../main'; // Import the reactive variable

// 路由守衛函數 (提取出來方便複用)
const requireAuth = (to, from, next) => {
  // 檢查登入狀態
  if (useLoginStatus().isLoggedIn.value) { // 訪問 .value
    next(); // 已登入，允許訪問
  } else {
    // 未登入，重定向到登入頁面
    // 可以選擇性地保存用戶原本想去的頁面，以便登入後跳轉回來
    next({ name: 'login', query: { redirect: to.fullPath } });
  }
};

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 這個通常不用改
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
      // 可選：如果用戶已登入，訪問登入頁時直接跳轉到首頁
      beforeEnter: (to, from, next) => {
        if (useLoginStatus().isLoggedIn.value) {
          next({ name: 'home' });
        } else {
          next();
        }
      },
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
       // 可選：如果用戶已登入，訪問註冊頁時直接跳轉到首頁
       beforeEnter: (to, from, next) => {
        if (useLoginStatus().isLoggedIn.value) {
          next({ name: 'home' });
        } else {
          next();
        }
      },
    },
    {
      path: '/listings/:id',
      name: 'ListingDetail',
      component: ListingDetail,
      props: true,
    },
    {
      path: '/add-listing',
      name: 'addListing',
      component: AddListing,
      beforeEnter: requireAuth, // *** 使用統一的守衛函數 ***
    },
    {
      path: '/listings/:id/edit',
      name: 'EditListing',
      component: EditListing,
      props: true,
      beforeEnter: requireAuth, // *** 添加守衛 ***
    },
    {
      path: '/profile', // *** 新增 Profile 路由 ***
      name: 'profile',
      component: ProfileView, // 確保創建了這個組件
      beforeEnter: requireAuth, // *** 添加守衛 ***
    }
    // 可以考慮添加一個 404 Not Found 路由
    // { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
  ],
});

// --- 可選：全局後置守衛 ---
// 可以在每次路由切換後執行一些操作，例如更新頁面標題
// router.afterEach((to, from) => {
//   const baseTitle = '相機市集'; // 你的應用名稱
//   document.title = to.meta.title ? `${t(to.meta.title)} - ${baseTitle}` : baseTitle;
// });

export default router;
