package org.ffb_be.service.cart;

import org.ffb_be.dto.cart.CartDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface CartService {
    void save(CartDTO cartDTO) throws IOException;
}
