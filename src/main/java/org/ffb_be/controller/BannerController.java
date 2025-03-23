package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.service.banner.BannerService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<?> getBanners(Pageable pageable) {
        return ResponseEntity.ok(bannerService.getBanners(pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBanner(
            @Validated @ModelAttribute ImageDTO banner,
            @RequestPart(value = "file", required = false) MultipartFile file,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        bannerService.createBanner(banner, file);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{bannerId}")
    public ResponseEntity<?> updateBanner(
            @PathVariable Long bannerId,
            @Validated @ModelAttribute ImageDTO banner,
            @RequestPart(value = "file", required = false) MultipartFile file,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        bannerService.updateBanner(bannerId, banner, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{bannerId}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long bannerId) {
        bannerService.delete(bannerId);
        return ResponseEntity.ok().build();
    }
}
