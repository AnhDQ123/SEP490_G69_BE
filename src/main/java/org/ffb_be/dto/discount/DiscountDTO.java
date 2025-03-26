package org.ffb_be.dto.discount;

import lombok.Data;
import org.ffb_be.dto.product.ProductResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private ProductResponseDTO productResponseDTO;


}
