<template>
  <div class="edit-listing-container">
    <h2>{{ $t('editListing.title') }}</h2>
    <!-- Loading State -->
    <div v-if="isLoading">載入中...</div>
    <!-- Error State (Initial Load) -->
    <div v-else-if="loadError">載入失敗：{{ loadError.message || '無法載入商品資料' }}</div>
    <!-- Form Display -->
    <form v-else-if="listing" @submit.prevent="handleSubmit">
      <!-- Title -->
      <div class="form-group">
        <label for="title">{{ $t('editListing.titleLabel') }}:</label>
        <input type="text" id="title" v-model="listing.title" required />
        <div v-if="validationErrors.title" class="error-message">{{ validationErrors.title }}</div>
      </div>
      <!-- Description -->
      <div class="form-group">
        <label for="description">{{ $t('editListing.descriptionLabel') }}:</label>
        <textarea id="description" v-model="listing.description" required></textarea>
        <div v-if="validationErrors.description" class="error-message">{{ validationErrors.description }}</div>
      </div>
      <!-- Make -->
      <div class="form-group">
        <label for="make">{{ $t('editListing.makeLabel') }}:</label>
        <select id="make" v-model="listing.make" required>
          <option value="" disabled>{{ $t('editListing.selectMake') }}</option>
          <option v-for="make in makes" :key="make" :value="make">{{ make }}</option>
        </select>
        <div v-if="validationErrors.make" class="error-message">{{ validationErrors.make }}</div>
      </div>
      <!-- Model -->
      <div class="form-group">
        <label for="model">{{ $t('editListing.modelLabel') }}:</label>
        <input type="text" id="model" v-model="listing.model" required />
        <div v-if="validationErrors.model" class="error-message">{{ validationErrors.model }}</div>
      </div>
      <!-- Price -->
      <div class="form-group">
        <label for="price">{{ $t('editListing.priceLabel') }}:</label>
        <input type="number" id="price" v-model.number="listing.price" required min="0" />
        <div v-if="validationErrors.price" class="error-message">{{ validationErrors.price }}</div>
      </div>
      <!-- Category -->
      <div class="form-group">
        <label for="category">{{ $t('editListing.categoryLabel') }}:</label>
        <select id="category" v-model="listing.category" required>
          <option value="" disabled>{{ $t('editListing.selectCategory') }}</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <div v-if="validationErrors.category" class="error-message">{{ validationErrors.category }}</div>
      </div>
      <!-- Type -->
      <div class="form-group">
        <label for="type">{{ $t('editListing.typeLabel') }}:</label>
        <select id="type" v-model="listing.type" required>
          <option value="" disabled>{{ $t('editListing.selectType') }}</option>
          <option v-for="type in localizedTypes" :key="type.value" :value="type.value">{{ type.label }}</option>
        </select>
        <div v-if="validationErrors.type" class="error-message">{{ validationErrors.type }}</div>
      </div>
      <!-- Tags -->
      <div class="form-group">
        <label for="tags">{{ $t('editListing.tagsLabel') }} ({{ $t('editListing.maxTags') }}):</label>
        <input type="text" id="tags" v-model="newTag" @keydown.enter.prevent="addTag" />
        <button type="button" @click="addTag" style="margin-left: 5px;">{{ $t('editListing.addTag') }}</button>
        <div v-if="validationErrors.tags" class="error-message">{{ validationErrors.tags }}</div>
        <div class="tags-container">
          <span v-for="(tag, index) in listing.tags" :key="index" class="tag">
            {{ tag.name }}
            <button type="button" @click="removeTag(index)" class="remove-tag" aria-label="Remove tag">X</button>
          </span>
        </div>
      </div>
      <!-- Submit Button -->
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? $t('editListing.submitting') : $t('editListing.submit') }}</button>
    </form>
    <!-- Success Message -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    <!-- General Error Message (for non-validation errors) -->
    <div v-if="submitError" class="error-message">
      {{ submitError }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import apiClient from '@/services/apiClient'; // Import the configured apiClient

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const listingId = route.params.id;

// State for listing data
const listing = ref({
  title: '',
  description: '',
  make: '',
  model: '',
  price: null,
  category: '',
  type: '',
  tags: [], // Store tags as objects { name: 'tagname' }
});

// State for UI control
const isLoading = ref(true);
const isSubmitting = ref(false); // Track submission state
const loadError = ref(null); // Error during initial data load
const submitError = ref(null); // General error during submission
const successMessage = ref('');
const validationErrors = ref({}); // Specific field validation errors from backend

// State for adding tags
const newTag = ref('');

// --- Static Data (Consider moving to constants file if used elsewhere) ---
const makes = ref(['CANON', 'NIKON', 'SONY', 'FUJIFILM', "OLYMPUS", "PANASONIC", "RICOH", "SIGMA", "LEICA", "HASSELBLAD", "DJI", "OTHER"]);
const categories = ref([
    'DSLR', 'MIRRORLESS', 'COMPACT', 'FILM', 'LENS', 'TRIPOD', 'ACCESSORY', 'BAG', 'OTHER'
]);

// Computed property for localized types based on i18n
const localizedTypes = computed(() => {
  return [
    { value: 'SALE', label: t('editListing.sale') },
    { value: 'WANTED', label: t('editListing.wanted') },
  ];
});
// --- End Static Data ---

// Fetch initial listing data when component mounts
onMounted(async () => {
  isLoading.value = true;
  loadError.value = null;
  try {
    // Use apiClient to fetch data. Assume fetching details is public.
    // Pass { requiresAuth: false } to prevent sending token
    const response = await apiClient.get(`/listings/${listingId}`, { requiresAuth: false });

    // Axios provides data directly in response.data
    const data = response.data;

    // Populate the listing ref with fetched data
    // Ensure tags are in the expected format { name: '...' }
    listing.value = {
      ...data, // Spread existing fields
      tags: Array.isArray(data.tags) ? data.tags.map(tag => (typeof tag === 'string' ? { name: tag } : tag)) : [], // Handle potential variations in tag format
      price: data.price // Ensure price is handled correctly (might be string from API)
    };

  } catch (err) {
    console.error("載入商品資訊失敗:", err);
    loadError.value = err; // Store the whole error object for more details if needed
  } finally {
    isLoading.value = false;
  }
});

// Handle form submission
const handleSubmit = async () => {
  isSubmitting.value = true;
  successMessage.value = '';
  submitError.value = null;
  validationErrors.value = {};

  try {
    // Prepare data for submission (ensure tags are just an array of strings or objects as backend expects)
    // Assuming backend expects an array of tag objects like { name: 'tagname' }
    const payload = {
      ...listing.value,
      tags: listing.value.tags.map(tag => ({ name: tag.name })), // Ensure correct format
      price: Number(listing.value.price) // Ensure price is a number
    };

    // Use apiClient.patch - it will automatically include the token if available (default behavior)
    const response = await apiClient.patch(`/listings/${listingId}`, payload);

    // Check if response indicates success (usually 200 OK for PATCH with body, or 204 No Content)
    if (response.status === 200 || response.status === 204) {
        successMessage.value = t('editListing.success');
        setTimeout(() => {
          router.push({ name: 'ListingDetail', params: { id: listingId } });
        }, 1000); // Redirect after 1 second
    } else {
        // This part might be less likely if Axios interceptor handles non-2xx
        submitError.value = t('editListing.failure');
    }

  } catch (err) {
    console.error('編輯商品請求錯誤:', err);
    if (err.response) {
      // Server responded with an error status (4xx, 5xx)
      const status = err.response.status;
      const errorData = err.response.data;

      if (status === 400 && typeof errorData === 'object' && errorData !== null) {
        // Handle validation errors from backend
        validationErrors.value = errorData;
        submitError.value = t('editListing.validationError'); // Set a general validation error message
      } else if (status === 401) {
        submitError.value = t('editListing.loginRequired');
        // Consider redirecting to login via interceptor or here
      } else if (status === 403) {
        submitError.value = t('editListing.permissionDenied');
      } else {
        // Other server errors
        submitError.value = (errorData && errorData.message) || t('editListing.failure');
      }
    } else if (err.request) {
      // Network error (no response received)
      submitError.value = t('editListing.networkError');
    } else {
      // Request setup error
      submitError.value = t('editListing.requestError');
    }
  } finally {
    isSubmitting.value = false;
  }
};

// Add a new tag
const addTag = () => {
  const tagName = newTag.value.trim();
  if (tagName !== '' && listing.value.tags.length < 10) {
    // Avoid adding duplicate tags
    if (!listing.value.tags.some(tag => tag.name === tagName)) {
      listing.value.tags.push({ name: tagName });
      newTag.value = ''; // Clear input
      validationErrors.value.tags = null; // Clear tag error if any
    } else {
       validationErrors.value.tags = t('editListing.duplicateTagError');
    }
  } else if (listing.value.tags.length >= 10) {
    validationErrors.value.tags = t('editListing.maxTagsError');
  }
};

// Remove a tag by index
const removeTag = (index) => {
  listing.value.tags.splice(index, 1);
};

</script>

<style scoped>
.edit-listing-container {
  max-width: 600px; /* Limit width for better readability */
  margin: 20px auto; /* Center the container */
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 1.2rem; /* Increased spacing */
  width: 100%; /* Make form groups take full width */
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #555;
}

input[type="text"],
input[type="number"],
textarea,
select {
  width: 100%;
  padding: 0.75rem; /* Increased padding */
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* Include padding and border in element's total width and height */
  font-size: 1rem;
}

textarea {
  height: 120px; /* Increased height */
  resize: vertical; /* Allow vertical resizing */
}

button[type="submit"] {
  padding: 0.75rem 1.5rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
  width: 100%; /* Make submit button full width */
  margin-top: 10px; /* Add some space above the button */
}

button[type="submit"]:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}


