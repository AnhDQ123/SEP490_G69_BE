package org.ffb_be.utils.mapping;

import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OwnerMapper {

    OwnerMapper INSTANCE = Mappers.getMapper(OwnerMapper.class);

    @Mapping(source = "profile", target = "profile")
    @Mapping(source = "profile.taxCode", target = "profile.taxCode")
    OwnerDTO toDTO(User owner);
}