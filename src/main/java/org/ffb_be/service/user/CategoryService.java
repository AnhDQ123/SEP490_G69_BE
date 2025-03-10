package org.ffb_be.service.user;

import org.ffb_be.dto.auth.category.CategoryDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<CategoryDTO> findAll();
}
