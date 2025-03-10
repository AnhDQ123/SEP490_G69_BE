package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.service.shop.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin("*")
@AllArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<Page<ShopDTO>> getShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getShops(pageable));
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDTO> getShopById(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerShop(
            @RequestParam Long userId,
            @Validated @ModelAttribute ShopRegisterDTO shopDTO,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestParam(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestParam(value = "registrationCert", required = false) MultipartFile registrationCert,
            @RequestParam(value = "foodSafetyCert", required = false) MultipartFile foodSafetyCert,
            @RequestParam(value = "menu", required = false) MultipartFile menu,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        // Gửi toàn bộ file lên Service, kể cả file rỗng
        shopService.registerShop(userId, shopDTO, logo, citizenIDFront, citizenIDBack, registrationCert, foodSafetyCert, menu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

