<template>
  <div class="add-listing-container">
    <h2>{{ $t('addListing.title') }}</h2>
    <form @submit.prevent="handleSubmit">
      <!-- Title -->
      <div class="form-group">
        <label for="title">{{ $t('addListing.titleLabel') }}:</label>
        <input type="text" id="title" v-model="listing.title" required />
        <div v-if="validationErrors.title" class="error-message">{{ validationErrors.title }}</div>
      </div>
      <!-- Description -->
      <div class="form-group">
        <label for="description">{{ $t('addListing.descriptionLabel') }}:</label>
        <textarea id="description" v-model="listing.description" required></textarea>
        <div v-if="validationErrors.description" class="error-message">{{ validationErrors.description }}</div>
      </div>
      <!-- Make -->
      <div class="form-group">
        <label for="make">{{ $t('addListing.makeLabel') }}:</label>
        <select id="make" v-model="listing.make" required>
          <option value="" disabled>{{ $t('addListing.selectMake') }}</option>
          <option v-for="make in makes" :key="make" :value="make">{{ make }}</option>
        </select>
        <div v-if="validationErrors.make" class="error-message">{{ validationErrors.make }}</div>
      </div>
      <!-- Model -->
      <div class="form-group">
        <label for="model">{{ $t('addListing.modelLabel') }}:</label>
        <input type="text" id="model" v-model="listing.model" required />
        <div v-if="validationErrors.model" class="error-message">{{ validationErrors.model }}</div>
      </div>
      <!-- Price -->
      <div class="form-group">
        <label for="price">{{ $t('addListing.priceLabel') }}:</label>
        <input type="number" id="price" v-model.number="listing.price" required min="0" />
        <div v-if="validationErrors.price" class="error-message">{{ validationErrors.price }}</div>
      </div>
      <!-- Category -->
      <div class="form-group">
        <label for="category">{{ $t('addListing.categoryLabel') }}:</label>
        <select id="category" v-model="listing.category" required>
          <option value="" disabled>{{ $t('addListing.selectCategory') }}</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <div v-if="validationErrors.category" class="error-message">{{ validationErrors.category }}</div>
      </div>
      <!-- Type -->
      <div class="form-group">
        <label for="type">{{ $t('addListing.typeLabel') }}:</label>
        <select id="type" v-model="listing.type" required>
          <option value="" disabled>{{ $t('addListing.selectType') }}</option>
          <option v-for="type in localizedTypes" :key="type.value" :value="type.value">{{ type.label }}</option>
        </select>
        <div v-if="validationErrors.type" class="error-message">{{ validationErrors.type }}</div>
      </div>
      <!-- Tags -->
      <div class="form-group">
        <label for="tags">{{ $t('addListing.tagsLabel') }} ({{ $t('addListing.maxTags') }}):</label>
        <input type="text" id="tags" v-model="newTag" @keydown.enter.prevent="addTag" />
        <button type="button" @click="addTag" style="margin-left: 5px;">{{ $t('addListing.addTag') }}</button>
        <div v-if="validationErrors.tags" class="error-message">{{ validationErrors.tags }}</div>
        <div class="tags-container">
          <span v-for="(tag, index) in listing.tags" :key="index" class="tag">
            {{ tag.name }}
            <button type="button" @click="removeTag(index)" class="remove-tag" :aria-label="$t('editListing.removeTagAriaLabel')">X</button> <!-- Borrowed aria-label from edit -->
          </span>
        </div>
      </div>
      <!-- Submit Button -->
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? $t('addListing.submitting') : $t('addListing.submit') }}</button>
    </form>
    <!-- Success Message -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    <!-- General Error Message (for non-validation errors) -->
    <div v-if="submitError" class="error-message">
      {{ submitError }}
    </div>
    <!-- Display created listing info (optional) -->
    <!-- <div v-if="createdListing && createdListing.seller" class="seller-info">
      <h3>{{ $t('addListing.sellerInfo') }}</h3>
      <p>{{ $t('addListing.sellerId') }}: {{ createdListing.seller.id }}</p>
      <p>{{ $t('addListing.sellerName') }}: {{ createdListing.seller.username }}</p>
    </div> -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import apiClient from '@/services/apiClient'; // Import the configured apiClient

const router = useRouter();
const { t } = useI18n();

// Reactive state for the form data
const listing = ref({
  title: '',
  description: '',
  make: '',
  model: '',
  price: null,
  category: '',
  type: '',
  tags: [], // Array of objects: { name: 'tag name' }
});

// UI control state
const isSubmitting = ref(false);
const successMessage = ref('');
const submitError = ref(null); // General submission error message
const validationErrors = ref({}); // Field-specific validation errors

// Tag input state
const newTag = ref('');

// Static data (could be moved to a constants file)
const makes = ref(['CANON', 'NIKON', 'SONY', 'FUJIFILM', "OLYMPUS", "PANASONIC", "RICOH", "SIGMA", "LEICA", "HASSELBLAD", "DJI", "OTHER"]);
const categories = ref([
    'DSLR', 'MIRRORLESS', 'COMPACT', 'FILM', 'LENS', 'TRIPOD', 'ACCESSORY', 'BAG', 'OTHER'
]);

