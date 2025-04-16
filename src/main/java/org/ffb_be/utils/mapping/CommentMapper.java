package org.ffb_be.utils.mapping;

import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Comment;
import org.ffb_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "hasMoreReplies", ignore = true)
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "avatarUrl", source = "profile.avatar")
    WriterDTO toWriterDTO(User user);

    default CommentDTO toDTOWithReplies(Comment comment, List<Comment> allReplies, int depth, int maxDepth) {
        if (depth >= maxDepth) return toDTO(comment);

        List<Comment> limitedReplies = allReplies.stream()
                .filter(c -> c.getParentComment() != null && c.getParentComment().getId().equals(comment.getId()))
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .limit(3)
                .toList();

        boolean hasMoreReplies = allReplies.stream().anyMatch(c -> c.getParentComment() != null
                && c.getParentComment().getId().equals(comment.getId())
                && !limitedReplies.contains(c));

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                toWriterDTO(comment.getWriter()),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                allReplies.size(),
                limitedReplies.stream().map(c -> toDTOWithReplies(c, allReplies, depth + 1, maxDepth)).collect(Collectors.toList()),
                hasMoreReplies
        );
    }
}
