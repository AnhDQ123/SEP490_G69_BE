package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.service.comment.CommentService;
import org.ffb_be.utils.enums.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{blogId}")
    public List<CommentDTO> getCommentsByBlog(@PathVariable Long blogId,
                                              @RequestParam(defaultValue = "0") int offset,
                                              @RequestParam(defaultValue = "10") int limit) {
        return commentService.getRootCommentsByBlogId(blogId, offset, limit);
    }

    @GetMapping("/comments/{parentId}/replies")
    public ResponseEntity<List<CommentDTO>> getReplies(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "3") int limit
    ) {
        List<CommentDTO> replies = commentService.getMoreReplies(parentId, offset, limit);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/replies")
    public List<CommentDTO> getMoreReplies(@RequestParam Long parentId,
                                           @RequestParam int offset,
                                           @RequestParam int limit) {
        return commentService.getMoreReplies(parentId, offset, limit);
    }

    @PostMapping
    public ResponseEntity<String> addComment(@RequestParam Long blogId, @RequestBody CommentDTO commentDTO) {
        commentService.addComment(blogId, commentDTO);
        return ResponseEntity.ok("Comment added successfully");
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> toggleLikeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.toggleLikeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    @PutMapping("/{commentId}/active")
    public ResponseEntity<String> activeComment(@PathVariable Long commentId) {
        commentService.commentStatusUpdate(commentId, Status.ACTIVE, null);
        return ResponseEntity.ok("Comment activated successfully");
    }

    @PutMapping("/{commentId}/inactive")
    public ResponseEntity<String> inactiveComment(@PathVariable Long commentId, @RequestParam String reason) {
        commentService.commentStatusUpdate(commentId, Status.INACTIVE, reason);
        return ResponseEntity.ok("Comment deactivated successfully");
    }


}
