package org.ffb_be.utils.mapping;

import org.ffb_be.dto.auth.userDto.UserRoleProfile;
import org.ffb_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "shopStatus", source = "shop.isActive")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    UserRoleProfile toDTO(User user);
}
