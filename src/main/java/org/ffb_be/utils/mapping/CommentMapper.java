package org.ffb_be.utils.mapping;

import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "replies", ignore = true)
    CommentDTO toDTO(Comment comment);

    default CommentDTO toDTOWithReplies(Comment comment, List<Comment> allReplies, int currentDepth, int maxDepth) {
        if (currentDepth >= maxDepth) {
            return toDTO(comment);
        }

        List<Comment> directReplies = allReplies.stream()
                .filter(c -> c.getParentComment() != null && c.getParentComment().getId().equals(comment.getId()))
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .limit(2)
                .toList();

        boolean hasMoreReplies = allReplies.stream()
                .anyMatch(c -> c.getParentComment() != null
                        && c.getParentComment().getId().equals(comment.getId())
                        && !directReplies.contains(c));

        CommentDTO dto = toDTO(comment);
        dto.setReplies(directReplies.stream()
                .map(c -> toDTOWithReplies(c, allReplies, currentDepth + 1, maxDepth))
                .collect(Collectors.toList()));
        dto.setHasMoreReplies(hasMoreReplies);
        return dto;
    }
}
