package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.dto.auth.userDto.ShipperRegisterDTO;
import org.ffb_be.service.shipper.ShipperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/shippers")
@RequiredArgsConstructor
public class ShipperController {
    private final ShipperService shipperService;

    @PostMapping("/register/{userId}")
    public ResponseEntity<?> registerShipper(
            @PathVariable Long userId,
            @Validated @ModelAttribute ShipperRegisterDTO shipperRegisterDTO,
            @RequestPart(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestParam(value = "drivingLicenseFront", required = false) MultipartFile drivingLicenseFront,
            @RequestParam(value = "drivingLicenseBack", required = false) MultipartFile drivingLicenseBack,
            @RequestParam(value = "judicialRecord", required = false) MultipartFile judicialRecord,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        shipperService.registerShipper(userId, shipperRegisterDTO, citizenIDFront, citizenIDBack, drivingLicenseFront, drivingLicenseBack, judicialRecord);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<?> approveShipper(@PathVariable Long userId) {
        shipperService.approveShipper(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<?> rejectShipper(@PathVariable Long userId) {
        shipperService.rejectShipper(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateShipperInfo(
            @PathVariable Long userId,
            @Validated @ModelAttribute ShipperRegisterDTO shipperRegisterDTO,
            @RequestPart(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestParam(value = "drivingLicenseFront", required = false) MultipartFile drivingLicenseFront,
            @RequestParam(value = "drivingLicenseBack", required = false) MultipartFile drivingLicenseBack,
            @RequestParam(value = "judicialRecord", required = false) MultipartFile judicialRecord,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        shipperService.updateShipperInfo(userId, shipperRegisterDTO, citizenIDFront, citizenIDBack, drivingLicenseFront, drivingLicenseBack, judicialRecord);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ShipperInfoDTO>> getShippersByStatus(@PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(shipperService.getShippersByStatus(status, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ShipperInfoDTO> getShipperDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(shipperService.getShipperDetail(userId));
    }
}
