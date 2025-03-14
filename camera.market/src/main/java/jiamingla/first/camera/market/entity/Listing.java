package jiamingla.first.camera.market.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy; // 新增
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy; // 新增
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener; //新增

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)//新增
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    // 欄位類型修改為 Make Enum
    @NotNull(message = "Make is required")
    @Enumerated(EnumType.STRING) // 用於標示這個欄位在資料庫中以字串方式儲存，否則預設是數字
    private Make make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Price is required")
    private int price;

    // 修改：將 category 的類型改為 Category enum
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING) // 用於標示這個欄位在資料庫中以字串方式儲存，否則預設是數字
    private Category category;

    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.OPEN;

    // 修改：重新使用 @ManyToOne 和 @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY) // 建議使用 LAZY 加載
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private Member seller;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(updatable = false) //設定新增後不可更新
    @CreatedDate //在新增時自動新增時間
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate //在每次更新時自動更新時間
    private LocalDateTime lastUpdateTime;

    @CreatedBy // 新增這行
    private String createdBy;

    @LastModifiedBy // 新增這行
    private String lastModifiedBy;

    @Override
    public String toString() {
        return "Listing{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", make=" + make +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", category=" + category +  // 修改：顯示 Category enum
                ", status=" + status +
                ", sellerId=" + (seller != null ? seller.getId() : null) + // 為了顯示sellerId
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", createdBy=" + createdBy +
                ", lastModifiedBy=" + lastModifiedBy +
                '}';
    }
}
