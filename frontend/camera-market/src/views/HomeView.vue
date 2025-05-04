<template>
  <main>
    <h1>{{ $t('homeView.title') }}</h1> <!-- Changed to specific key -->
    <!-- Loading State -->
    <div v-if="isLoading" key="loading" class="status-message">{{ $t('common.loading') }}</div>
    <!-- Error State -->
    <div v-else-if="error" key="error" class="error-message">
      {{ $t('common.error') }}: {{ error.message || $t('homeView.loadErrorGeneric') }} <!-- Added generic load error -->
    </div>
    <!-- Success State (Data Loaded) -->
    <div v-else key="content">
      <!-- Check if listings exist -->
      <div v-if="listings.length > 0" class="listings-container">
        <router-link
          v-for="listing in listings"
          :key="listing.id"
          :to="{ name: 'ListingDetail', params: { id: listing.id } }"
          class="listing-item-link"
        >
          <div class="listing-item">
            <!-- Optional: Display first image if available -->
            <img v-if="listing.images && listing.images.length > 0" :src="listing.images[0]" :alt="listing.title" class="listing-thumbnail"/>
            <div v-else class="listing-thumbnail placeholder"><span>{{ $t('homeView.noImage') }}</span></div>

            <div class="listing-info">
              <h3>{{ listing.title }}</h3>
              <p><strong>{{ $t('listingDetail.makeLabel') }}:</strong> {{ listing.make }}</p>
              <p><strong>{{ $t('listingDetail.priceLabel') }}:</strong> NT$ {{ listing.price?.toLocaleString() }}</p> <!-- Added formatting -->
              <p><strong>{{ $t('listingDetail.categoryLabel') }}:</strong> {{ listing.category }}</p>
              <p><strong>{{ $t('listingDetail.typeLabel') }}:</strong> {{ $t(`listingDetail.${listing.type?.toLowerCase()}`) }}</p> <!-- Added type translation -->
              <p>
                <strong>{{ $t('listingDetail.sellerLabel') }}:</strong>
                <span v-if="listing.member">{{ listing.member.username }}</span>
                <span v-else>{{ $t('listingDetail.unknownSeller') }}</span> <!-- Use i18n key -->
              </p>
              <p class="update-time">
                <strong>{{ $t('listingDetail.lastUpdateLabel') }}:</strong> {{ formatDateTime(listing.lastUpdateTime) }} <!-- Added formatting -->
              </p>
            </div>
          </div>
        </router-link>
      </div>
      <!-- No listings found -->
      <div v-else class="status-message">{{ $t('homeView.noListings') }}</div> <!-- Use i18n key -->
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// Removed useRouter as it's not used directly in script setup
import { useI18n } from 'vue-i18n'; // 導入 useI18n
import apiClient from '@/services/apiClient';

// const router = useRouter(); // Not needed if only used in template via <router-link>
const { t, d } = useI18n(); // Initialize i18n, include 'd' for date formatting

const listings = ref([]);
const isLoading = ref(true);
const error = ref(null);

onMounted(async () => {
  // Added console logs for debugging (can be removed later)
  console.log('HomeView: onMounted - Start fetching listings...');
  isLoading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get('/listings', { requiresAuth: false });
    console.log('HomeView: API response received', response.status);

    if (response.status === 200) {
      listings.value = Array.isArray(response.data) ? response.data : [];
      console.log(`HomeView: ${listings.value.length} listings loaded.`);
    } else {
      // This case might be less likely with Axios interceptors handling non-2xx
      throw new Error(`${t('homeView.fetchFailedStatus')}: ${response.status}`); // Use i18n key
    }

  } catch (err) {
    console.error("HomeView: API request failed:", err);
    // Use a more specific error message if available from the error object
    error.value = err.response?.data?.message || err.message || t('homeView.loadErrorGeneric');
  } finally {
    isLoading.value = false;
    console.log('HomeView: Fetching finished, isLoading:', isLoading.value);
  }
});

// Helper function to format date/time (consistent with ListingDetail.vue)
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return 'N/A';
  try {
    // Using vue-i18n's date formatting (requires configuration in main.js)
    return d(new Date(dateTimeString), 'long'); // 'long' format defined in main.js
  } catch (e) {
    console.error("Error formatting date:", e);
    return dateTimeString; // Fallback
  }
};

</script>

<style scoped>
h1 {
  text-align: center;
  margin-bottom: 30px;
}

.status-message {
  text-align: center;
  padding: 20px;
  font-size: 1.1rem;
  color: #666;
}

.error-message {
  color: #d9534f;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  padding: 15px;
  border-radius: 4px;
  margin: 20px auto;
  max-width: 600px;
  text-align: center;
}

.listings-container {
  display: grid;
  /* Adjust columns for better spacing and responsiveness */
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem; /* Increased gap */
  padding: 0 1rem; /* Add some horizontal padding to the container */
}

.listing-item-link {
  text-decoration: none;
  color: inherit;
  display: block; /* Ensure link takes up the grid cell */
  border: 1px solid #ddd; /* Add border to the link itself */
  border-radius: 8px;
  overflow: hidden; /* Keep contents within rounded corners */
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  background-color: #fff; /* White background for items */
}

.listing-item-link:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px); /* Slight lift effect */
}

.listing-item {
  display: flex; /* Use flexbox for image and text layout */
  flex-direction: column; /* Stack image and info vertically */
}

.listing-thumbnail {
  width: 100%;
  height: 200px; /* Fixed height for thumbnails */
  object-fit: cover; /* Cover the area, cropping if needed */
  background-color: #eee; /* Placeholder background */
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
}

.listing-thumbnail.placeholder span {
    font-size: 0.9em;
}


.listing-info {
  padding: 1rem; /* Padding for text content */
}

.listing-info h3 {
  margin-top: 0;
  margin-bottom: 0.75rem;
  font-size: 1.2rem;
  color: #333;
  /* Limit title lines */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* Show max 2 lines */
  -webkit-box-orient: vertical;
}

.listing-info p {
  margin: 0.3rem 0;
  font-size: 0.95rem;
  color: #555;
}

.listing-info p strong {
    color: #333;
    margin-right: 4px;
}

.update-time {
  font-size: 0.85rem;
  color: #777;
  margin-top: 0.5rem;
}
</style>
