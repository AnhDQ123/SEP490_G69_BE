package org.ffb_be.dto.product.recommendation;

import lombok.Data;

@Data
public class OrderItemDataDTO {
    private Long orderId;
    private Long userId;
    private Long productId;
    private int quantity;
}
