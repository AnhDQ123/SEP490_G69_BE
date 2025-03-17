package org.ffb_be.repository;

import org.ffb_be.entity.Cart;
import org.ffb_be.utils.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.owner.id = :ownerId AND c.status = :status")
    List<Cart> findAllByOwner_IdAndStatus( Long ownerId, CartStatus status);

}
