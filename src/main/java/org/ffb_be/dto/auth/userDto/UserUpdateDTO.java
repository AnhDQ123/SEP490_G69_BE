package org.ffb_be.dto.auth.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
@Validated
@Data
public class UserUpdateDTO {
    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotBlank(message = "Bank code cannot be blank")
    private String bankCode;
    private Long id;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be before today")
    private LocalDate dob;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
