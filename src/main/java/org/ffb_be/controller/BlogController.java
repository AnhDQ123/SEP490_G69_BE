package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.service.blog.BlogService;
import org.ffb_be.utils.constants.PagingConstant;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin("*")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogDTO>> getBlogs(
            Pageable pageable) {
        return ResponseEntity.ok(blogService.getBlogs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogDTO> getBlogById(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBlog(
            @Validated @ModelAttribute BlogDTO blogDTO,
            BindingResult result,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        blogService.createBlog(blogDTO, files);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBlog(
            @PathVariable Long id,
            @Validated @ModelAttribute BlogDTO blogDTO,
            BindingResult result,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        blogService.updateBlog(id, blogDTO, files);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{blogId}/like")
    public ResponseEntity<?> toggleLikeBlog(@PathVariable Long blogId, @RequestParam Long userId) {
        blogService.toggleLike(blogId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<?> activeBlog(@PathVariable Long id) {
        blogService.blogStatusUpdate(id, Status.ACTIVE, null);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/inactive")
    public ResponseEntity<?> inactiveBlog(@PathVariable Long id, @RequestParam String reason) {
        blogService.blogStatusUpdate(id, Status.INACTIVE, reason);
        return ResponseEntity.ok().build();
    }
}
