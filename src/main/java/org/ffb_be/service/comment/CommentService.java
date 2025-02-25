package org.ffb_be.service.comment;

import org.ffb_be.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment createComment(Comment comment);

    Comment getCommentById(Long id);

    Comment updateComment(Long id, Comment commentDetails);

    void deleteComment(Long id);

    List<Comment> getComments(int page, int limit);

    List<Comment> getCommentsByBlogId(Long blogId, int page, int limit);
}
