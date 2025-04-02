package org.ffb_be.repository;

import org.ffb_be.entity.Cart;
import org.ffb_be.utils.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.owner.id = :ownerId AND c.status = :status")
    List<Cart> findAllByOwner_IdAndStatus( Long ownerId, CartStatus status);
    @Query("SELECT c FROM Cart c JOIN CartItem ci ON c.id = ci.cart.id " +
            "JOIN Product p ON ci.product.id = p.id " +
            "JOIN Shop s ON p.shop.id = s.id " +
            "WHERE c.owner.id = :userId AND s.id = :shopId AND c.status = :status")
    Optional<Cart> findByUserIdAndShopIdAndStatus(Long userId,
                                                  Long shopId,
                                                  CartStatus status);
}
