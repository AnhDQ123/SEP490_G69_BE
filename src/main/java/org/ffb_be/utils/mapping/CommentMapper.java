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

    @Mapping(target = "parentId", source = "parentComment.id")
    @Mapping(target = "parentWriterName", source = "parentComment.writer.profile.name")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "avatarUrl", source = "profile.avatar")
    WriterDTO toWriterDTO(User user);
}
