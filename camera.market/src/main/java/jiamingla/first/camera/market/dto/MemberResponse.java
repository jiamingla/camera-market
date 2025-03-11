package jiamingla.first.camera.market.dto;

import jiamingla.first.camera.market.entity.Listing;
import lombok.Data;

import java.util.List;

@Data
public class MemberResponse {
    private Long id;
    private String username;
    private String email;
    private List<Listing> listings;
}
