package org.ffb_be.utils.mapping;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.profile.name")
    @Mapping(target = "shipperId", source = "shipper.id")
    @Mapping(target = "shipperName", source = "shipper.profile.name")
    @Mapping(target = "shipMethodId", source = "deliveryMethod.id")
    @Mapping(target = "paymentMethodId", source = "paymentMethod.id")
    @Mapping(target = "voucherId", source = "voucher.id")
//    @Mapping(target = "voucherAmount", source = "voucher.discount_percentage")
    @Mapping(target = "address", source = "shippingAddress")
    @Mapping(target = "shopName", source = "voucher.shop.name")
    @Mapping(target = "shopId", source = "voucher.shop.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "orderItem", source = "orderItems")
    OrderDTO toDTO(Order order);
}