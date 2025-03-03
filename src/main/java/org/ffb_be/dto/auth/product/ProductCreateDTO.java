package org.ffb_be.dto.auth.product;

import org.ffb_be.entity.FoodOption;

import java.time.LocalDate;

public class ProductCreateDTO {
    private String name;
    private String description;
    private LocalDate expiryDate;
    private FoodOption foodOption;
    private String supplier;
    private String manufacturer;
}
