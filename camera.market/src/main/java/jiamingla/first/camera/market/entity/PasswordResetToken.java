package jiamingla.first.camera.market.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity // 標記這個類是一個 JPA 實體，對應到資料庫中的一個表格
public class PasswordResetToken {

    @Id // 標記這個欄位是主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主鍵自動生成策略，使用資料庫的自增長機制
    private Long id;

    @Column(nullable = false, unique = true) // 標記這個欄位是一個資料庫欄位，不允許為空，且必須唯一
    private String token; // 用於重置密碼的唯一令牌

    @OneToOne(targetEntity = Member.class, fetch = FetchType.EAGER) // 與 Member 實體的一對一關聯，立即加載
    @JoinColumn(nullable = false, name = "member_id") // 外鍵欄位，對應 Member 表格的 id
    private Member member; // 與此令牌相關聯的會員

    @Column(nullable = false) // 標記這個欄位是一個資料庫欄位，不允許為空
    private LocalDateTime expiryDate; // 令牌的過期時間

    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString(); // 生成一個隨機的 UUID 作為令牌
        this.expiryDate = LocalDateTime.now().plusHours(1); // 設定令牌有效期為 1 小時
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
