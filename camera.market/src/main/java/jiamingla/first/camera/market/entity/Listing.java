package jiamingla.first.camera.market.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
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

    @NotBlank(message = "Category is required")
    private String category;

    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private Member seller;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createTime;

    // ... (getters and setters)
}
