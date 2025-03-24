<template>
  <div class="add-listing-container">
    <h2>新增商品</h2>
    <form @submit.prevent="handleSubmit">
      <!-- @submit.prevent 防止頁面刷新 -->
      <div class="form-group">
        <label for="title">標題:</label>
        <input type="text" id="title" v-model="listing.title" required />
        <!-- v-model 雙向綁定標題 -->
      </div>
      <div class="form-group">
        <label for="description">描述:</label>
        <textarea id="description" v-model="listing.description" required></textarea>
        <!-- v-model 雙向綁定描述 -->
      </div>
      <div class="form-group">
        <label for="make">廠牌:</label>
        <select id="make" v-model="listing.make" required>
          <option v-for="make in makes" :key="make" :value="make">{{ make }}</option>
        </select>
        <!-- v-model 雙向綁定廠牌 -->
      </div>
      <div class="form-group">
        <label for="model">型號:</label>
        <input type="text" id="model" v-model="listing.model" required />
        <!-- v-model 雙向綁定型號 -->
      </div>
      <div class="form-group">
        <label for="price">價格:</label>
        <input type="number" id="price" v-model="listing.price" required />
        <!-- v-model 雙向綁定價格 -->
      </div>
      <div class="form-group">
        <label for="category">類別:</label>
        <select id="category" v-model="listing.category" required>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <!-- v-model 雙向綁定類別 -->
      </div>
      <div class="form-group">
        <label for="type">類型:</label>
        <select id="type" v-model="listing.type" required>
          <option v-for="type in types" :key="type" :value="type">{{ type }}</option>
        </select>
        <!-- v-model 雙向綁定類型 -->
      </div>
      <button type="submit">新增商品</button>
      <!-- 提交按鈕 -->
    </form>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
      <!-- 顯示成功訊息 -->
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
      <!-- 顯示錯誤訊息 -->
    </div>
    <!-- 顯示 seller 資訊 -->
    <div v-if="createdListing && createdListing.seller" class="seller-info">
      <h3>商品發布者資訊</h3>
      <p>發布者 ID: {{ createdListing.seller.id }}</p>
      <p>發布者名稱: {{ createdListing.seller.username }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 使用 ref 建立響應式變數，用於儲存商品資料
const listing = ref({
  title: '',
  description: '',
  make: '', // will be enum
  model: '',
  price: null,
  category: '', // will be enum
  type: '', // will be enum
});

// 用於顯示成功訊息
const successMessage = ref('');
// 用於顯示錯誤訊息
const errorMessage = ref('');
// 用於儲存後端返回的 Listing 物件
const createdListing = ref(null);

// Enum values
const makes = ref(['CANON', 'NIKON', 'SONY', 'FUJIFILM', 'PANASONIC', 'OLYMPUS', 'LEICA']);
const categories = ref(['DSLR', 'LENS', 'ACCESSORY']);
const types = ref(['SALE', 'WANTED']);

// 處理表單提交
const handleSubmit = async () => {
  // 清空成功和錯誤訊息
  successMessage.value = '';
  errorMessage.value = '';
  createdListing.value = null; // 清空 createdListing

  try {
    // 從 localStorage 取得 token
    const token = localStorage.getItem('token');
    console.log('token', token);

    // 如果沒有 token，表示使用者未登入
    if (!token) {
      errorMessage.value = '請先登入';
      console.error('使用者未登入');
      return; // 停止執行
    }

    // 發送 API 請求
    const response = await fetch('/api/listings', {
      method: 'POST', // 使用 POST 方法
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // 在 header 中加入 token
      },
      body: JSON.stringify(listing.value), // 將商品資料轉換為 JSON 字串
    });

    // 判斷 API 請求是否成功
    if (response.ok) {
      successMessage.value = '商品新增成功！';
      // 取得後端返回的 Listing 物件
      createdListing.value = await response.json();
      console.log('createdListing', createdListing.value);
      // Reset the form
      listing.value = {
        title: '',
        description: '',
        make: '',
        model: '',
        price: null,
        category: '',
        type: '',
      };
      // Optionally, redirect to login page after a delay
      setTimeout(() => {
        router.push('/login');
      }, 2000); // Redirect after 2 seconds
    } else {
      // 如果 API 請求失敗，顯示錯誤訊息
      const errorData = await response.json();
      errorMessage.value = errorData.message || '新增商品失敗。';
    }
  } catch (error) {
    // 如果發生錯誤，顯示錯誤訊息
    console.error('新增商品請求錯誤:', error);
    errorMessage.value = '新增商品請求發生錯誤，請稍後再試。';
  }
};
</script>

<style scoped>
/* 樣式保持不變 */
.add-listing-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.form-group {
  margin-bottom: 1rem;
  width: 300px;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input,
textarea,
select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

textarea {
  height: 100px;
}

button {
  padding: 0.5rem 1rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.success-message {
  color: green;
  margin-top: 1rem;
}

.error-message {
  color: red;
  margin-top: 1rem;
}
/* 新增樣式 */
.seller-info {
  margin-top: 20px;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 4px;
}
</style>
