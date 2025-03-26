package org.ffb_be.repository;

import org.ffb_be.entity.Discount;
import org.ffb_be.entity.Product;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    @Override
    Page<Discount> findAll(Pageable pageable);

    Discount findById(Long id);

    Discount findByProduct(Product product);

    List<Discount> id(Long id);

    List<Discount> findAllByShop_Id(Long shopId);

    List<Discount> findAllByShop_IdAndStatus(Long shopId, Status status);

    List<Discount> findAllByProduct_Id(Long productId);
}
