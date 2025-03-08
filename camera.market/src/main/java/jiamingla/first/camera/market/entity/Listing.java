package jiamingla.first.camera.market.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

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

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Price is required")
    private Double price;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Member seller;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;
    
    private String category;

    private LocalDateTime createTime;

    // ... (getters and setters)
}
