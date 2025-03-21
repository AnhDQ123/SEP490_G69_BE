package org.ffb_be.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String manufacturer;
    private String supplier;
    private int quantity;
    private String shopName;
    private String category;
    private String status;
    private BigDecimal discount;
    private String image;
    private List<FoodOptionDTO> foodOption;
    private Float rate;
    private BigDecimal defaultPrice;
}
