package org.ffb_be.repository;

import org.ffb_be.entity.Order;
import org.ffb_be.entity.Shop;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.ffb_be.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    int countByOwnerIdAndVoucher(Long ownerId, Voucher voucher);

    @Query("SELECT v.code, COUNT(o) FROM Order o JOIN o.voucher v GROUP BY v.code")
    List<Object[]> getVoucherUsageStatistics();

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

    @Query("""
        SELECT o.status, COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE p.shop.id = :shopId
        GROUP BY o.status
    """)
    List<Object[]> countOrdersByStatusForShop( Long shopId);

    @Query("SELECT o FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:orderCode IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :orderCode, '%')))")
    Page<Order> findByStatusAndCreatedAtBetween(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("orderCode") String orderCode,
            Pageable pageable);

    @Query("SELECT o.createdAt, COUNT(o) FROM Order o WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY o.createdAt ORDER BY o.createdAt")
    List<Object[]> countOrdersByStatusAndDay( OrderStatus status,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate);

    @Query("SELECT FUNCTION('MONTH', o.createdAt), FUNCTION('YEAR', o.createdAt), COUNT(o) " +
            "FROM Order o WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> countOrdersByStatusAndMonth( OrderStatus status,
                                                LocalDateTime startDate,
                                                LocalDateTime endDate);
    @Query("SELECT FUNCTION('YEAR', o.createdAt), COUNT(o) FROM Order o " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> countOrdersByStatusAndYear( OrderStatus status,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) AS totalQuantity, SUM(oi.unitPrice) AS totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.createdAt >= :startDate " +
            "AND o.createdAt < :endDate " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsToday(LocalDateTime startDate, LocalDateTime endDate,Pageable pageable);
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.unitPrice) AS totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE FUNCTION('YEAR', o.createdAt) = FUNCTION('YEAR', CURRENT_DATE) " +  // Lọc theo năm hiện tại
            "AND FUNCTION('MONTH', o.createdAt) = FUNCTION('MONTH', CURRENT_DATE) " +  // Lọc theo tháng hiện tại
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsThisMonth(Pageable pageable);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.unitPrice) AS totalValue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE FUNCTION('YEAR', o.createdAt) = FUNCTION('YEAR', CURRENT_DATE) " +  // Lọc theo năm hiện tại
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsThisYear(Pageable pageable);
    @Query("SELECT COUNT(o) FROM Order o")
    Long countAllOrders();
    @Query("SELECT COUNT(o) FROM Order o where o.status='RETURN_PENDING'")
    Long countReturnPendingOrders();
    @Query("SELECT DATE(o.createdAt), COUNT(o) FROM Order o " +
            "WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.shop.id = :shopId " +
            "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
    List<Object[]> countShopOrdersByStatusAndDay(OrderStatus status,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate,
                                                 Long shopId);
    @Query("SELECT FUNCTION('MONTH', o.createdAt), FUNCTION('YEAR', o.createdAt), COUNT(o) " +
            "FROM Order o WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.shop.id = :shopId " +
            "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> countShopOrdersByStatusAndMonth( OrderStatus status,
                                                LocalDateTime startDate,
                                               LocalDateTime endDate,
                                                Long shopId);
    @Query("SELECT FUNCTION('YEAR', o.createdAt), COUNT(o) " +
            "FROM Order o WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.shop.id = :shopId " +
            "GROUP BY FUNCTION('YEAR', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> countShopOrdersByStatusAndYear( OrderStatus status,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Long shopId);

    @Query("SELECT FUNCTION('DATE', o.createdAt) AS date, o.shop.id, SUM(o.total) " +
            "FROM Order o " +
            "WHERE FUNCTION('DATE', o.createdAt) BETWEEN FUNCTION('DATE', :startDate) AND FUNCTION('DATE', :endDate) " +  // Lọc theo ngày
            "AND o.shop.id = :shopId " +
            "GROUP BY FUNCTION('DATE', o.createdAt), o.shop.id")
    List<Object[]> calculateShopRevenueByDay(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             Long shopId);

    @Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), SUM(o.total) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.shop.id = :shopId " +
            "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> calculateShopRevenueByMonth( LocalDateTime startDate,
                                                LocalDateTime endDate,
                                                Long shopId);

    @Query("SELECT FUNCTION('YEAR', o.createdAt), SUM(o.total) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.shop.id = :shopId " +
            "GROUP BY FUNCTION('YEAR', o.createdAt) " +
            "ORDER BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> calculateShopRevenueByYear( LocalDateTime startDate,
                                              LocalDateTime endDate,
                                               Long shopId);

    List<Order> findAllByShop_IdAndStatus(Long shopId, OrderStatus status);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
    @Query("SELECT COUNT(u) " +
            "FROM Order u " +
            "WHERE u.createdAt >= :startDate " +
            "AND u.createdAt < :endDate")
    Double countOrderByMonth( LocalDateTime startDate,
                                      LocalDateTime endDate);
    Long countAllByStatus(OrderStatus status);

    Page<Order> findAllByShipper_IdAndStatus(Long shipperId, OrderStatus status, Pageable pageable);
    @Query("SELECT COUNT(u) " +
            "FROM Order u " +
            "WHERE u.shipper.id=:shipperId and u.createdAt >= :startDate " +
            "AND u.createdAt < :endDate")
    long countOrdersByShipper_Id(Long shipperId,LocalDateTime startDate,
                                 LocalDateTime endDate);
}
