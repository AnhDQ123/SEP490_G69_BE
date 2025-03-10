package org.ffb_be.repository;

import org.ffb_be.dto.category.CategoryDTO;
import org.ffb_be.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);

    @Query("SELECT new org.ffb_be.dto.category.CategoryDTO(c.id, c.name) FROM Category c")
    List<CategoryDTO> findAllCategories();
    Category findByName(String name);
}
