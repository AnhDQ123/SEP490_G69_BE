package org.ffb_be.dto.auth.userDto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private Long id;
    private String gender;
    private LocalDate dob;
    private String name;
    private String address;
}
