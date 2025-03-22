package org.ffb_be.dto.image;

import lombok.Data;

@Data
public class ImageDTO {
    private String url;
    private Long relatedId;
    private Long ownerId;
    private Long id;
    private Long typeId;
}
