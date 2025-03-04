package org.ffb_be.service.comment;

import org.ffb_be.dto.comment.CommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByBlogId(Long blogId);


    List<CommentDTO> getMoreReplies(Long parentId, int offset, int limit);

    void addComment(Long blogId, CommentDTO commentDTO);

    void deleteComment(Long commentId);
}
