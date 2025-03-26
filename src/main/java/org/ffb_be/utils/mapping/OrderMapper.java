package org.ffb_be.utils.mapping;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.product.recommendation.OrderDataDTO;
import org.ffb_be.dto.product.recommendation.OrderItemDataDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.profile.name")
    @Mapping(target = "shipperId", source = "shipper.id")
    @Mapping(target = "shipperName", source = "shipper.profile.name")
    @Mapping(target = "shipperPhone", source = "shipper.phone")
    @Mapping(target = "shipMethodId", source = "deliveryMethod.id")
    @Mapping(target = "shipMethodName", source = "deliveryMethod.name")
    @Mapping(target = "shippingFee", source = "deliveryMethod.fee")
    @Mapping(target = "paymentMethodId", source = "paymentMethod.id")
    @Mapping(target = "paymentMethodName", source = "paymentMethod.name")
    @Mapping(target = "voucherId", source = "voucher.id")
    @Mapping(target = "voucherAmount", source = "voucher.discountValue")
    @Mapping(target = "address", source = "shippingAddress")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "orderItem", source = "orderItems")
    OrderDTO toDTO(Order order);

    OrderDataDTO toDTOData(Order order);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.owner.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    OrderItemDataDTO toDTO(OrderItem orderItem);
}
