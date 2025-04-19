package org.ffb_be.service.comment;

import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.utils.enums.Status;

import java.util.List;

public interface CommentService {

    List<CommentDTO> getRootCommentsByBlogId(Long blogId, int offset, int limit);

    List<CommentDTO> getMoreReplies(Long parentId, int offset, int limit);

    void addComment(Long blogId, CommentDTO commentDTO);

    void toggleLikeComment(Long commentId, Long userId);

    void deleteComment(Long commentId);

    void commentStatusUpdate(Long id, Status status, String reason);
}
