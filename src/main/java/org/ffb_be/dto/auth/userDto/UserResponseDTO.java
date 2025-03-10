package org.ffb_be.dto.auth.userDto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private String name;
    private String phone;
    private String email;
    private String username;
    private String role;
    private String status;
    private String created_at;
    private String avatar;

}
