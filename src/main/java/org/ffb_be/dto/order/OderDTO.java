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
public class OderDTO {
    private Long id;
    private Long ownerId;
    private Long shipperId;
    private Long shipMethodId;
    private Long voucherId;
    private String address;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItem;
}
