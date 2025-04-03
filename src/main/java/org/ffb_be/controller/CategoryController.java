package org.ffb_be.controller;

import org.ffb_be.dto.category.CategoryDTO;
import org.ffb_be.service.category.CategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategory(@RequestParam(value = "search", defaultValue = "", required = false) String search,
                                            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(categoryService.findAll(search, pageable));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.findById(categoryId));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @Validated @ModelAttribute CategoryDTO category,
            @RequestPart(value = "file", required = false) MultipartFile file,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        categoryService.create(category, file);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long categoryId,
            @Validated @ModelAttribute CategoryDTO category,
            @RequestPart(value = "file", required = false) MultipartFile file,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        categoryService.update(categoryId, category, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok().build();
    }
}
