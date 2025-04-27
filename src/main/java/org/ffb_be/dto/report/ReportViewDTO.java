package org.ffb_be.dto.report;

import lombok.Data;
import org.ffb_be.dto.image.ImageDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportViewDTO {
    private Long id;
    private String reportName;
    private String reportType;
    private Long reporterId;
    private Long reportedUserId;
    private Long reportItemId;
    private Long shipperId;
    private Long shopId;
    private String reason;
    private LocalDateTime createdAt;
    private String status;
    private List<ImageDTO> image;
}
