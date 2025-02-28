package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.comment.CommentDTO;
import org.ffb_be.service.comment.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs/{blogId}/comments")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDTO> getCommentsByBlog(@PathVariable Long blogId) {
        return commentService.getCommentsByBlogId(blogId);
    }

    @GetMapping("/replies/{parentId}")
    public List<CommentDTO> getMoreReplies(@PathVariable Long parentId) {
        return commentService.getMoreReplies(parentId);
    }

    @PostMapping
    public ResponseEntity<String> addComment(@PathVariable Long blogId, @RequestBody CommentDTO commentDTO) {
        commentService.addComment(blogId, commentDTO);
        return ResponseEntity.ok("Comment added successfully");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }

}
