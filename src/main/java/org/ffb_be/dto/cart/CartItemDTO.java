package org.ffb_be.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private int quantity;
    private List<CartItemOptionDTO> cartItemOptionDTOList;
}
