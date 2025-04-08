package org.ffb_be.dto.auth.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShipperRegisterDTO {
    private String name;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String email;
    private String accountNumber;
    private String bankCode;
    private String citizenIDNumber;
    private LocalDate citizenIDExpiredDate;
    private LocalDate drivingLicenseExpiredDate;
}
