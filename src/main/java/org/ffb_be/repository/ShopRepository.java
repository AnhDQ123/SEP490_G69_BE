package org.ffb_be.repository;


import org.ffb_be.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ShopRepository extends JpaRepository<Shop, Long>, JpaSpecificationExecutor<Shop> {

    Page<Shop> findAll(Specification<Shop> specification, Pageable pageable);

    boolean existsByOwnerId(Long userId);


    @Query("SELECT s FROM Shop s JOIN s.products p WHERE p.id = :productId")
    Shop findByProduct( Long productId);

    Optional<Shop> findByOwnerId(Long userId);

    @Query("SELECT s.createdAt, COUNT(s) FROM Shop s WHERE s.isActive = :status " +
            "AND s.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY s.createdAt ORDER BY s.createdAt")
    List<Object[]> countShopsByStatusAndDay(String status,
                                             LocalDateTime startDate,
                                             LocalDateTime endDate);
    @Query("SELECT FUNCTION('MONTH', s.createdAt), COUNT(s) FROM Shop s " +
            "WHERE s.isActive = :status AND s.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('MONTH', s.createdAt) " +
            "ORDER BY FUNCTION('MONTH', s.createdAt)")
    List<Object[]> countShopsByStatusAndMonth(String status,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);

    @Query("SELECT FUNCTION('YEAR', s.createdAt), COUNT(s) FROM Shop s " +
            "WHERE s.isActive = :status AND s.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', s.createdAt) " +
            "ORDER BY FUNCTION('YEAR', s.createdAt)")
    List<Object[]> countShopsByStatusAndYear( String status,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate);
    @Query("SELECT COUNT(u) FROM Shop u WHERE u.isActive = 'PENDING'")
    long countPendingShop();
}
