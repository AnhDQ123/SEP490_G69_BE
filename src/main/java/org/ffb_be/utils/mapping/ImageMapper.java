package org.ffb_be.utils.mapping;

import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "typeId", source = "type.id")
    ImageDTO toDTO(Image image);

    Image toEntity(ImageDTO imageDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ImageDTO imageDTO,@MappingTarget Image image);
}
