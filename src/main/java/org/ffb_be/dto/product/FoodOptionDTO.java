package org.ffb_be.dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodOptionDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private Long type_id;
    private String status;
    private Long product_id;
}
