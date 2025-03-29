package org.ffb_be.repository;

import org.ffb_be.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);

    @Query("SELECT c FROM Category c WHERE :search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Category> findAllByName(@Param("search") String search, Pageable pageable);

    Category findByName(String name);
}
