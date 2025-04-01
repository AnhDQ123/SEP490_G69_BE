package org.ffb_be.dto.product;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class ProductCreateDTO {
    private String name;
    private String description;
    private String category;
    private int quantity;
    private LocalDate expiryDate;
    private List<FoodOptionDTO> foodOption;
    private String supplier;
    private String manufacturer;
    private String foodType;
}
