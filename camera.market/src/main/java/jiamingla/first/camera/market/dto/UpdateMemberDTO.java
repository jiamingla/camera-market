package jiamingla.first.camera.market.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateMemberDTO {
    @NotBlank(message = "Username is required")
    private String username;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    private String name;
    private String address;
    private String phone;
}
