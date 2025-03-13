package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.exception.SystemException;
import jiamingla.first.camera.market.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jiamingla.first.camera.market.entity.Make;

@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MemberService memberService;

    @Transactional
    public Listing createListing(Listing listing) {
        logger.info("Attempting to create a new listing: {}", listing);
        listing.setCreateTime(LocalDateTime.now());
        listing.setStatus(ListingStatus.OPEN);

        // Validate Listing fields
        validateListing(listing);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Member seller = memberService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Seller not found with username: {}", username);
                    return new SystemException("Seller not found");
                });
        //直接設定seller就好
        listing.setSeller(seller);

        logger.info("Listing created successfully: {}", listing);
        return listingRepository.save(listing);
    }

    public List<Listing> getAllListings() {
        logger.info("Retrieving all listings.");
        List<Listing> listings = listingRepository.findAll();
        logger.info("Retrieved {} listings.", listings.size());
        return listings;
    }

    public Listing getListingById(Long id) {
        logger.info("Retrieving listing by ID: {}", id);
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Listing not found with id: {}", id);
                    return new SystemException("Listing not found with id: " + id);
                });
        logger.info("Retrieved listing: {}", listing);
        return listing;
    }

    @Transactional
    public Listing updateListing(Listing listing) {
        logger.info("Attempting to update listing: {}", listing);
        Listing existingListing = getListingById(listing.getId());
        // Check if the listing belongs to the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        //現在可以直接使用getSeller()取得
        if (!existingListing.getSeller().getUsername().equals(username)) {
            logger.error("User {} is not the owner of listing {}.", username, listing.getId());
            throw new BusinessException("You can not edit other's listing.");
        }
        existingListing.setTitle(listing.getTitle());
        existingListing.setDescription(listing.getDescription());
        existingListing.setMake(listing.getMake());
        existingListing.setModel(listing.getModel());
        existingListing.setPrice(listing.getPrice());
        existingListing.setCategory(listing.getCategory());
        //確認傳入的 make 是 enum 中的選項
        if (!isValidMake(existingListing.getMake())) {
            logger.error("Make is not correct.");
            throw new BusinessException("Make is not correct");
        }
        Listing updatedListing = listingRepository.save(existingListing);
        logger.info("Listing updated successfully: {}", updatedListing);
        return updatedListing;
    }

    @Transactional
    public void deleteListing(Long id) {
        logger.info("Attempting to delete listing with ID: {}", id);
        Listing existingListing = getListingById(id);
        // Check if the listing belongs to the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        //現在可以直接使用getSeller()取得
        if (!existingListing.getSeller().getUsername().equals(username)) {
            logger.error("User {} is not the owner of listing {}.", username, id);
            throw new BusinessException("You can not delete other's listing.");
        }
        listingRepository.deleteById(id);
        logger.info("Listing deleted successfully: {}", id);
    }

    public List<Listing> getListingsByStatus(ListingStatus status) {
        logger.info("Retrieving listings by status: {}", status);
        List<Listing> listings = listingRepository.findByStatus(status);
        logger.info("Retrieved {} listings with status {}.", listings.size(), status);
        return listings;
    }

    public List<Listing> getListingsByCategoryId(String category) {
        logger.info("Retrieving listings by category: {}", category);
        List<Listing> listings = listingRepository.findByCategory(category);
        logger.info("Retrieved {} listings in category {}.", listings.size(), category);
        return listings;
    }

    private boolean isValidMake(Make make) {
        logger.debug("Validating make: {}", make);
        // 直接檢查 enum 中是否存在
        for (Make validMake : Make.values()) {
            if (validMake == make) {
                logger.debug("Make is valid: {}", make);
                return true;
            }
        }
        logger.warn("Make is not valid: {}", make);
        return false;
    }

    private void validateListing(Listing listing) {
        if (listing.getTitle() == null || listing.getTitle().trim().isEmpty()) {
            logger.error("Listing title cannot be empty");
            throw new BusinessException("Listing title cannot be empty");
        }
        if (listing.getDescription() == null || listing.getDescription().trim().isEmpty()) {
            logger.error("Listing description cannot be empty");
            throw new BusinessException("Listing description cannot be empty");
        }

        if (listing.getPrice() < 0) {
            logger.error("Listing price cannot be negative");
            throw new BusinessException("Listing price cannot be negative");
        }
        //確認傳入的 make 是 enum 中的選項
        if (!isValidMake(listing.getMake())) {
            logger.error("Make is not correct.");
            throw new BusinessException("Make is not correct");
        }

        logger.debug("Listing information check passed");
    }
}
