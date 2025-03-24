package jiamingla.first.camera.market.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

    @NotNull(message = "ListingType is required")
    @Enumerated(EnumType.STRING)
    private ListingType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    @OneToMany(mappedBy = "listing", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Cascade(CascadeType.DELETE)
    private List<ListingImage> images;

    // 新增 tags 欄位，使用 ManyToMany 關聯 Tag 實體
    @ManyToMany(cascade = {jakarta.persistence.CascadeType.PERSIST, jakarta.persistence.CascadeType.MERGE}) // 多對多關係
    @JoinTable( // 指定中間表的資訊
            name = "listing_tag", // 中間表的名稱
            joinColumns = @JoinColumn(name = "listing_id"), // 在中間表中，指向 Listing 的外鍵欄位
            inverseJoinColumns = @JoinColumn(name = "tag_id") // 在中間表中，指向 Tag 的外鍵欄位
    )
    private List<Tag> tags = new ArrayList<>(); // 初始化為空列表

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
                ", type=" + type +
                ", memberId=" + (member != null ? member.getId() : null) +
                ", images=" + (images != null ? images.toString() : "null") +
                //把tags也加進來
                ", tags=" + (tags != null ? tags.toString() : "null") +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", createdBy=" + createdBy +
                ", lastModifiedBy=" + lastModifiedBy +
                '}';
    }
}
