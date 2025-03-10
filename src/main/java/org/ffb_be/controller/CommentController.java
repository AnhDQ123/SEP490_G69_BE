package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.service.comment.CommentService;
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
                                              @RequestParam int offset,
                                              @RequestParam int limit) {
        return commentService.getCommentsByBlogId(blogId, offset, limit);
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

}
