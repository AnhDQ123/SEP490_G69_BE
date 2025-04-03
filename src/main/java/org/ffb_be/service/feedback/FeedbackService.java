package org.ffb_be.service.feedback;

import org.ffb_be.dto.feedback.FeedbackDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface FeedbackService {
    Page<FeedbackDTO> findAll(Long id, Pageable pageable);
    void create(Long userId,Long productId,FeedbackDTO feedbackDTO);
}
