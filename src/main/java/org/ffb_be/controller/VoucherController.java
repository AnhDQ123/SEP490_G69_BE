package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.voucher.VoucherDTO;
import org.ffb_be.service.voucher.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping("/{shopId}")
    public ResponseEntity<List<VoucherDTO>> getAllVouchers(@PathVariable Long shopId) {
        return ResponseEntity.ok(voucherService.getAllVouchers(shopId));
    }

    @GetMapping("/{code}")
    public ResponseEntity<VoucherDTO> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.addVoucher(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply")
    public ResponseEntity<BigDecimal> applyVoucher(@RequestParam String code,
                                                   @RequestParam BigDecimal orderTotal,
                                                   @RequestParam Long userId) {
        return ResponseEntity.ok(voucherService.applyVoucher(code, orderTotal, userId));
    }

    @GetMapping("/usage-stats")
    public ResponseEntity<Map<String, Integer>> getVoucherUsageStats() {
        return ResponseEntity.ok(voucherService.getVoucherUsageStats());
    }

}
