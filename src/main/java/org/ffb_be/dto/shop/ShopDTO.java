
package org.ffb_be.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.OwnerDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO {
    private String name;
    private String description;
    private String logo;
    private String backgroundImage;
    private String phone;
    private String address;
    private int rate;
    private int view_count;
    private Boolean is_shipping;
    private Boolean is_opening;
    private OwnerDTO owner;
}