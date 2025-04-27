package org.ffb_be.repository;


import org.ffb_be.entity.Category;
import org.ffb_be.entity.Product;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByShop_Id(Long shopId,Pageable pageable);

    Optional<Product> findById(Long id);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:product%")
    List<Product> findSimilarProducts(String product);

    List<Product> findAllByCategory(Category category);

    List<Product> findByIdIn(List<Long> ids);
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.unitPrice) as totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.createdAt >= CURRENT_DATE " +  // Ngày hôm nay
            "AND o.shop.id = :shopId " +  // Lọc theo shopId
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsToday(Long shopId);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.unitPrice) as totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.createdAt >= :startOfMonth " +  // Ngày đầu tháng
            "AND o.createdAt <= :endOfMonth " +    // Ngày cuối tháng
            "AND o.shop.id = :shopId " +  // Lọc theo shopId
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsThisMonth(Long shopId,
                                                   LocalDateTime startOfMonth,
                                                   LocalDateTime endOfMonth);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.unitPrice) as totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE FUNCTION('YEAR', o.createdAt) = FUNCTION('YEAR', CURRENT_DATE) " +  // Lọc theo năm hiện tại
            "AND o.shop.id = :shopId " +  // Lọc theo shopId
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsThisYear(Long shopId);

    List<Product> getByStatus(Status status);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIds(List<Long> ids);

    List<Product> getByShop_Id(Long shopId);
}

