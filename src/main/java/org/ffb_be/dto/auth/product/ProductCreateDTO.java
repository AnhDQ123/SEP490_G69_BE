package org.ffb_be.dto.auth.product;

import lombok.Data;
import org.ffb_be.entity.FoodOption;

import java.time.LocalDate;
import java.util.List;
@Data
public class ProductCreateDTO {
    private String name;
    private String description;
    private Long category_id;
    private int quantity;
    private LocalDate expiryDate;
    private List<FoodOption> foodOption;
    private String supplier;
    private String manufacturer;
}
