package org.ffb_be.service.cart;

import org.ffb_be.dto.cart.CartDTO;
import org.ffb_be.dto.product.FoodOptionDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface CartService {
    void save(CartDTO cartDTO) throws IOException;
    List<CartDTO> findByUserId(Long id);
    CartDTO findById(Long id);
    void increaseOptionQuantity( Long id);
    void decreaseOptionQuantity(Long id);
    void changeSize(Long oldId,Long newId);
    void deleteItemFromCart(Long cartId,Long id);
    void deleteOptionFromCart(Long cartId,Long optionId);
    void addOptionToItem(Long cartItemId,Long optionId);
}
