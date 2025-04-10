package org.ffb_be.dto.product;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
@Data
@Validated
public class ProductCreateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private Long shopId;
    @NotBlank(message = "Description is required")
    private String description;
    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    private List<FoodOptionDTO> foodOption;

    private String supplier;

    private String manufacturer;
    @NotBlank(message = "FoodType is required")
    private String foodType;
}
