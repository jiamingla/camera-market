package jiamingla.first.camera.market.repository;

import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.Category;
import jiamingla.first.camera.market.entity.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByCategory(Category category);
    List<Listing> findByTags(Tag tag);
    // TODO: 未來需要用tag找Listing，但應該不能用複數tag去找資料，應該要用單一tag去找
}
