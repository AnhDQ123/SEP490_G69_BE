package org.ffb_be.service.comment;

import lombok.AllArgsConstructor;
import org.ffb_be.entity.Comment;
import org.ffb_be.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;

    @Override
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Override
    public Comment updateComment(Long id, Comment commentDetails) {
        return null;
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public List<Comment> getComments(int page, int limit) {
        return List.of();
    }

    @Override
    public List<Comment> getCommentsByBlogId(Long blogId, int page, int limit) {
        commentRepository.findCommentsByBlogId(blogId);
        return List.of();
    }
}
