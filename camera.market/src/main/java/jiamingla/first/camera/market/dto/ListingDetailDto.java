package jiamingla.first.camera.market.dto;

import jiamingla.first.camera.market.entity.Category;
import jiamingla.first.camera.market.entity.ListingImage;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.ListingType;
import jiamingla.first.camera.market.entity.Make;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListingDetailDto {
    private Long id;
    private String title;
    private String description;
    private Make make;
    private String model;
    private int price;
    private Category category;
    private ListingStatus status;
    private ListingType type;
    private MemberSummaryDto member;
    private List<ListingImage> images;
    private LocalDateTime lastUpdateTime;
    private List<TagSummaryDto> tags;
}
