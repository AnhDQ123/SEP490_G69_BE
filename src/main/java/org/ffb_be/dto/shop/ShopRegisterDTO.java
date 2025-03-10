package org.ffb_be.dto.shop;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.utils.enums.SellType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopRegisterDTO {
    private String name;
    private String description;
    private String phone;
    private String address;
    private String taxCode;
    private String citizenIDNumber;
    private LocalDate citizenIDExpiredDate;
    private SellType sellType;
    private LocalTime openTime;
    private LocalTime closeTime;
}
