package org.ffb_be.dto.auth.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.entity.FoodOption;

import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String manufacturer;
    private String supplier;
    private int quantity;
    private String status;
    private BigDecimal discount;
    private String image;
    private FoodOption foodOption;
}
