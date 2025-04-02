package org.ffb_be.repository;

import org.ffb_be.entity.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {

    List<CartItemOption> findAllByCartItem_Id(Long cartItemId);
    Optional<CartItemOption> findById(Long id);

    CartItemOption findByCartItem_IdAndFoodOption_Id(Long cartItemId, Long foodOptionId);
    void deleteAllByCartItemId(Long cartItemId);
    List<CartItemOption> findAllByCartItemId(Long cartItemId);
}
