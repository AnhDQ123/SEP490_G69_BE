package org.ffb_be.dto.auth.ProfileDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDTO {
    private Long id;
    private String name;
    private String address;
    private String avatar;
}
