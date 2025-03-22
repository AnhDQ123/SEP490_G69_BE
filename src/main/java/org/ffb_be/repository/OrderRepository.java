package org.ffb_be.repository;

import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    List<Order> findAllByOwner_IdAndStatus(Long ownerId, OrderStatus status);
    @Override
    Optional<Order> findById(Long aLong);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.shop.id = :shopId AND o.status = :status")
    List<Order> findOrdersByShopIdAndStatus( Long shopId, OrderStatus status);

}
