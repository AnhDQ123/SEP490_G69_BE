package org.ffb_be.service.comment;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Comment;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.BlogRepository;
import org.ffb_be.repository.CommentRepository;
import org.ffb_be.utils.mapping.CommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDTO> getCommentsByBlogId(Long blogId) {
        List<Comment> rootComments = commentRepository.findRootCommentsByBlogId(blogId);
        return rootComments.stream()
                .map(comment -> commentMapper.toDTOWithReplies(comment, rootComments, 0, 3))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getMoreReplies(Long parentId) {
        List<Comment> replies = commentRepository.findByParentCommentId(parentId)
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .collect(Collectors.toList());

        return replies.stream()
                .map(reply -> commentMapper.toDTOWithReplies(reply, replies, 0, 3)) // Giới hạn cấp reply
                .collect(Collectors.toList());
    }

    @Override
    public void addComment(Long blogId, CommentDTO commentDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setBlog(blog);
        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment"));
            comment.setParentComment(parentComment);
        }
        commentRepository.save(comment);
    }

    // 📌 Xóa comment
    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }
}
