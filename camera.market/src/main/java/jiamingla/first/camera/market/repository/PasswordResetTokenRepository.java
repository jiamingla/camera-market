package jiamingla.first.camera.market.repository;

import jiamingla.first.camera.market.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 標記這個介面是一個 Spring Data JPA 儲存庫
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    // 通過令牌字串查找 PasswordResetToken
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByMemberId(Long id);
    // 刪除對應member id 的 token
    void deleteByMemberId(Long id);
}
