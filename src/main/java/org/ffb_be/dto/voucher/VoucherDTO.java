package org.ffb_be.dto.voucher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.ffb_be.utils.enums.DiscountType;
import org.ffb_be.utils.enums.Status;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
@Validated
@Data
public class VoucherDTO {
    private Long id;

    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @DecimalMax(value = "1.0", message = "Discount value must be less than or equal to 1 (for percentage)")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "Minimum order value must be greater than 0")
    private BigDecimal minOrderValue;

    @Min(value = 1, message = "Total vouchers must be greater than 0")
    private Integer totalVouchers;


    private Integer usedVouchers;

    @FutureOrPresent(message = "Start date must be in the future")
    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @NotNull(message = "Voucher status is required")
    private Status status;

    @Min(value = 1, message = "Max usage per customer must be greater than 0")
    private Integer maxUsagePerCustomer;

    @NotNull(message = "Stackability information is required")
    private Boolean isStackable;
}
