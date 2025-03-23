package org.ffb_be.controller;

import org.ffb_be.dto.cart.CartDTO;

import org.ffb_be.service.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;


@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@Validated @RequestBody() CartDTO cartDTO,
                                        BindingResult bindingResult) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        cartService.save(cartDTO);
        return ResponseEntity.ok().body(cartDTO);
    }
    @GetMapping("owner/{id}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.findByUserId(id));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.findById(id));
    }
}
