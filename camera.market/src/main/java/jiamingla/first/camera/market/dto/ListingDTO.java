package jiamingla.first.camera.market.dto;

import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.Member;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListingDTO {
    private Long id;
    private String title;
    private String description;
    private String make;
    private String model;
    private Double price;
    private String category;
    private LocalDateTime createTime;
    private Member seller;
    private ListingStatus status;
}

