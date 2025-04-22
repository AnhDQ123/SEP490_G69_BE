package org.ffb_be.utils.mapping;

import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    @Mapping(target = "imageUrls", ignore = true)
    BlogDTO toDTO(Blog blog);

    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "avatarUrl", source = "profile.avatar")
    WriterDTO toWriterDTO(User user);

    default BlogDTO toDTOWithImages(Blog blog, List<Image> images) {
        BlogDTO dto = toDTO(blog);
        List<String> imageUrls = images.stream().map(Image::getUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);
        return dto;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "content", target = "content")
    Blog toEntity(BlogDTO blogDTO);

    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(BlogDTO blogDTO, @MappingTarget Blog blog);
}
