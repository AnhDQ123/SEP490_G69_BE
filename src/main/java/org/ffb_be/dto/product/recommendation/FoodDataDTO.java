package org.ffb_be.dto.product.recommendation;

import lombok.Data;
import org.ffb_be.utils.enums.SellType;

@Data
public class FoodDataDTO {
    private Long id;
    private String name;
    private String description;
    private SellType type;
}
