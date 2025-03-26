package org.ffb_be.utils.mapping;

import org.ffb_be.dto.product.recommendation.FeedbackDataDTO;
import org.ffb_be.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "writer.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    FeedbackDataDTO toDTO(Feedback feedback);
}