// Computed property for localized types
const localizedTypes = computed(() => {
  return [
    { value: 'SALE', label: t('addListing.sale') },
    { value: 'WANTED', label: t('addListing.wanted') },
  ];
});

// Function to reset the form
const resetForm = () => {
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
  newTag.value = '';
  validationErrors.value = {};
  submitError.value = null;
  successMessage.value = '';
};

// Handle form submission
const handleSubmit = async () => {
  isSubmitting.value = true;
  successMessage.value = '';
  submitError.value = null;
  validationErrors.value = {};

  try {
    // Prepare the payload - ensure tags are in the correct format if needed
    // Assuming backend expects an array of tag objects like { name: 'tagname' }
    const payload = {
      ...listing.value,
      tags: listing.value.tags.map(tag => ({ name: tag.name })), // Ensure correct format
      price: Number(listing.value.price) // Ensure price is a number
    };

    // Use apiClient.post - Token is handled by the interceptor (default requiresAuth: true)
    const response = await apiClient.post('/listings', payload);

    // Check for successful creation (HTTP 201 Created is typical for POST)
    if (response.status === 201) {
      successMessage.value = t('addListing.success');
      // Optionally store the created listing data if needed elsewhere
      // createdListing.value = response.data;
      resetForm(); // Reset the form on success
      // Redirect after a short delay
      setTimeout(() => {
        // Redirect to the newly created listing's detail page if the ID is in the response
        if (response.data && response.data.id) {
          router.push({ name: 'ListingDetail', params: { id: response.data.id } });
        } else {
          router.push('/'); // Fallback to home page
        }
      }, 1500); // Redirect after 1.5 seconds
    } else {
      // Handle unexpected success status codes if necessary
      submitError.value = `${t('addListing.failure')} (Status: ${response.status})`;
    }

  } catch (err) {
    console.error('新增商品請求錯誤:', err);
    if (err.response) {
      // Server responded with an error status (4xx, 5xx)
      const status = err.response.status;
      const errorData = err.response.data;

      if (status === 400 && typeof errorData === 'object' && errorData !== null) {
        // Handle validation errors from backend
        validationErrors.value = errorData;
        submitError.value = t('addListing.validationError');
      } else if (status === 401) {
        submitError.value = t('addListing.loginRequired');
        // The interceptor might handle redirection, but show message
      } else if (status === 403) {
        submitError.value = t('addListing.permissionDenied');
      } else {
        // Other server errors
        submitError.value = (errorData && errorData.message) || t('addListing.failure');
      }
    } else if (err.request) {
      // Network error (no response received)
      submitError.value = t('addListing.networkError');
    } else {
      // Request setup error
      submitError.value = t('addListing.requestError');
    }
  } finally {
    isSubmitting.value = false;
  }
};

// Add a new tag
const addTag = () => {
  const tagName = newTag.value.trim();
  if (tagName) {
    if (listing.value.tags.length >= 10) {
      validationErrors.value.tags = t('addListing.maxTagsError');
    } else if (listing.value.tags.some(tag => tag.name === tagName)) {
      validationErrors.value.tags = t('addListing.duplicateTagError');
    } else {
      listing.value.tags.push({ name: tagName });
      newTag.value = ''; // Clear input
      validationErrors.value.tags = null; // Clear tag error if any
    }
  }
};

// Remove a tag by index
const removeTag = (index) => {
  listing.value.tags.splice(index, 1);
  // Clear max tags error if removing a tag brings it below the limit
  if (listing.value.tags.length < 10 && validationErrors.value.tags === t('addListing.maxTagsError')) {
     validationErrors.value.tags = null;
  }
};

</script>

<style scoped>
/* Styles are similar to EditListing, adjusted for consistency */
.add-listing-container {
  max-width: 600px;
  margin: 20px auto;
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
  margin-bottom: 1.2rem;
  width: 100%;
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
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 1rem;
}

textarea {
  height: 120px;
  resize: vertical;
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
  width: 100%;
  margin-top: 10px;
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
  vertical-align: middle; /* Align with input */
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

/* General error message styling */
.add-listing-container > .error-message {
    color: #d9534f;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    padding: 10px;
    border-radius: 4px;
    margin-top: 1rem;
    text-align: center;
}

/* Field-specific validation error */
.form-group .error-message {
  color: #d9534f;
  font-size: 0.875em;
  margin-top: 0.25rem;
}


.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  min-height: 40px;
}

.tag {
  background-color: #e0e0e0;
  padding: 5px 10px;
  border-radius: 15px;
  display: inline-flex;
  align-items: center;
  font-size: 0.9em;
}

.remove-tag {
  background-color: transparent;
  border: none;
  color: #dc3545;
  cursor: pointer;
  margin-left: 5px;
  padding: 0 3px;
  font-weight: bold;
  line-height: 1;
}

.remove-tag:hover {
    color: #a02530;
}

/* Removed seller-info styles as the display is commented out */
</style>
