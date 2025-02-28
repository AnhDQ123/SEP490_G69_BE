package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.service.blog.BlogService;
import org.ffb_be.utils.constants.PagingConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> createBlog(
            @Validated @RequestBody BlogDTO blogDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        blogService.createBlog(blogDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(
            @PathVariable Long id,
            @Validated @RequestBody BlogDTO blogDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        blogService.updateBlog(id, blogDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}
