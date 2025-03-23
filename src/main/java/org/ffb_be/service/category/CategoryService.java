package org.ffb_be.service.category;

import org.ffb_be.dto.category.CategoryDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<CategoryDTO> findAll();
}
