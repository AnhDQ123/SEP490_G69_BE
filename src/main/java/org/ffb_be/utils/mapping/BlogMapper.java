package org.ffb_be.utils.mapping;

import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Comment;
import org.ffb_be.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    @Mapping(target = "imageUrls", ignore = true)
    BlogDTO toDTO(Blog blog);

    default BlogDTO toDTOWithImages(Blog blog, List<Image> images) {
        BlogDTO dto = toDTO(blog);
        List<String> imageUrls = images.stream().map(Image::getUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);
        return dto;
    }

    Blog toEntity(BlogDTO blogDTO);

    void updateEntity(BlogDTO blogDTO, @MappingTarget Blog blog);
}
