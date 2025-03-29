package org.ffb_be.service.category;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.category.CategoryDTO;

import org.ffb_be.entity.Category;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.CategoryRepository;

import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.CategoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<CategoryDTO> findAll(String name, Pageable pageable) {
        Page<Category> categories = categoryRepository.findAllByName(name, pageable);
        return categories.map(categoryMapper::toDTO);
    }

    @Override
    public CategoryDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
        return categoryMapper.toDTO(category);
    }

    @Override
    public void create(CategoryDTO categoryDTO, MultipartFile file) throws IOException {
        Category category = categoryMapper.toEntity(categoryDTO);
        if(file != null){
            String url = cloudinaryUpload.uploadFile(file);
            category.setImage(url);
        }
        category.setCreatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public void update(Long id, CategoryDTO categoryDTO, MultipartFile file) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
        categoryMapper.updateEntity(categoryDTO, category);
        if(file != null){
            String url = cloudinaryUpload.uploadFile(file);
            category.setImage(url);
        }
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
        categoryRepository.delete(category);
    }
}

