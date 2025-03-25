package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.*;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.exception.SystemException;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private Validator validator; // 注入 Validator

    @Transactional
    public Listing createListing(Listing listing) {
        logger.info("Attempting to create a new listing: {}", listing);
        listing.setStatus(ListingStatus.OPEN);

        // Validate Listing fields
        validateListing(listing);
        //使用validator驗證
        validateWithValidator(listing);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Seller not found with username: {}", username);
                    return new SystemException("Seller not found");
                });
        listing.setMember(member);

        //處理tag
        handleTags(listing);

        logger.info("Listing created successfully: {}", listing);
        return listingRepository.save(listing);
    }

    @Transactional
    public List<Listing> getAllListings() {
        logger.info("Retrieving all listings.");
        // Use sorting by lastUpdateTime in descending order
        Sort sort = Sort.by(Sort.Direction.DESC, "lastUpdateTime");
        List<Listing> listings = listingRepository.findAll(sort);
        logger.info("Retrieved {} listings, sorted by lastUpdateTime (descending).", listings.size());
        return listings;
    }

    @Transactional
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

        if (!existingListing.getMember().getUsername().equals(username)) {
            logger.error("User {} is not the owner of listing {}.", username, listing.getId());
            throw new BusinessException("You can not edit other's listing.");
        }
        existingListing.setTitle(listing.getTitle());
        existingListing.setDescription(listing.getDescription());
        existingListing.setMake(listing.getMake());
        existingListing.setModel(listing.getModel());
        existingListing.setPrice(listing.getPrice());
        existingListing.setCategory(listing.getCategory());
        //更新tag
        existingListing.getTags().clear();
        handleTags(listing);
        existingListing.getTags().addAll(listing.getTags());
        //使用validator驗證
        validateWithValidator(existingListing);

        if (!isValidMake(existingListing.getMake())) {
            logger.error("Make is not correct.");
            throw new BusinessException("Make is not correct");
        }

        if (!isValidCategory(existingListing.getCategory())) {
            logger.error("Category is not correct.");
            throw new BusinessException("Category is not correct");
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
        if (!existingListing.getMember().getUsername().equals(username)) {
            logger.error("User {} is not the owner of listing {}.", username, id);
            throw new BusinessException("You can not delete other's listing.");
        }
        listingRepository.deleteById(id);
        logger.info("Listing deleted successfully: {}", id);
    }

    @Transactional
    public List<Listing> getListingsByStatus(ListingStatus status) {
        logger.info("Retrieving listings by status: {}", status);
        List<Listing> listings = listingRepository.findByStatus(status);
        logger.info("Retrieved {} listings with status {}.", listings.size(), status);
        return listings;
    }

    @Transactional
    public List<Listing> getListingsByCategoryId(String category) {
        logger.info("Retrieving listings by category: {}", category);
        //沒有辦法直接使用category enum，所以要再轉型一次。
        Category categoryEnum = Category.valueOf(category);
        List<Listing> listings = listingRepository.findByCategory(categoryEnum);
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

    private boolean isValidCategory(Category category) {
        logger.debug("Validating category: {}", category);
        // 直接檢查 enum 中是否存在
        for (Category validCategory : Category.values()) {
            if (validCategory == category) {
                logger.debug("Category is valid: {}", category);
                return true;
            }
        }
        logger.warn("Category is not valid: {}", category);
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
        if (listing.getMake()==null||!isValidMake(listing.getMake())) {
            logger.error("Make is not correct.");
            throw new BusinessException("Make is not correct");
        }
        //新增檢查category
        if (listing.getCategory()==null||!isValidCategory(listing.getCategory())) {
            logger.error("Category is not correct.");
            throw new BusinessException("Category is not correct");
        }
        if (listing.getModel()==null||listing.getModel().trim().isEmpty()) {
            logger.error("Listing Model cannot be empty");
            throw new BusinessException("Listing Model cannot be empty");
        }
        logger.debug("Listing information check passed");
    }
    private void handleTags(Listing listing) {
        if (listing.getTags() != null) {
            for (Tag tag : listing.getTags()) {
                // 檢查 tag 是否已存在，如果存在則使用已存在的 tag，避免重複建立
                Optional<Tag> existingTag = tagRepository.findByName(tag.getName());
                if (existingTag.isPresent()) {
                    tag.setId(existingTag.get().getId());
                }
            }
        }
    }
    private void validateWithValidator(Listing listing) {
        Set<ConstraintViolation<Listing>> violations = validator.validate(listing);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Listing> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            logger.error("Listing validation failed: {}", sb.toString());
            throw new BusinessException("Listing validation failed: " + sb.toString());
        }
    }
}
