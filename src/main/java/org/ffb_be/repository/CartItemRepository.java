package org.ffb_be.repository;


import org.ffb_be.entity.CartItem;
import org.ffb_be.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCart_Id(Long cartId);

    CartItem findByProduct(Product product);
}
