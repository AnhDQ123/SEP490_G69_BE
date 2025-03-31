package org.ffb_be.dto.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CountByDateDTO {
    private LocalDateTime date;
    private int count;
}
