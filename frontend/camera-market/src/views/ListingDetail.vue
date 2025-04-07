<template>
  <main>
    <h1>商品詳細資訊</h1>
    <div v-if="isLoading">載入中...</div>
    <div v-else-if="error">載入失敗：{{ error }}</div>
    <div v-else-if="listing">
      <h3>{{ listing.title }}</h3>
      <p>廠牌: {{ listing.make }}</p>
      <p>價格: {{ listing.price }}</p>
      <p>類別: {{ listing.category }}</p>
      <p>描述: {{ listing.description }}</p>
      <p>圖片: {{ listing.images }}</p>
      <p>狀態: {{ listing.status }}</p>
      <p>刊登者: {{ listing.member.username }}</p>
      <p>最後編輯時間: {{ listing.lastUpdateTime }}</p>
      <p>標籤: {{ listing.tags }}</p>

      <div class="actions">
        <button @click="editListing" class="edit-button">編輯</button>
        <button @click="deleteListing" class="delete-button">刪除</button>
      </div>
    </div>
    <div v-else>找不到商品</div>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
      <!-- 顯示成功訊息 -->
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
      <!-- 顯示錯誤訊息 -->
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// 使用 ref 建立響應式變數，用於儲存商品資料
const route = useRoute();
const router = useRouter();
const listingId = route.params.id;

// 用於顯示成功訊息
const successMessage = ref('');
// 用於顯示錯誤訊息
const errorMessage = ref('');

// 商品資料

const listing = ref(null);
const isLoading = ref(true);
const error = ref(null);

onMounted(async () => {
  try {
    const response = await fetch(`/api/listings/${listingId}`);
    if (!response.ok) {
      throw new Error('無法載入商品資訊');
    }
    listing.value = await response.json();
  } catch (err) {
    error.value = err.message;
  } finally {
    isLoading.value = false;
  }
});

const editListing = () => {
  // Navigate to the edit page, passing the listing ID
  router.push({ name: 'EditListing', params: { id: listingId } });
};

const deleteListing = async () => {
  if (confirm(t('deleteListing.confirm'))) {
    try {
      const token = localStorage.getItem('token'); // 假設 token 存在 localStorage 中
      if (!token) {
        throw new Error('使用者未登入');
      }
      const response = await fetch(`/api/listings/${listingId}`, {
        method: 'DELETE',
        headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // 在 header 中加入 token
      },
      });
      if (!(response.status === 204)) {
        errorMessage.value = t('deleteListing.failure');

        throw new Error('刪除商品失敗');
      }
      successMessage.value = t('deleteListing.success');

      setTimeout(() => {
        router.push('/');
      }, 1000);
    } catch (err) {
      error.value = err.message;
    }
  }
};
</script>

<style scoped>
.actions {
  margin-top: 20px;
}

.edit-button,
.delete-button {
  padding: 10px 15px;
  margin-right: 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.edit-button {
  background-color: #4CAF50; /* Green */
  color: white;
}

.delete-button {
  background-color: #f44336; /* Red */
  color: white;
}
</style>
