package org.ffb_be.dto.feedback;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ffb_be.dto.image.ImageDTO;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
@Validated
@Data
public class FeedbackDTO {
    private Long id;

    @NotBlank(message = "Trạng thái không được để trống")
    private String content;

    private String status;

    @NotNull(message = "Tỷ lệ không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tỷ lệ phải lớn hơn hoặc bằng 0")
    private Double rate;

    private List<ImageDTO> image;
    private LocalDateTime createdAt;

}