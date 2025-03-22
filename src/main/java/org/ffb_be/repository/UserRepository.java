package org.ffb_be.repository;

import org.ffb_be.entity.Role;
import org.ffb_be.entity.User;
import org.ffb_be.utils.enums.ShipperStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


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

    Page<User> findByRoleAndShipperStatus(Role role, ShipperStatus status, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.role.name = 'SHIPPER'
      AND u.status = org.ffb_be.utils.enums.Status.ACTIVE
       AND u.deliveryStatus = org.ffb_be.utils.enums.DeliveryStatus.AVAILABLE
""")
    List<User> findAllAvailableShippers();

    Page<User> findAll(Specification<User> spec, Pageable pageable);
}

