package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.service.blog.BlogService;
import org.ffb_be.utils.constants.PagingConstant;
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
            @RequestParam(defaultValue = "" + PagingConstant.PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "" + PagingConstant.PAGE_SIZE) int limit) {
        return ResponseEntity.ok(blogService.getBlogs(page, limit));
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
}
