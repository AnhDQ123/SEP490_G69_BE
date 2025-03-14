package org.ffb_be.utils.mapping;

import org.ffb_be.dto.product.recommendation.OrderDataDTO;
import org.ffb_be.dto.product.recommendation.OrderItemDataDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "owner.id", target = "userId")
    OrderDataDTO toDTO(Order order);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.owner.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    OrderItemDataDTO toDTO(OrderItem orderItem);
}
