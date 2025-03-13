package org.ffb_be.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long ownerId;
    private Long shipperId;
    private Long deliveryMethodId;
    private Long paymentMethodId;
    private Long voucherId;
    private List<OrderItemDTO> orderItemDTO;
    private BigDecimal total;
    private String status;

}
