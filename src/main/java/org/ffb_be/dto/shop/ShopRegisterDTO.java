package org.ffb_be.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.utils.enums.SellType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopRegisterDTO {
    private String name;
    private String description;
    private String phone;
    private String address;
    private String registrationCertificate;
    private String foodSafetyCertificate;
    private Boolean isShipping;
    private Boolean isOpening;
    private SellType sellType;
    private String openTime;
    private BusinessProfileDTO profile;
}
