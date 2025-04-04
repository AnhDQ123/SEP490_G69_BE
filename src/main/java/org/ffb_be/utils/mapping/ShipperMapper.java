package org.ffb_be.utils.mapping;
import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipperMapper {

    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "dob", source = "profile.dob")
    @Mapping(target = "citizenIDNumber", source = "profile.citizenIDNumber")
    @Mapping(target = "citizenIDCardFront", source = "profile.citizenIDCardFront")
    @Mapping(target = "citizenIDCardBack", source = "profile.citizenIDCardBack")
    @Mapping(target = "drivingLicenseFront", source = "profile.drivingLicenseFront")
    @Mapping(target = "drivingLicenseBack", source = "profile.drivingLicenseBack")
    @Mapping(target = "judicialRecord", source = "profile.judicialRecord")
    @Mapping(target = "citizenIDExpiredDate", source = "profile.citizenIDExpiredDate")
    @Mapping(target = "drivingLicenseExpiredDate", source = "profile.drivingLicenseExpiredDate")
    ShipperInfoDTO toDTO(User shipper);
}
