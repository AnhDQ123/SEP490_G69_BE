package org.ffb_be.service.comment;

public interface CommentService {
    Comment createComment(Comment comment);

    Comment getCommentById(Long id);

    Comment updateComment(Long id, Comment commentDetails);

    void deleteComment(Long id);
}
