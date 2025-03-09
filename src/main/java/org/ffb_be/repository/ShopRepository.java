package org.ffb_be.repository;

import org.ffb_be.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Page<Shop> findAll(Pageable pageable);

    boolean existsByOwnerId(Long userId);
}
