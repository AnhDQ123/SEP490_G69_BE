package org.ffb_be.utils.mapping;

import org.ffb_be.dto.voucher.VoucherDTO;
import org.ffb_be.entity.Voucher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper {

    VoucherDTO toDTO(Voucher voucher);

    Voucher toEntity(VoucherDTO dto);
}
