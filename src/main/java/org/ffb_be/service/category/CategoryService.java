package org.ffb_be.service.category;

import org.ffb_be.dto.category.CategoryDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public interface CategoryService {
    Page<CategoryDTO> findAll(String name, Pageable pageable);

    CategoryDTO findById(Long id);

    void create(CategoryDTO categoryDTO, MultipartFile file) throws IOException;

    void update(Long id, CategoryDTO categoryDTO, MultipartFile file) throws IOException;

    void delete(Long id);
}
