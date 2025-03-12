package org.ffb_be.dto.auth.ProfileDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDTO {
    private Long id;
    private String name;
    private String address;
    private String avatar;
    private LocalDateTime createdAt;;
    private LocalDate dob;
    private String phone;
    private String tax_code;
    private String citizenIDNumber;
    private String citizenIDCardFront;
    private String citizenIDCardBack;
    private LocalDate citizenIDExpiredDate;

}
