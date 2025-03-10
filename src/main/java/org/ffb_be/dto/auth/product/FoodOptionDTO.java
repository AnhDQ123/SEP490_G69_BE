package org.ffb_be.dto.auth.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodOptionDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private String type_id;
    private String status;
}
