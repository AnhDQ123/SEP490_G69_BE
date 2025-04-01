package org.ffb_be.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportCreateDTO {
    private Long reportId;
    private Long userId;
    private Long relatedId;
    private Long typeId;
    private String reason;
    private LocalDateTime createdAt;
}
