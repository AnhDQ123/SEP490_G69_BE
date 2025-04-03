package org.ffb_be.dto.banner;

import lombok.Data;

@Data
public class BannerDTO {
    private Long bannerId;
    private String url;
    private Long shopId;
    private String status;
}
