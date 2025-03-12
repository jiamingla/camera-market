package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.exception.SystemException;
import jiamingla.first.camera.market.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MemberService memberService;

    @Transactional
    public Listing createListing(Listing listing) {
        listing.setCreateTime(LocalDateTime.now());
        listing.setStatus(ListingStatus.OPEN);
    
        // Validate Listing fields
        if (listing.getTitle() == null || listing.getTitle().trim().isEmpty()) {
            throw new BusinessException("Listing title cannot be empty.");
        }
        if (listing.getDescription() == null || listing.getDescription().trim().isEmpty()) {
            throw new BusinessException("Listing description cannot be empty.");
        }
        // ... other validations for make, model, price, category, etc.
    
        if (listing.getPrice() < 0) {
            throw new BusinessException("Price cannot be negative.");
        }
    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Member seller = memberService.findByUsername(username)
                .orElseThrow(() -> new SystemException("Seller not found"));
        listing.setSeller(seller);
        System.out.println("listing: " + listing);
        return listingRepository.save(listing);
    }
    
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public Listing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new SystemException("Listing not found with id: " + id));
    }

    @Transactional
    public Listing updateListing(Listing listing) {
        Listing existingListing = getListingById(listing.getId());
        // Check if the listing belongs to the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(!existingListing.getSeller().getUsername().equals(username)){
            throw new BusinessException("You can not edit other's listing.");
        }
        existingListing.setTitle(listing.getTitle());
        existingListing.setDescription(listing.getDescription());
        existingListing.setMake(listing.getMake());
        existingListing.setModel(listing.getModel());
        existingListing.setPrice(listing.getPrice());
        existingListing.setCategory(listing.getCategory());
        return listingRepository.save(existingListing);
    }

    @Transactional
    public void deleteListing(Long id) {
        Listing existingListing = getListingById(id);
        // Check if the listing belongs to the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(!existingListing.getSeller().getUsername().equals(username)){
            throw new BusinessException("You can not delete other's listing.");
        }
        listingRepository.deleteById(id);
    }

    public List<Listing> getListingsByStatus(ListingStatus status) {
        return listingRepository.findByStatus(status);
    }

    public List<Listing> getListingsByCategoryId(String category) {
        return listingRepository.findByCategory(category);
    }
}
