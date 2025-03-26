package org.ffb_be.dto.auth.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ShipperStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipperInfoDTO {
    private String name;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String email;
    private ShipperStatus shipperStatus;
    private String citizenIDNumber;
    private String citizenIDCardFront;
    private String citizenIDCardBack;
    private String drivingLicenseFront;
    private String drivingLicenseBack;
    private String judicialRecord;
    private LocalDate citizenIDExpiredDate;
    private LocalDate drivingLicenseExpiredDate;
}
