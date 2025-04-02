package org.ffb_be.utils.mapping;

import org.ffb_be.dto.payment.PaymentDTO;
import org.ffb_be.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDTO toDTO(Payment payment);

    @Mapping(target = "id", ignore = true)
    Payment toEntity(PaymentDTO paymentDTO);
}
