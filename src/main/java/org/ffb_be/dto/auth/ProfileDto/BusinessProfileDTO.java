package org.ffb_be.dto.auth.ProfileDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BusinessProfileDTO {
    private Long id;
    private String name;
    private String address;
    private String avatar;
    private String tax_code;
    private String citizenIDNumber;
    private String citizenIDCardFront;
    private String citizenIDCardBack;
    private String drivingLicense;
    private LocalDate citizenIDExpiredDate;
}
