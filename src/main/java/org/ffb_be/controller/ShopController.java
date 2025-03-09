package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.service.shop.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin("*")
@AllArgsConstructor
public class ShopController {
    private final ShopService shopService;

    // 📌 Lấy danh sách các shop có phân trang
    @GetMapping
    public ResponseEntity<Page<ShopDTO>> getAllShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getShops(pageable));
    }

    // 📌 Lấy thông tin shop theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ShopDTO> getShopById(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    // 📌 Đăng ký shop mới (gắn với một User)
    @PostMapping("/register/{userId}")
    public ResponseEntity<ShopRegisterDTO> registerShop(
            @PathVariable Long userId,
            @RequestBody ShopRegisterDTO shopDTO) {
        return ResponseEntity.ok(shopService.registerShop(userId, shopDTO));
    }

//    // 📌 Xóa shop theo ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
//        shopService.deleteShop(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // 📌 Cập nhật thông tin shop
//    @PutMapping("/{id}")
//    public ResponseEntity<ShopDTO> updateShop(
//            @PathVariable Long id,
//            @RequestBody ShopDTO shopDTO) {
//        return ResponseEntity.ok(shopService.updateShop(id, shopDTO));
//    }
}

