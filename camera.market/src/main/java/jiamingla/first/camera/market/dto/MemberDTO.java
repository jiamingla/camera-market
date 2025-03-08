package jiamingla.first.camera.market.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberDTO {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String address;
    private String phone;
    private LocalDateTime createTime;
}
