package org.ffb_be.dto.auth.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodOptionDTO {
    private String id;
    private String name;
    private BigDecimal price;
    private String description;
    private String image;
    private int quantity;
    private String type_id;
    private String status;
}
