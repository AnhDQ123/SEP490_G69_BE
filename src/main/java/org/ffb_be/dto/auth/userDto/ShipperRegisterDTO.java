package org.ffb_be.dto.auth.userDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShipperRegisterDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name cannot be longer than 50 characters")
    private String name;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotBlank(message = "Bank code cannot be blank")
    private String bankCode;

    @NotBlank(message = "Citizen ID number cannot be blank")
    @Pattern(regexp = "^[0-9]{12}$", message = "Citizen ID number must be 12 digits")
    private String citizenIDNumber;

    @NotNull(message = "Citizen ID expiration date cannot be null")
    private LocalDate citizenIDExpiredDate;

    @NotNull(message = "Driving license expiration date cannot be null")
    private LocalDate drivingLicenseExpiredDate;
}
