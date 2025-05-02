package org.ffb_be.dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopProductDTO {
    private String name;
    private Long totalQuantity;
    private BigDecimal totalValue;
    private String image;
}
