package org.ffb_be.repository;

import org.ffb_be.entity.Order;
import org.ffb_be.entity.Voucher;
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


    @Override
    Optional<Order> findById(Long aLong);

    int countByOwnerIdAndVoucher(Long ownerId, Voucher voucher);

    @Query("SELECT v.code, COUNT(o) FROM Order o JOIN o.voucher v GROUP BY v.code")
    List<Object[]> getVoucherUsageStatistics();
}
