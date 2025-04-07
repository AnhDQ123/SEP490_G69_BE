package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.dto.auth.userDto.ShipperRegisterDTO;
import org.ffb_be.service.shipper.ShipperService;
import org.ffb_be.utils.enums.ShipperStatus;
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
@CrossOrigin("*")
public class ShipperController {
    private final ShipperService shipperService;

    @PostMapping("/register/{userId}")
    public ResponseEntity<?> registerShipper(
            @PathVariable Long userId,
            @Validated @ModelAttribute ShipperRegisterDTO shipperRegisterDTO,
            @RequestPart(value = "citizenIDFront") MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack") MultipartFile citizenIDBack,
            @RequestParam(value = "drivingLicenseFront") MultipartFile drivingLicenseFront,
            @RequestParam(value = "drivingLicenseBack") MultipartFile drivingLicenseBack,
            @RequestParam(value = "judicialRecord") MultipartFile judicialRecord,
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

    @PostMapping("/{userId}/reject")
    public ResponseEntity<String> rejectShipper(@PathVariable Long userId, @RequestParam String reason) {
        shipperService.rejectShipper(userId, reason);
        return ResponseEntity.ok("Shipper bị từ chối thành công.");
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateShipperInfo(
            @PathVariable Long userId,
            @Validated @ModelAttribute ShipperRegisterDTO shipperRegisterDTO,
            @RequestPart(value = "citizenIDFront") MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack") MultipartFile citizenIDBack,
            @RequestParam(value = "drivingLicenseFront") MultipartFile drivingLicenseFront,
            @RequestParam(value = "drivingLicenseBack") MultipartFile drivingLicenseBack,
            @RequestParam(value = "judicialRecord") MultipartFile judicialRecord,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        shipperService.updateShipperInfo(userId, shipperRegisterDTO, citizenIDFront, citizenIDBack, drivingLicenseFront, drivingLicenseBack, judicialRecord);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/active")
    public ResponseEntity<?> shipperActive(
            @PathVariable Long userId) {
        shipperService.shipperStatus(userId, ShipperStatus.ACTIVE, "");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/inactive")
    public ResponseEntity<?> shipperInactive(
            @PathVariable Long userId,
            @RequestParam(value = "reason") String reason
    ) {
        shipperService.shipperStatus(userId, ShipperStatus.INACTIVE, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<ShipperInfoDTO>> getShippersByStatus(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(shipperService.getShippersByStatus(status, search, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ShipperInfoDTO> getShipperDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(shipperService.getShipperDetail(userId));
    }
    @PutMapping("isBusy/{userId}")
    public void updateShipperIsBusy(@PathVariable Long userId){
        shipperService.changeIsBusy(userId);
    }
}
