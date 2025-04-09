<template>
  <main>
    <h1>歡迎來到首頁</h1>
    <!-- 載入中 -->
    <div v-if="isLoading">載入中...</div>
    <!-- 載入失敗 -->
    <div v-else-if="error">載入失敗：{{ error }}</div>
    <!-- 載入成功 -->
    <div v-else>
      <!-- 有商品 -->
      <div v-if="listings.length > 0" class="listings-container">
        <router-link
          v-for="listing in listings"
          :key="listing.id"
          :to="{ name: 'ListingDetail', params: { id: listing.id } }"
          class="listing-item-link"
        >
          <div class="listing-item">
            <h3>{{ listing.title }}</h3>
            <p>廠牌: {{ listing.make }}</p>
            <p>價格: {{ listing.price }}</p>
            <p>類別: {{ listing.type }}</p>
            <p>刊登者: {{ listing.member.username }}</p>
            <p>最後編輯時間: {{ listing.lastUpdateTime }}</p>
          </div>
        </router-link>
      </div>
      <!-- 沒有商品 -->
      <div v-else>目前沒有商品</div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 商品列表資料
const listings = ref([]);
// 是否正在載入中
const isLoading = ref(true);
// 錯誤訊息
const error = ref(null);

// 元件掛載後執行
onMounted(async () => {
  try {
    // 發送 GET 請求到 /api/listings 取得商品列表
    const response = await fetch('/api/listings');
    // 如果請求失敗，拋出錯誤
    if (!response.ok) {
      throw new Error('無法載入商品列表');
    }
    // 將回應的 JSON 資料轉換為 JavaScript 物件，並賦值給 listings
    listings.value = await response.json();
  } catch (err) {
    // 捕獲錯誤，並將錯誤訊息賦值給 error
    error.value = err.message;
  } finally {
    // 無論成功或失敗，都將 isLoading 設為 false
    isLoading.value = false;
  }
});
</script>

<style scoped>
/* 商品列表容器 */
.listings-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); /* 自動調整列數，每列最小 250px */
  gap: 1rem; /* 間距 */
}

/* 商品項目 */
.listing-item {
  border: 1px solid #ccc; /* 邊框 */
  padding: 1rem; /* 內邊距 */
  border-radius: 4px; /* 圓角 */
  cursor: pointer; /* Add cursor pointer */
  transition: background-color 0.3s ease; /* Add transition for hover effect */
}

.listing-item:hover {
  background-color: #f0f0f0; /* Add hover effect */
}

.listing-item-link {
  text-decoration: none; /* Remove underline from link */
  color: inherit; /* Inherit color from parent */
  display: block; /* Make the link a block element */
}
</style>
