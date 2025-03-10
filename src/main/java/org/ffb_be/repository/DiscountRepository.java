package org.ffb_be.repository;

import org.ffb_be.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    @Override
    Page<Discount> findAll(Pageable pageable);

    Discount findById(Long id);
}
