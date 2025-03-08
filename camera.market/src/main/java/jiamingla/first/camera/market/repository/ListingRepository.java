package jiamingla.first.camera.market.repository;

import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByCategory(String category);
}
