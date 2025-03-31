package org.ffb_be.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderCode;
    private Long ownerId;
    private String ownerName;
    private Long shipperId;
    private String shipperName;
    private String shipperPhone;
    private Long shipMethodId;
    private String shipMethodName;
    private Long paymentMethodId;
    private String paymentMethodName;
    private Long voucherId;
    private BigDecimal voucherAmount;
    private String address;
    private String shopName;
    private Long shopId;
    private String status;
    private String image;
    private String paymentProof;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItem;
    private String reason;
    private String phone;
    private String shopAddress;


}
