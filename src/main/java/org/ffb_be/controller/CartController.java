package org.ffb_be.controller;

import org.ffb_be.dto.cart.CartDTO;

import org.ffb_be.service.cart.CartService;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/owner/{id}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.findByUserId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.findById(id));
    }


    @DeleteMapping("/item/delete")
    public ResponseEntity<?> deleteItemFromCart(@RequestParam("cartId") Long cartId,
                                                @RequestParam("id") Long id) {
        cartService.deleteItemFromCart(cartId, id);
        return ResponseEntity.ok().body("Sản phẩm đã được xóa khỏi giỏ hàng thành công");

    }

    @DeleteMapping("/option/delete")
    public ResponseEntity<?> deleteItemOptionFromCart(@RequestParam("cartId") Long cartId,
                                                      @RequestParam("id") Long id) {
        try {
            cartService.deleteOptionFromCart(cartId, id);
            return ResponseEntity.ok().body("Tùy chọn sản phẩm đã được xóa khỏi giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy tùy chọn trong giỏ hàng hoặc giỏ hàng không hợp lệ");
        }
    }
    @PostMapping("/option/increase")
    public void increaseQuantity( @RequestParam("id") Long id){
        cartService.increaseOptionQuantity(id);
    }
    @PostMapping("/option/decrease")
    public void decreaseQuantity(@RequestParam("id") Long id){
        cartService.decreaseOptionQuantity(id);
    }
    @PostMapping("/size/change")
    public void changeSize(@RequestParam("id") Long id,
                           @RequestParam("newId") Long newId){
        cartService.changeSize(id,newId);
    }
    @PostMapping("/add/option")
    public void addOption(@RequestParam("cartItemId") Long cartItemId,
                          @RequestParam("optionId") Long optionId){
        cartService.addOptionToItem(cartItemId,optionId);
    }
}
