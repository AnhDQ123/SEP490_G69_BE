package org.ffb_be.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private Long id;
    private Long userId;
    private Long shopId;
    private BigDecimal price;
    private String status;
    private List<CartItemDTO> cartItemDTOList;
}
