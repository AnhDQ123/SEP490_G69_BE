package org.ffb_be.dto.product.recommendation;

import lombok.Data;

@Data
public class FeedbackDataDTO {
    private Long userId;
    private Long productId;
    private Double rate = 0.0;
}
