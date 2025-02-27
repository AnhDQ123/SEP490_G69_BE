package org.ffb_be.service.comment;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Comment;
import org.ffb_be.repository.BlogRepository;
import org.ffb_be.repository.CommentRepository;
import org.ffb_be.utils.mapping.CommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final CommentMapper commentMapper;

    public List<CommentDTO> getCommentsByBlogId(Long blogId) {
        List<Comment> allComments = commentRepository.findByBlogId(blogId);
        return allComments.stream()
                .filter(comment -> comment.getParentComment() == null) // Chỉ lấy comment cha
                .map(comment -> commentMapper.toDTOWithReplies(comment, allComments, 0, 3)) // Tối đa 3 cấp
                .collect(Collectors.toList());
    }

    public List<CommentDTO> getMoreReplies(Long parentId) {
        List<Comment> replies = commentRepository.findByParentCommentId(parentId);

        return replies.stream()
                .map(reply -> commentMapper.toDTOWithReplies(reply, replies, 0, 3))
                .collect(Collectors.toList());
    }

    public CommentDTO addComment(Long blogId, Long parentId, String content, String username) {
        Comment newComment = new Comment();
        newComment.setContent(content);
        newComment.setBlog(blogRepository.findById(blogId).orElseThrow());
//        newComment.setWriter();

        if (parentId != null) {
            newComment.setParentComment(commentRepository.findById(parentId).orElseThrow());
        }

        Comment savedComment = commentRepository.save(newComment);
        return commentMapper.toDTO(savedComment);
    }
}
