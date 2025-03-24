package org.ffb_be.repository;

import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();

    Page<Order> findAllByOwner_IdAndStatus(Long ownerId, OrderStatus status, Pageable pageable);
    @Override
    Optional<Order> findById(Long aLong);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.shop.id = :shopId AND o.status = :status")
    Page<Order> findOrdersByShopIdAndStatus( Long shopId, OrderStatus status,Pageable pageable);

    @Query("""
    SELECT o.shipper.id AS shipperId, COUNT(o.id) AS totalOrders
    FROM Order o
    WHERE o.createdAt >= CURRENT_DATE
    AND o.shipper IS NOT NULL
    GROUP BY o.shipper.id
    ORDER BY totalOrders ASC
""")
    List<Object[]> findShipperWithLeastOrdersToday();
    
    Page<Order> findAllByShipper_Id(Long shipperId, Pageable pageable);

    List<Order> findAllByStatus(OrderStatus status);

    Page<Order> findAllByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
