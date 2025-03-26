package org.ffb_be.dto.order;

import lombok.Data;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.entity.Image;

import java.util.List;

@Data
public class ReturnOrderDTO {
    private OrderDTO order;
    private List<ImageDTO> image;
}
