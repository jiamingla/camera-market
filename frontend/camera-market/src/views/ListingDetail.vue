<template>
  <main class="listing-detail-container">
    <h1>{{ $t('listingDetail.title') }}</h1>

    <!-- Loading State -->
    <div v-if="isLoading" key="loading">{{ $t('common.loading') }}</div>

    <!-- Load Error State -->
    <div v-else-if="loadError" key="load-error" class="error-message">
      {{ $t('listingDetail.loadError') }}: {{ loadError.message || $t('common.error') }}
    </div>

    <!-- Listing Found State -->
    <div v-else-if="listing" key="listing-content" class="listing-content">
      <h2>{{ listing.title }}</h2>

      <div class="detail-grid">
        <div class="detail-item">
          <strong>{{ $t('listingDetail.makeLabel') }}:</strong> {{ listing.make }}
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.modelLabel') }}:</strong> {{ listing.model }}
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.priceLabel') }}:</strong> NT$ {{ listing.price?.toLocaleString() }}
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.categoryLabel') }}:</strong> {{ listing.category }}
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.typeLabel') }}:</strong> {{ $t(`listingDetail.${listing.type?.toLowerCase()}`) }} <!-- Translate SALE/WANTED -->
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.statusLabel') }}:</strong> {{ listing.status }} <!-- Consider translating status if needed -->
        </div>
        <div class="detail-item detail-full-width">
          <strong>{{ $t('listingDetail.descriptionLabel') }}:</strong>
          <p>{{ listing.description }}</p>
        </div>
        <div class="detail-item detail-full-width">
          <strong>{{ $t('listingDetail.tagsLabel') }}:</strong>
          <span v-if="listing.tags && listing.tags.length > 0" class="tags-display">
            <span v-for="tag in listing.tags" :key="tag.id || tag.name" class="tag">{{ tag.name }}</span>
          </span>
          <span v-else>{{ $t('listingDetail.noTags') }}</span>
        </div>
         <div class="detail-item detail-full-width">
          <strong>{{ $t('listingDetail.imagesLabel') }}:</strong>
          <!-- Basic image display - assumes listing.images is an array of URLs -->
          <div v-if="listing.images && listing.images.length > 0" class="image-gallery">
             <img v-for="(image, index) in listing.images" :key="index" :src="image" :alt="`${listing.title} image ${index + 1}`" class="listing-image"/>
          </div>
          <span v-else>{{ $t('listingDetail.noImages') }}</span>
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.sellerLabel') }}:</strong> {{ listing.member?.username || $t('listingDetail.unknownSeller') }}
        </div>
        <div class="detail-item">
          <strong>{{ $t('listingDetail.lastUpdateLabel') }}:</strong> {{ formatDateTime(listing.lastUpdateTime) }}
        </div>
      </div>

      <!-- Action Buttons (Show only if logged in and is the owner) -->
      <div v-if="isOwner" class="actions">
        <button @click="editListing" class="edit-button" :disabled="isDeleting">
          {{ $t('listingDetail.editButton') }}
        </button>
        <button @click="deleteListing" class="delete-button" :disabled="isDeleting">
          {{ isDeleting ? $t('listingDetail.deleting') : $t('listingDetail.deleteButton') }}
        </button>
      </div>
      <!-- Display delete error/success messages -->
       <div v-if="deleteError" class="error-message">
        {{ deleteError }}
      </div>
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
    </div>

    <!-- Listing Not Found State -->
    <div v-else key="not-found">{{ $t('listingDetail.notFound') }}</div>

    <button @click="goBack" class="back-button">{{ $t('common.goBack') }}</button>

  </main>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import apiClient from '@/services/apiClient';
import { useLoginStatus } from '../main'; // Import login status

const { t, d } = useI18n(); // Get t and d (for date formatting)
const route = useRoute();
const router = useRouter();
const { isLoggedIn, currentUserId } = useLoginStatus(); // Get login status and user ID

const listingId = route.params.id;

// State
const listing = ref(null);
const isLoading = ref(true);
const loadError = ref(null); // Error during initial load
const deleteError = ref(''); // Error during deletion attempt
const successMessage = ref(''); // Success message for deletion
const isDeleting = ref(false); // Track deletion process

// Fetch listing data on mount
onMounted(async () => {
  isLoading.value = true;
  loadError.value = null;
  try {
    // Fetching details is public, no token needed
    const response = await apiClient.get(`/listings/${listingId}`, { requiresAuth: false });
    listing.value = response.data;
    // Basic validation if listing is actually returned
    if (!listing.value || !listing.value.id) {
        throw new Error(t('listingDetail.notFound'));
    }
  } catch (err) {
    console.error("載入商品資訊失敗:", err);
    if (err.response && err.response.status === 404) {
        loadError.value = { message: t('listingDetail.notFound') }; // Specific 404 message
    } else {
        loadError.value = err; // Store the whole error object
    }
  } finally {
    isLoading.value = false;
  }
});

