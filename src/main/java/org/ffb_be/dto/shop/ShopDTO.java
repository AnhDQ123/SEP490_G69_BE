
package org.ffb_be.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.utils.enums.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String backgroundImage;
    private String menu;
    private String phone;
    private String address;
    private int rate;
    private int viewCount;
    private Status status;
    private Boolean isShipping;
    private Boolean isOpening;
    private OwnerDTO owner;
}