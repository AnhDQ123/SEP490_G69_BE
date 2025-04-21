package org.ffb_be.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ShopRegisterDTO {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 20, message = "Name cannot be longer than 20 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10  digits")
    private String phone;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "Tax code cannot be blank")
//    @Pattern(regexp = "^[0-9]{10}$", message = "Tax code must be 10 digits")
    private String taxCode;

    @NotBlank(message = "Citizen ID number cannot be blank")
    @Pattern(regexp = "^[0-9]{12}$", message = "Citizen ID number must be 12 digits")
    private String citizenIDNumber;

    @NotNull(message = "Citizen ID expiration date cannot be null")
    private LocalDate citizenIDExpiredDate;

    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotBlank(message = "Bank code cannot be blank")
    private String bankCode;

    @NotNull(message = "Opening time cannot be null")
    private LocalTime openTime;

    @NotNull(message = "Closing time cannot be null")
    private LocalTime closeTime;
}
