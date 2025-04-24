package org.ffb_be.utils.mapping;

import org.ffb_be.dto.delivery.DeliveryDTO;
import org.ffb_be.dto.payment.PaymentDTO;
import org.ffb_be.entity.DeliveryMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    DeliveryDTO toDTO(DeliveryMethod deliveryMethod);

    DeliveryMethod toEntity(DeliveryDTO deliveryDTO);
}
