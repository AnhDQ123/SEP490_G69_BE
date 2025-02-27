package org.ffb_be.dto.auth.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDTO {
    private String phone;
    private String email;
    private String username;
    private String password;
}
