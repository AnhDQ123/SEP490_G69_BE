package org.ffb_be.service.feedback;

import org.ffb_be.dto.feedback.FeedbackDTO;
import org.ffb_be.repository.FeedbackRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Page<FeedbackDTO> findAll(Long id,Pageable pageable) {
        return feedbackRepository.findAllByProduct_Id(id,pageable).map(feedback -> {
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            feedbackDTO.setId(feedback.getId());
            feedbackDTO.setContent(feedback.getContent());
            feedbackDTO.setImage(feedback.getImage());
            feedbackDTO.setCreatedAt(feedback.getCreatedAt());
            feedbackDTO.setStatus(feedback.getStatus().toString());
            BeanUtils.copyProperties(feedback, feedbackDTO);
            return feedbackDTO;
        });
    }
}
