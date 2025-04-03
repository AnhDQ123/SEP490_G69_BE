package org.ffb_be.dto.feedback;

import lombok.Data;
import org.ffb_be.dto.image.ImageDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeedbackDTO {
    private Long id;
    private String content;
    private String status;
    private Double rate;
    private List<ImageDTO> image;
    private LocalDateTime createdAt;

}