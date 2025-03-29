package org.ffb_be.dto.report;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ReportViewDTO {
    private Long id;
    private String reportName;
    private String reportType;
    private String reason;
    private LocalDateTime createdAt;
    private String status;
}
