package org.ffb_be.utils.mapping;

import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.entity.Shop;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OwnerMapper.class})
public interface ShopMapper {
    ShopDTO toDTO(Shop shop);
    Shop toEntity(ShopRegisterDTO dto);
    ShopRegisterDTO toRegisterDTO(Shop shop);
}

