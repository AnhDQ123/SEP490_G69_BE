package org.ffb_be.dto.auth.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ShipperStatus;
import org.ffb_be.utils.enums.Status;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRoleProfile {
    private Long id;
    private Long shopId;
    private Status shopStatus;
    private Long roleId;
    private String roleName;
    private ShipperStatus shipperStatus;
}
