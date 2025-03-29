package jiamingla.first.camera.market.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jiamingla.first.camera.market.entity.ListingStatus;
import jiamingla.first.camera.market.entity.ListingType;
import jiamingla.first.camera.market.entity.Make;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListingSummaryDto {
    private Long id;
    private String title;
    private Make make;
    private int price;
    private ListingStatus status;
    private ListingType type;
    private MemberSummaryDto member; // Change the type to MemberSummaryDto
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lastUpdateTime;
}
