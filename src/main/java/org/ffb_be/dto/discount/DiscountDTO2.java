package org.ffb_be.dto.discount;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class DiscountDTO2 {
    private Long id;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
