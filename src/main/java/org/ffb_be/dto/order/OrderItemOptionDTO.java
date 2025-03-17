package org.ffb_be.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemOptionDTO {
    private Long id;
    private Long orderItemId;
    private Long optionId;
    private Long typeId;
    private BigDecimal price;
    private BigDecimal total;
    private int quantity;
}
