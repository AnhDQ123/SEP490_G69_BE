package org.ffb_be.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.utils.enums.Status;

import java.time.LocalTime;

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
    private LocalTime openTime;
    private LocalTime closeTime;
    private String registrationCertificate;
    private String foodSafetyCertificate;
    private String address;
    private Status isActive;
    private Double rate;
    private String reason;
    private int viewCount;
    private Boolean isShipping;
    private Boolean isOpening;
    private OwnerDTO owner;
    private Double longitude;
    private Double latitude;

}