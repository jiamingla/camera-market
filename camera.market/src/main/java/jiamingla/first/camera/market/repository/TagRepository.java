package jiamingla.first.camera.market.repository;

import jiamingla.first.camera.market.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name); // 根據 tag 名稱查詢
}
