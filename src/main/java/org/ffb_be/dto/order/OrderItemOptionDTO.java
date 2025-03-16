package org.ffb_be.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.N;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@N
public class OrderItemOptionDTO {
    private Long id;
    private Long orderItemId;
    private Long optionId;
    private BigDecimal price;
    private BigDecimal total;
    private int quantity;
}
