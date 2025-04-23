package org.ffb_be.repository;

import org.ffb_be.entity.Role;
import org.ffb_be.entity.User;
import org.ffb_be.utils.enums.ShipperStatus;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT e FROM User e LEFT JOIN e.profile p " +
            "WHERE lower(p.name) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.phone) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.email) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.status) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.role.name) LIKE lower(concat('%', :search, '%')) ")
    Page<User>findByAllField( String search, Pageable pageable);

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    @Query("SELECT COUNT(u) " +
            "FROM User u " +
            "WHERE u.status = 'ACTIVE' " +
            "AND u.createdAt >= :startDate " +
            "AND u.createdAt < :endDate")
    Double countActiveUsersByMonth( LocalDateTime startDate,
                                    LocalDateTime endDate);
    @Query("""
    SELECT u FROM User u
    WHERE u.role.name = 'SHIPPER'
      AND u.status = org.ffb_be.utils.enums.Status.ACTIVE
       AND u.deliveryStatus = org.ffb_be.utils.enums.DeliveryStatus.AVAILABLE
""")
    List<User> findAllAvailableShippers();

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Query("SELECT u.createdAt, COUNT(u) " +
            "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "AND u.status = :status " +
            "GROUP BY u.createdAt " +
            "ORDER BY u.createdAt")
    List<Object[]> countUsersByDayAndStatus( LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             Status status);

    @Query("SELECT FUNCTION('MONTH', u.createdAt), FUNCTION('YEAR', u.createdAt), COUNT(u) " +
            "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "AND u.status = :status " +
            "GROUP BY FUNCTION('MONTH', u.createdAt), FUNCTION('YEAR', u.createdAt) " +
            "ORDER BY FUNCTION('YEAR', u.createdAt), FUNCTION('MONTH', u.createdAt)")
    List<Object[]> countUsersByMonthAndStatus( LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Status status);

    @Query("SELECT FUNCTION('YEAR', u.createdAt), COUNT(u) " +
            "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "AND u.status = :status " +
            "GROUP BY FUNCTION('YEAR', u.createdAt) " +
            "ORDER BY FUNCTION('YEAR', u.createdAt)")
    List<Object[]> countUsersByYearAndStatus( LocalDateTime startDate,
                                              LocalDateTime endDate,
                                              Status status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = 4")
    long countUsersAreShipper();
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = 2")
    long countUsersHaveShop();
    @Query("SELECT COUNT(u) FROM User u WHERE u.shipperStatus = 'PENDING'")
    long countPendingShipper();
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUser();
    @Query("SELECT COUNT(u) " +
            "FROM User u " +
            "WHERE u.status = 'ACTIVE' " +
            "AND u.role.id=4 " +
            "AND u.createdAt >= :startDate " +
            "AND u.createdAt < :endDate")
    Double countActiveShipperByMonth( LocalDateTime startDate,
                                    LocalDateTime endDate);
    @Query("SELECT COUNT(u) " +
            "FROM User u " +
            "WHERE u.shipperStatus = 'PENDING' " +
            "AND u.role.id=4 " +
            "AND u.createdAt >= :startDate " +
            "AND u.createdAt < :endDate")
    Double countPendingShipperByMonth( LocalDateTime startDate,
                                      LocalDateTime endDate);
}

