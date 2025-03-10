package org.ffb_be.dto.auth.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OwnerDTO {
    private String username;
    private String email;
    private String phone;
    private BusinessProfileDTO profile;
}