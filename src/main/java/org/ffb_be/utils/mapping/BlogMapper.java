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
    @Mapping(target = "comments", ignore = true)
    BlogDTO toDTO(Blog blog);

    default BlogDTO toDTOWithImagesAndComments(Blog blog, List<Image> images, List<Comment> allComments) {
        BlogDTO dto = toDTO(blog);
        List<String> imageUrls = images.stream().map(Image::getUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        // Lấy danh sách comment gốc
        List<CommentDTO> topLevelComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> toCommentDTO(comment, allComments))
                .collect(Collectors.toList());

        dto.setComments(topLevelComments);
        return dto;
    }

    default CommentDTO toCommentDTO(Comment comment, List<Comment> allComments) {
        // Lấy tối đa 2 replies con
        List<CommentDTO> limitedReplies = allComments.stream()
                .filter(c -> c.getParentComment() != null && c.getParentComment().getId().equals(comment.getId()))
                .sorted((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt())) // Sắp xếp theo thời gian
                .limit(2)
                .map(c -> toCommentDTO(c, allComments))
                .collect(Collectors.toList());

        boolean hasMoreReplies = allComments.stream().anyMatch(c ->
                c.getParentComment() != null &&
                        c.getParentComment().getId().equals(comment.getId()) &&
                        !limitedReplies.contains(c)
        );

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
//                comment.getWriter().getUsername(),
                limitedReplies,
                hasMoreReplies
        );
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
