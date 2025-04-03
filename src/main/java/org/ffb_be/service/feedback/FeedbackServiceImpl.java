package org.ffb_be.service.feedback;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.feedback.FeedbackDTO;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.entity.Feedback;
import org.ffb_be.entity.Image;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.Status;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public void create(Long userId,Long productId,FeedbackDTO feedbackDTO) {
        Feedback feedback=new Feedback();
        feedback.setId(feedbackDTO.getId());
        feedback.setContent(feedbackDTO.getContent());
        feedback.setRate(feedbackDTO.getRate());
        feedback.setProduct(productRepository.findById(productId).get());
        feedback.setStatus(Status.ACTIVE);
        feedback.setWriter(userRepository.findById(userId).get());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setShop(shopRepository.findByProduct(productId));
        feedbackRepository.save(feedback);
    }
    @Override
    public Page<FeedbackDTO> findAll(Long id, Pageable pageable) {
        return feedbackRepository.findAllByProduct_Id(id,pageable).map(feedback -> {
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            feedbackDTO.setId(feedback.getId());
            feedbackDTO.setRate(feedback.getRate());
            feedbackDTO.setContent(feedback.getContent());
            List<Image> imageList=imageRepository.findAllByRelatedIdAndType_Id(feedback.getId(),8l);
            List<ImageDTO> imageDTOList=new ArrayList<>();
            for (Image image : imageList) {
                ImageDTO imageDTO=new ImageDTO();
                imageDTO.setUrl(image.getUrl());
                imageDTO.setRelatedId(feedback.getId());
                imageDTO.setId(image.getId());
                imageDTO.setOwnerId(image.getOwnerId());
                imageDTO.setTypeId(8l);
                imageDTOList.add(imageDTO);
            }
            feedbackDTO.setImage(imageDTOList);
            feedbackDTO.setCreatedAt(feedback.getCreatedAt());
            feedbackDTO.setStatus(feedback.getStatus().toString());
            BeanUtils.copyProperties(feedback, feedbackDTO);
            return feedbackDTO;
        });
    }


}
