package org.ffb_be.dto.discount;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class DiscountDTO2 {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}