// Computed property to check if the current user is the owner
const isOwner = computed(() => {
  // Check if logged in, listing data is loaded, member info exists, and IDs match
  return isLoggedIn.value && listing.value && listing.value.member && listing.value.member.id === currentUserId.value;
});

// Navigate to edit page
const editListing = () => {
  router.push({ name: 'EditListing', params: { id: listingId } });
};

// Delete the listing
const deleteListing = async () => {
  if (confirm(t('deleteListing.confirm'))) {
    isDeleting.value = true;
    deleteError.value = '';
    successMessage.value = '';
    try {
      // apiClient handles the token automatically (default requiresAuth: true)
      const response = await apiClient.delete(`/listings/${listingId}`);

      if (response.status === 204) { // HTTP 204 No Content is success for DELETE
        successMessage.value = t('deleteListing.success');
        setTimeout(() => {
          router.push('/'); // Redirect to home after successful deletion
        }, 1500);
      } else {
        // Should ideally not happen if interceptor works, but good fallback
        deleteError.value = `${t('deleteListing.failure')} (Status: ${response.status})`;
      }
    } catch (err) {
      console.error('刪除商品請求錯誤:', err);
       if (err.response) {
        const status = err.response.status;
        const errorData = err.response.data;
        if (status === 401) {
          deleteError.value = t('deleteListing.loginRequired');
        } else if (status === 403) {
          deleteError.value = t('deleteListing.permissionDenied');
        } else if (status === 404) {
          deleteError.value = t('listingDetail.notFound'); // Maybe already deleted
        } else {
          deleteError.value = (errorData && errorData.message) || t('deleteListing.failure');
        }
      } else if (err.request) {
        deleteError.value = t('common.networkError');
      } else {
        deleteError.value = t('common.requestError');
      }
    } finally {
      isDeleting.value = false;
    }
  }
};

// Go back to the previous page or home
const goBack = () => {
  // router.go(-1) might be unreliable if the user landed here directly
  router.push('/'); // Safer to go home
};

// Helper function to format date/time (optional, requires date-fns or similar)
// If you don't have a date library, you can use basic JS Date formatting
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return 'N/A';
  try {
    // Using vue-i18n's date formatting (if configured)
    return d(new Date(dateTimeString), 'long'); // Example format, configure in main.js
    // Or basic JS:
    // return new Date(dateTimeString).toLocaleString();
  } catch (e) {
    console.error("Error formatting date:", e);
    return dateTimeString; // Fallback to original string
  }
};

</script>

<style scoped>
.listing-detail-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h1, h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.listing-content {
  margin-top: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); /* Responsive grid */
  gap: 1rem;
  margin-bottom: 20px;
}

.detail-item {
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.detail-item strong {
  margin-right: 5px;
  color: #555;
}

.detail-full-width {
  grid-column: 1 / -1; /* Make description, tags, images span full width */
}

.detail-item p {
    margin-top: 5px;
    margin-bottom: 0;
    white-space: pre-wrap; /* Preserve whitespace in description */
    word-break: break-word;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 5px;
}

.tag {
  background-color: #e0e0e0;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.9em;
}

.image-gallery {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 10px;
}

.listing-image {
    max-width: 150px; /* Limit image size */
    max-height: 150px;
    object-fit: cover; /* Ensure images cover the area nicely */
    border: 1px solid #ddd;
    border-radius: 4px;
}

.actions {
  margin-top: 20px;
  text-align: right; /* Align buttons to the right */
}

.edit-button,
.delete-button,
.back-button {
  padding: 10px 20px;
  margin-left: 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.edit-button {
  background-color: #ffc107; /* Warning color */
  color: #333;
}
.edit-button:hover:not(:disabled) {
  background-color: #e0a800;
}

.delete-button {
  background-color: #dc3545; /* Danger color */
  color: white;
}
.delete-button:hover:not(:disabled) {
  background-color: #c82333;
}

.back-button {
  background-color: #6c757d; /* Secondary color */
  color: white;
  display: block; /* Make it block to center with margin */
  width: fit-content;
  margin: 30px auto 0; /* Center button below content */
}
.back-button:hover {
  background-color: #5a6268;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
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
  color: #d9534f;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  padding: 10px;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
}
</style>
