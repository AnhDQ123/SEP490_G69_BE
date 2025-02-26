package org.ffb_be.repository;

import org.ffb_be.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT e FROM User e LEFT JOIN e.profile p " +
            "WHERE lower(p.name) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.phone) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.email) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.status) LIKE lower(concat('%', :search, '%')) " +
            "OR lower(e.role.name) LIKE lower(concat('%', :search, '%')) ")
    Page<User> findByAllField( String search, Pageable pageable);

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
}