button[type="submit"]:hover:not(:disabled) {
  background-color: #36a374;
}

button[type="button"] { /* Style for Add Tag button */
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

button[type="button"]:hover {
  background-color: #0056b3;
}


.success-message {
  color: green;
  background-color: #e9f7ef;
  border: 1px solid #a6d7b7;
  padding: 10px;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
}

.error-message {
  color: #d9534f; /* Bootstrap danger color */
  font-size: 0.875em;
  margin-top: 0.25rem;
}

/* General submission error styling */
.edit-listing-container > .error-message {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    padding: 10px;
    border-radius: 4px;
    margin-top: 1rem;
    text-align: center;
}


.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* Increased gap */
  margin-top: 8px; /* Increased margin */
  padding: 8px;
  border: 1px solid #eee; /* Add a light border */
  border-radius: 4px;
  min-height: 40px; /* Ensure some height even when empty */
}

.tag {
  background-color: #e0e0e0;
  padding: 5px 10px;
  border-radius: 15px;
  display: inline-flex; /* Use inline-flex for better alignment */
  align-items: center;
  font-size: 0.9em;
}

.remove-tag {
  background-color: transparent;
  border: none;
  color: #dc3545; /* Bootstrap danger color */
  cursor: pointer;
  margin-left: 5px;
  padding: 0 3px;
  font-weight: bold;
  line-height: 1; /* Adjust line height */
}

.remove-tag:hover {
    color: #a02530;
}
</style>
