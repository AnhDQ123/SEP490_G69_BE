package org.ffb_be.dto.feedback;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Long id;
    private String content;
    private String status;
    private String image;
    private LocalDateTime createdAt;

}
