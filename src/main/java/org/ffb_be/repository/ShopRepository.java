package org.ffb_be.repository;


import org.ffb_be.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface ShopRepository extends JpaRepository<Shop, Long>, JpaSpecificationExecutor<Shop> {

    @Override
    Page<Shop> findAll(Pageable pageable);

    boolean existsByOwnerId(Long userId);


    @Query("SELECT s FROM Shop s JOIN s.products p WHERE p.id = :productId")
    Shop findByProduct( Long productId);

}
