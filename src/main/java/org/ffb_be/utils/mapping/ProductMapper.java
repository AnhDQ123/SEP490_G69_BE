package org.ffb_be.utils.mapping;

import org.ffb_be.dto.product.recommendation.FoodDataDTO;
import org.ffb_be.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    FoodDataDTO toDTO(Product product);
}
