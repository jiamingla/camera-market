<template>
  <div class="add-listing-container">
    <h2>{{ $t('addListing.title') }}</h2>
    <form @submit.prevent="handleSubmit">
      <!-- @submit.prevent 防止頁面刷新 -->
      <div class="form-group">
        <label for="title">{{ $t('addListing.titleLabel') }}:</label>
        <input type="text" id="title" v-model="listing.title" required />
        <div v-if="errors.title" class="error-message">{{ errors.title }}</div>
        <!-- v-model 雙向綁定標題 -->
      </div>
      <div class="form-group">
        <label for="description">{{ $t('addListing.descriptionLabel') }}:</label>
        <textarea id="description" v-model="listing.description" required></textarea>
        <div v-if="errors.description" class="error-message">{{ errors.description }}</div>
        <!-- v-model 雙向綁定描述 -->
      </div>
      <div class="form-group">
        <label for="make">{{ $t('addListing.makeLabel') }}:</label>
        <select id="make" v-model="listing.make" required>
          <option value="" disabled>{{ $t('addListing.selectMake') }}</option>
          <option v-for="make in makes" :key="make" :value="make">{{ make }}</option>
        </select>
        <div v-if="errors.make" class="error-message">{{ errors.make }}</div>
        <!-- v-model 雙向綁定廠牌 -->
      </div>
      <div class="form-group">
        <label for="model">{{ $t('addListing.modelLabel') }}:</label>
        <input type="text" id="model" v-model="listing.model" required />
        <div v-if="errors.model" class="error-message">{{ errors.model }}</div>
        <!-- v-model 雙向綁定型號 -->
      </div>
      <div class="form-group">
        <label for="price">{{ $t('addListing.priceLabel') }}:</label>
        <input type="number" id="price" v-model="listing.price" required />
        <div v-if="errors.price" class="error-message">{{ errors.price }}</div>
        <!-- v-model 雙向綁定價格 -->
      </div>
      <div class="form-group">
        <label for="category">{{ $t('addListing.categoryLabel') }}:</label>
        <select id="category" v-model="listing.category" required>
          <option value="" disabled>{{ $t('addListing.selectCategory') }}</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <div v-if="errors.category" class="error-message">{{ errors.category }}</div>
        <!-- v-model 雙向綁定類別 -->
      </div>
      <div class="form-group">
        <label for="type">{{ $t('addListing.typeLabel') }}:</label>
        <select id="type" v-model="listing.type" required>
          <option value="" disabled>{{ $t('addListing.selectType') }}</option>
          <option v-for="type in localizedTypes" :key="type.value" :value="type.value">{{ type.label }}</option>
        </select>
        <div v-if="errors.type" class="error-message">{{ errors.type }}</div>
        <!-- v-model 雙向綁定類型 -->
      </div>
      <div class="form-group">
        <label for="tags">{{ $t('addListing.tagsLabel') }} ({{ $t('addListing.maxTags') }}):</label>
        <input type="text" id="tags" v-model="newTag" @keyup.enter="addTag" />
        <button type="button" @click="addTag">{{ $t('addListing.addTag') }}</button>
        <div v-if="errors.tags" class="error-message">{{ errors.tags }}</div>
        <div class="tags-container">
          <span v-for="(tag, index) in listing.tags" :key="index" class="tag">
            {{ tag.name }}
            <button type="button" @click="removeTag(index)" class="remove-tag">X</button>
          </span>
        </div>
      </div>
      <button type="submit">{{ $t('addListing.submit') }}</button>
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
      <h3>{{ $t('addListing.sellerInfo') }}</h3>
      <p>{{ $t('addListing.sellerId') }}: {{ createdListing.seller.id }}</p>
      <p>{{ $t('addListing.sellerName') }}: {{ createdListing.seller.username }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const { t } = useI18n();

// 使用 ref 建立響應式變數，用於儲存商品資料
const listing = ref({
  title: '',
  description: '',
  make: '', // will be enum
  model: '',
  price: null,
  category: '', // will be enum
  type: '', // will be enum
  tags: [], // 新增 tags 陣列, 每個 tag 是一個 { name: 'tag name' } 物件
});

// 用於顯示成功訊息
const successMessage = ref('');
// 用於顯示錯誤訊息
const errorMessage = ref('');
// 用於儲存後端返回的 Listing 物件
const createdListing = ref(null);
// 用於儲存錯誤訊息
const errors = ref({});
// 用於新增標籤
const newTag = ref('');

// Enum values
const makes = ref(['CANON', 'NIKON', 'SONY', 'FUJIFILM', "OLYMPUS", "PANASONIC", "RICOH", "SIGMA", "LEICA", "HASSELBLAD", "DJI", "OTHER"]);
const categories = ref([
    'DSLR', // 單眼相機
    'MIRRORLESS', // 無反相機
    'COMPACT', // 輕便相機
    'FILM', // 底片相機
    'LENS', // 鏡頭
    'TRIPOD', // 腳架
    'ACCESSORY', // 配件
    'BAG', // 相機包
    'OTHER', // 其他
    ]);
// const types = ref(['SALE', 'WANTED']); // Removed the original types

// Localized types
const localizedTypes = computed(() => {
  return [
    { value: 'SALE', label: t('addListing.sale') },
    { value: 'WANTED', label: t('addListing.wanted') },
  ];
});

// 處理表單提交
const handleSubmit = async () => {
  // 清空成功和錯誤訊息
  successMessage.value = '';
  errorMessage.value = '';
  createdListing.value = null; // 清空 createdListing
  errors.value = {}; // 清空錯誤訊息

  try {
    // 從 localStorage 取得 token
    const token = localStorage.getItem('token');
    console.log('token', token);

    // 如果沒有 token，表示使用者未登入
    if (!token) {
      errorMessage.value = t('addListing.loginRequired');
      console.error('使用者未登入');
      return; // 停止執行
    }


    // Transform tags to the required format
    const transformedListing = {
      ...listing.value,
      tags: listing.value.tags.map(tag => ({ name: tag.name })),
    };

    // console.log('transformedListing', JSON.stringify(transformedListing));
    // 發送 API 請求
    const response = await fetch('/api/listings', {
      method: 'POST', // 使用 POST 方法
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // 在 header 中加入 token
      },
      body: JSON.stringify(transformedListing), // 將商品資料轉換為 JSON 字串
    });

    // 判斷 API 請求是否成功
    if (response.ok) {
      successMessage.value = t('addListing.success');
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
        tags: [],
      };
      // Optionally, redirect to login page after a delay
      setTimeout(() => {
        router.push('/login');
      }, 2000); // Redirect after 2 seconds
    } else {
      // 如果 API 請求失敗，顯示錯誤訊息
      const errorData = await response.json();
      if (response.status === 400) {
        errors.value = errorData;
      } else {
        errorMessage.value = errorData.message || t('addListing.failure');
      }
    }
  } catch (error) {
    // 如果發生錯誤，顯示錯誤訊息
    console.error('新增商品請求錯誤:', error);
    errorMessage.value = t('addListing.requestError');
  }
};

// 新增標籤
const addTag = () => {
  if (newTag.value.trim() !== '' && listing.value.tags.length < 10) {
    listing.value.tags.push({ name: newTag.value.trim() }); // Push an object with a name property
    newTag.value = '';
    errors.value.tags = null;
  } else if (listing.value.tags.length >= 10) {
    errors.value.tags = t('addListing.maxTagsError');
  }
};

// 移除標籤
const removeTag = (index) => {
  listing.value.tags.splice(index, 1);
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
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 5px;
}

.tag {
  background-color: #e0e0e0;
  padding: 5px 10px;
  border-radius: 15px;
  display: flex;
  align-items: center;
}

.remove-tag {
  background-color: transparent;
  border: none;
  color: red;
  cursor: pointer;
  margin-left: 5px;
  padding: 0;
}
</style>
