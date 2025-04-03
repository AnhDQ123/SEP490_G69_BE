package org.ffb_be.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String accountNumber;
    private String bankCode;
    private LocalTime openTime;
    private LocalTime closeTime;
}
