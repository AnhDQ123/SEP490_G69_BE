package org.ffb_be.repository;

import org.ffb_be.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();
}
