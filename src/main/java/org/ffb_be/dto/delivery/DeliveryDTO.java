package org.ffb_be.dto.delivery;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.entity.BaseEntity;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDTO {
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
    private BigDecimal fee;
    private LocalDateTime createdAt;
}
