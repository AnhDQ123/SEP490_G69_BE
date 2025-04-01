package org.ffb_be.dto.CountDTOBy;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CountByDateDTO {
    private LocalDateTime date;
    private Long count;
}
