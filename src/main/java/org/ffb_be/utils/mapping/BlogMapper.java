package org.ffb_be.utils.mapping;

import org.ffb_be.dto.auth.blog.BlogDTO;
import org.ffb_be.dto.auth.comment.CommentDTO;
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
    @Mapping(target = "comments", ignore = true)
    BlogDTO toDTO(Blog blog);

    default BlogDTO toDTO(Blog blog, List<String> imageUrls, List<Comment> comments) {
        BlogDTO dto = toDTO(blog);
        dto.setImageUrls(imageUrls);
        dto.setComments(comments.stream().map(this::toCommentDTO).collect(Collectors.toList()));
        return dto;
    }

    default BlogDTO toDTOWithImagesAndComment(Blog blog, List<Image> images, Comment topComment) {
        BlogDTO dto = toDTO(blog);
        List<String> imageUrls = images.stream().map(Image::getUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        if (topComment != null) {
            dto.setComments(List.of(toCommentDTO(topComment)));
        } else {
            dto.setComments(List.of());
        }

        return dto;
    }

    default BlogDTO toDTOWithFullComments(Blog blog, List<Image> images, List<Comment> comments) {
        BlogDTO dto = toDTOWithImagesAndComment(blog, images, null);
        dto.setComments(comments.stream().map(this::toCommentDTO).collect(Collectors.toList()));
        return dto;
    }

    default CommentDTO toCommentDTO(Comment comment) {
        List<CommentDTO> replies = comment.getReplies().stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());

        return new CommentDTO(comment.getId(), comment.getContent(), comment.getWriter().getUsername(), replies);
    }

    Blog toEntity(BlogDTO blogDTO);

    default List<Comment> toEntities(List<CommentDTO> commentDTOs, Blog blog) {
        return commentDTOs.stream()
                .map(dto -> toCommentEntity(dto, blog))
                .collect(Collectors.toList());
    }

    default Comment toCommentEntity(CommentDTO dto, Blog blog) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setBlog(blog);
        return comment;
    }

    void updateEntity(BlogDTO blogDTO, @MappingTarget Blog blog);
}
