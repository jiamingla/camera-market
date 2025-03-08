package jiamingla.first.camera.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateListingDTO {
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
    private String category;
}
