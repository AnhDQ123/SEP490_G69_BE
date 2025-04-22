package org.ffb_be.service.comment;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Comment;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.BlogRepository;
import org.ffb_be.repository.CommentRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.mapping.CommentMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDTO> getRootCommentsByBlogId(Long blogId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("createdAt").descending());
        List<Comment> rootComments = commentRepository.findRootCommentsByBlogId(blogId, pageable);

        // Kiểm tra nếu có thêm comment gốc
        boolean hasMoreRootComments = commentRepository.countByBlogId(blogId) > (offset + limit);

        return rootComments.stream()
                .map(comment -> {
                    CommentDTO dto = commentMapper.toDTO(comment);
                    WriterDTO writerDTO = commentMapper.toWriterDTO(comment.getWriter());
                    dto.setWriter(writerDTO);

                    // Tính replyCount cho comment gốc
                    int replyCount = commentRepository.countByParentCommentId(comment.getId());
                    dto.setReplyCount(replyCount);

                    // Kiểm tra có thêm reply cho comment gốc không
                    dto.setHasMoreReplies(replyCount > (limit - 1));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getMoreReplies(Long parentId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("createdAt").descending());
        List<Comment> replies = commentRepository.findByParentCommentId(parentId, pageable);

        // Kiểm tra nếu có thêm reply
        boolean hasMoreReplies = commentRepository.countByParentCommentId(parentId) > (offset + limit);

        return replies.stream()
                .map(comment -> {
                    CommentDTO dto = commentMapper.toDTO(comment);
                    WriterDTO writerDTO = commentMapper.toWriterDTO(comment.getWriter());
                    dto.setWriter(writerDTO);

                    // Gán thông tin về việc có thêm reply không
                    dto.setHasMoreReplies(hasMoreReplies);

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public void addComment(Long blogId, CommentDTO commentDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));
        User user = userRepository.findById(commentDTO.getWriter().getId())
                .orElseThrow(() -> new NotFoundException("User"));
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setBlog(blog);
        comment.setWriter(user);

        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment"));

            if (!blogId.equals(parentComment.getBlog().getId())) {
                throw new BadRequestException("Blog id mismatch with parent comment");
            }
            if (parentComment.getParentComment() != null) {
                comment.setParentComment(parentComment.getParentComment());
            } else {
                comment.setParentComment(parentComment);
            }
        }
        commentRepository.save(comment);
    }


    @Override
    public void toggleLikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Set<Long> likedUserSet = new HashSet<>();
        if (comment.getLikedUsers() != null) {
            likedUserSet.addAll(Arrays.stream(comment.getLikedUsers().split(","))
                    .map(Long::parseLong).collect(Collectors.toSet()));
        }

        if (likedUserSet.contains(userId)) {
            likedUserSet.remove(userId);
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            likedUserSet.add(userId);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        comment.setLikedUsers(likedUserSet.isEmpty() ? null : likedUserSet.stream()
                .map(String::valueOf).collect(Collectors.joining(",")));

        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void commentStatusUpdate(Long id, Status status, String reason) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment"));
        if (status == Status.ACTIVE) {
            comment.setReason(null);
        }
        if (status == Status.INACTIVE) {
            comment.setReason(reason);
        }
        comment.setStatus(status);
        commentRepository.save(comment);
    }
}
