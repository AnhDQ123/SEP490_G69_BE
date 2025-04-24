package org.ffb_be.dto.image;

import lombok.Data;
import org.ffb_be.utils.enums.Status;

@Data
public class ImageDTO {
    private String url;
    private Long relatedId;
    private Long ownerId;
    private Long id;
    private Long typeId;
    private Status status;
}
