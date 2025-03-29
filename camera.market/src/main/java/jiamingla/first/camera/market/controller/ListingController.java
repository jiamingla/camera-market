package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.dto.ListingSummaryDto;
import jiamingla.first.camera.market.dto.MemberSummaryDto;
import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.service.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingService listingService;

    @PostMapping
    public ResponseEntity<Listing> createListing(@Valid @RequestBody Listing listing) {
        logger.info("Received request to create a new listing: {}", listing);
        Listing createdListing = listingService.createListing(listing);
        logger.info("Created listing: {}", createdListing);
        return new ResponseEntity<>(createdListing, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ListingSummaryDto>> getAllListings() {
        logger.info("Received request to get all listings.");
        List<Listing> listings = listingService.getAllListings();
        // Convert Listing to ListingSummaryDto
        List<ListingSummaryDto> listingSummaryDtos = listings.stream()
                .map(this::convertToListingSummaryDto)
                .collect(Collectors.toList());
        logger.info("Returning {} listing summaries.", listingSummaryDtos.size());
        return new ResponseEntity<>(listingSummaryDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable Long id) {
        logger.info("Received request to get listing by ID: {}", id);
        Listing listing = listingService.getListingById(id);
        logger.info("Returning listing: {}", listing);
        return new ResponseEntity<>(listing, HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<Listing> updateListing(@Valid @RequestBody Listing listing) {
        logger.info("Received request to update listing: {}", listing);
        Listing updatedListing = listingService.updateListing(listing);
        logger.info("Updated listing: {}", updatedListing);
        return new ResponseEntity<>(updatedListing, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        logger.info("Received request to delete listing with ID: {}", id);
        listingService.deleteListing(id);
        logger.info("Listing deleted successfully: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ListingSummaryDto>> getListingsByStatus(@PathVariable ListingStatus status) {
        logger.info("Received request to get listings by status: {}", status);
        List<Listing> listings = listingService.getListingsByStatus(status);
        List<ListingSummaryDto> listingSummaryDtos = listings.stream()
                .map(this::convertToListingSummaryDto)
                .collect(Collectors.toList());
        logger.info("Returning {} listings with status {}.", listingSummaryDtos.size(), status);
        return new ResponseEntity<>(listingSummaryDtos, HttpStatus.OK);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ListingSummaryDto>> getListingsByCategoryId(@PathVariable String category) {
        logger.info("Received request to get listings by category: {}", category);
        List<Listing> listings = listingService.getListingsByCategoryId(category);
        List<ListingSummaryDto> listingSummaryDtos = listings.stream()
                .map(this::convertToListingSummaryDto)
                .collect(Collectors.toList());
        logger.info("Returning {} listings in category {}.", listingSummaryDtos.size(), category);
        return new ResponseEntity<>(listingSummaryDtos, HttpStatus.OK);
    }

    // 新增異常處理器
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    // Helper method to convert Listing to ListingSummaryDto
    private ListingSummaryDto convertToListingSummaryDto(Listing listing) {
        ListingSummaryDto dto = new ListingSummaryDto();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setMake(listing.getMake());
        dto.setPrice(listing.getPrice());
        dto.setStatus(listing.getStatus());
        dto.setType(listing.getType());

        // Convert Member to MemberSummaryDto
        Member member = listing.getMember();
        if (member != null) {
            MemberSummaryDto memberDto = new MemberSummaryDto();
            memberDto.setId(member.getId());
            memberDto.setUsername(member.getUsername());
            dto.setMember(memberDto);
        }

        dto.setLastUpdateTime(listing.getLastUpdateTime());
        return dto;
    }
}
