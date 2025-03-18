package org.ffb_be.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private int quantity;
    private BigDecimal total;
    private List<OrderItemOptionDTO> orderItemOptions;
}
