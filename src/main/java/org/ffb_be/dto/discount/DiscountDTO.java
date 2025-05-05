package org.ffb_be.dto.discount;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import lombok.Data;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;

@Validated
@Data
public class DiscountDTO {
    private Long id;

    @DecimalMin(value = "0.01", message = "Discount amount must be greater than 0")
    @DecimalMax(value = "1.00", message = "Discount amount must be less than or equal to 1")
    private BigDecimal amount;
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;
    @Future(message = "Start date must be in the future")
    private LocalDate endDate;

    private String status;
    private ProductResponseDTO productResponseDTO;


}
