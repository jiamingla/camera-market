package jiamingla.first.camera.market.entity;

import java.time.LocalDateTime;
import java.util.List; // 引入 List

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Make is required")
    @Enumerated(EnumType.STRING)
    private Make make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Price is required")
    private int price;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.OPEN;

    // 新增 ListingType 欄位
    @NotNull(message = "ListingType is required")
    @Enumerated(EnumType.STRING)
    private ListingType type; // 新增：表示 Listing 的類型（徵求或拍賣）

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private Member seller;

    // 新增 images 欄位
    @OneToMany(mappedBy = "listing", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Cascade(CascadeType.DELETE) //刪除listing時，把圖片也刪除
    private List<ListingImage> images;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    private LocalDateTime lastUpdateTime;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
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
                ", category=" + category +
                ", status=" + status +
                ", type=" + type + // 新增：輸出 ListingType
                ", sellerId=" + (seller != null ? seller.getId() : null) +
                //把images也加進來
                ", images=" + (images != null ? images.toString() : "null")+
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", createdBy=" + createdBy +
                ", lastModifiedBy=" + lastModifiedBy +
                '}';
    }
}
