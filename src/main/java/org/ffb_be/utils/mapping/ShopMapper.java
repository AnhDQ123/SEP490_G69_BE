package org.ffb_be.utils.mapping;

import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {OwnerMapper.class})
public interface ShopMapper {
    @Mapping(target = "isActive", source = "isActive")
    ShopDTO toDTO(Shop shop);

    Shop toEntity(ShopRegisterDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "bankCode", ignore = true)
    void updateShopFromDTO(ShopRegisterDTO dto, @MappingTarget Shop shop);
}