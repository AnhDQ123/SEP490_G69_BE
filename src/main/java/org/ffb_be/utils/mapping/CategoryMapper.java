package org.ffb_be.utils.mapping;

import org.ffb_be.dto.category.CategoryDTO;
import org.ffb_be.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);

    Category toEntity(CategoryDTO categoryDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntity(CategoryDTO categoryDTO,@MappingTarget Category category);
}
