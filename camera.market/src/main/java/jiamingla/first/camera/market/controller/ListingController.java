package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @PostMapping
    public ResponseEntity<Listing> createListing(@Valid @RequestBody Listing listing) {
        Listing createdListing = listingService.createListing(listing);
        return new ResponseEntity<>(createdListing, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        List<Listing> listings = listingService.getAllListings();
        return new ResponseEntity<>(listings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable Long id) {
        Listing listing = listingService.getListingById(id);
        return new ResponseEntity<>(listing, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Listing> updateListing(@Valid @RequestBody Listing listing) {
        Listing updatedListing = listingService.updateListing(listing);
        return new ResponseEntity<>(updatedListing, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        listingService.deleteListing(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Listing>> getListingsByStatus(@PathVariable ListingStatus status) {
        List<Listing> listings = listingService.getListingsByStatus(status);
        return new ResponseEntity<>(listings, HttpStatus.OK);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Listing>> getListingsByCategoryId(@PathVariable String category) {
        List<Listing> listings = listingService.getListingsByCategoryId(category);
        return new ResponseEntity<>(listings, HttpStatus.OK);
    }

}
