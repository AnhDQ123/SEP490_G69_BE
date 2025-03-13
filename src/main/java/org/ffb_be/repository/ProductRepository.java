package org.ffb_be.repository;


import org.ffb_be.entity.Category;
import org.ffb_be.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByShop_Id(Long shopId,Pageable pageable);

    Optional<Product> findById(Long id);
    @Query("SELECT p FROM Product p JOIN p.shop s WHERE s.sellType = 'Fresh'")
    List<Product> findFreshProducts();

    @Query("SELECT p FROM Product p JOIN p.shop s WHERE s.sellType = 'Cooked'")
    List<Product> findCookedProducts();
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:product%")
    List<Product> findSimilarProducts(String product);

    List<Product> findAllByCategory(Category category);

    List<Product> findByIdIn(List<Long> ids);
}
