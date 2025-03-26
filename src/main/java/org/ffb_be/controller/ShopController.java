package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.service.shop.ShopService;
import org.ffb_be.utils.enums.Status;
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
    public ResponseEntity<Page<ShopDTO>> getShops(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(value = "type",required = false) String type,
            Pageable pageable) {

        Page<ShopDTO> shops = shopService.getShops(type, status, search, pageable);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDTO> getShopById(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }

    @GetMapping("/byUser/{userId}")
    public ResponseEntity<ShopDTO> getShopByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(shopService.getShopByUserId(userId));
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

    @PutMapping(value = "/{shopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateShop(
            @PathVariable Long shopId,
            @Validated @ModelAttribute ShopRegisterDTO shopDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestPart(value = "registrationCert", required = false) MultipartFile registrationCert,
            @RequestPart(value = "foodSafetyCert", required = false) MultipartFile foodSafetyCert,
            @RequestPart(value = "menu", required = false) MultipartFile menu,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        shopService.updateShop(shopId, shopDTO, logo, citizenIDFront, citizenIDBack, registrationCert, foodSafetyCert, menu);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/isOpen")
    public ResponseEntity<Boolean> isShopOpen(@RequestParam Long shopId) {
        boolean isOpen = shopService.isShopOpen(shopId);
        return ResponseEntity.ok(isOpen);
    }

    @PutMapping("/{shopId}/active")
    public ResponseEntity<Void> activeShop(@PathVariable Long shopId) {
        shopService.updateShopStatus(shopId, Status.ACTIVE, "");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/inactive")
    public ResponseEntity<Void> inactiveShop(
            @PathVariable Long shopId,
            @RequestParam String reason
    ) {
        shopService.updateShopStatus(shopId, Status.INACTIVE, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/reject")
    public ResponseEntity<Void> rejectShop(
            @PathVariable Long shopId,
            @RequestParam String reason
    ) {
        shopService.rejectShop(shopId, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/approve")
    public ResponseEntity<Void> approveShop(@PathVariable Long shopId) {
        shopService.approveShop(shopId);
        return ResponseEntity.ok().build();
    }

}

