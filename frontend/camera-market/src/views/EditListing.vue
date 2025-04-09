<template>
  <div class="edit-listing-container">
    <h2>{{ $t('editListing.title') }}</h2>
    <div v-if="isLoading">載入中...</div>
    <div v-else-if="error">載入失敗：{{ error }}</div>
    <form v-else @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="title">{{ $t('editListing.titleLabel') }}:</label>
        <input type="text" id="title" v-model="listing.title" required />
        <div v-if="errors.title" class="error-message">{{ errors.title }}</div>
      </div>
      <div class="form-group">
        <label for="description">{{ $t('editListing.descriptionLabel') }}:</label>
        <textarea id="description" v-model="listing.description" required></textarea>
        <div v-if="errors.description" class="error-message">{{ errors.description }}</div>
      </div>
      <div class="form-group">
        <label for="make">{{ $t('editListing.makeLabel') }}:</label>
        <select id="make" v-model="listing.make" required>
          <option value="" disabled>{{ $t('editListing.selectMake') }}</option>
          <option v-for="make in makes" :key="make" :value="make">{{ make }}</option>
        </select>
        <div v-if="errors.make" class="error-message">{{ errors.make }}</div>
      </div>
      <div class="form-group">
        <label for="model">{{ $t('editListing.modelLabel') }}:</label>
        <input type="text" id="model" v-model="listing.model" required />
        <div v-if="errors.model" class="error-message">{{ errors.model }}</div>
      </div>
      <div class="form-group">
        <label for="price">{{ $t('editListing.priceLabel') }}:</label>
        <input type="number" id="price" v-model="listing.price" required />
        <div v-if="errors.price" class="error-message">{{ errors.price }}</div>
      </div>
      <div class="form-group">
        <label for="category">{{ $t('editListing.categoryLabel') }}:</label>
        <select id="category" v-model="listing.category" required>
          <option value="" disabled>{{ $t('editListing.selectCategory') }}</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <div v-if="errors.category" class="error-message">{{ errors.category }}</div>
      </div>
      <div class="form-group">
        <label for="type">{{ $t('editListing.typeLabel') }}:</label>
        <select id="type" v-model="listing.type" required>
          <option value="" disabled>{{ $t('editListing.selectType') }}</option>
          <option v-for="type in localizedTypes" :key="type.value" :value="type.value">{{ type.label }}</option>
        </select>
        <div v-if="errors.type" class="error-message">{{ errors.type }}</div>
      </div>
      <div class="form-group">
        <label for="tags">{{ $t('editListing.tagsLabel') }} ({{ $t('editListing.maxTags') }}):</label>
        <input type="text" id="tags" v-model="newTag" @keyup.enter="addTag" />
        <button type="button" @click="addTag">{{ $t('editListing.addTag') }}</button>
        <div v-if="errors.tags" class="error-message">{{ errors.tags }}</div>
        <div class="tags-container">
          <span v-for="(tag, index) in listing.tags" :key="index" class="tag">
            {{ tag.name }}
            <button type="button" @click="removeTag(index)" class="remove-tag">X</button>
          </span>
        </div>
      </div>
      <button type="submit">{{ $t('editListing.submit') }}</button>
    </form>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const listingId = route.params.id;

const listing = ref({
  title: '',
  description: '',
  make: '',
  model: '',
  price: null,
  category: '',
  type: '',
  tags: [],
});
const isLoading = ref(true);
const error = ref(null);
const successMessage = ref('');
const errorMessage = ref('');
const errors = ref({});
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

// Localized types
const localizedTypes = computed(() => {
  return [
    { value: 'SALE', label: t('editListing.sale') },
    { value: 'WANTED', label: t('editListing.wanted') },
  ];
});

onMounted(async () => {
  try {
    const response = await fetch(`/api/listings/${listingId}`);
    if (!response.ok) {
      throw new Error('無法載入商品資訊');
    }
    const data = await response.json();
    // Map the tags to the format required by the component
    listing.value = {
      ...data,
      tags: data.tags.map(tag => ({ name: tag.name })),
    };
  } catch (err) {
    error.value = err.message;
  } finally {
    isLoading.value = false;
  }
});

const handleSubmit = async () => {
  successMessage.value = '';
  errorMessage.value = '';
  errors.value = {};

  try {
    const token = localStorage.getItem('token');
    if (!token) {
      errorMessage.value = t('editListing.loginRequired');
      return;
    }

    const transformedListing = {
      ...listing.value,
      tags: listing.value.tags.map(tag => ({ name: tag.name })),
    };

    const response = await fetch(`/api/listings/${listingId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(transformedListing),
    });

    if (response.ok) {
      successMessage.value = t('editListing.success');
      setTimeout(() => {
        router.push({ name: 'ListingDetail', params: { id: listingId } });
      }, 1000);
    } else {
      const errorData = await response.json();
      if (response.status === 400) {
        errors.value = errorData;
      } else {
        errorMessage.value = errorData.message || t('editListing.failure');
      }
    }
  } catch (err) {
    console.error('編輯商品請求錯誤:', err);
    errorMessage.value = t('editListing.requestError');
  }
};

const addTag = () => {
  if (newTag.value.trim() !== '' && listing.value.tags.length < 10) {
    listing.value.tags.push({ name: newTag.value.trim() });
    newTag.value = '';
    errors.value.tags = null;
  } else if (listing.value.tags.length >= 10) {
    errors.value.tags = t('editListing.maxTagsError');
  }
};

const removeTag = (index) => {
  listing.value.tags.splice(index, 1);
};
</script>

<style scoped>
.edit-listing-container {
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
