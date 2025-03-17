package org.ffb_be.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemOptionDTO {
    private Long id;
    private Long optionId;
    private Long typeId;
    private String optionName;
    private String image;
    private Long cartItemId;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private int quantity;

}
