package org.ffb_be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipPaymentDTO {
    private Long shipperId;
    private String shipperName;
    private BigDecimal amount;
}
