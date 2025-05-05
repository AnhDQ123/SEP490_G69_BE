package org.ffb_be.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.discount.DiscountDTO2;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private List<DiscountDTO2> discount;
    private String image;
    private List<FoodOptionDTO> foodOption;
    private String description;
    private Float rate;
    private BigDecimal defaultPrice;
    private String foodType;
    private Long shopId;
    private int reportCount;
    private LocalDate expiryDate;
}
