package org.ffb_be.dto.voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.DiscountType;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoucherDTO {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Integer totalVouchers;
    private Integer usedVouchers;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;
    private Integer maxUsagePerCustomer;
    private Boolean isStackable;
}
