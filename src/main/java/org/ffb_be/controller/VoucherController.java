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

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<VoucherDTO>> getAllVouchers(@PathVariable Long shopId, @RequestParam(required = false) boolean isShopkeeper) {
        return ResponseEntity.ok(voucherService.getAllVouchers(shopId, isShopkeeper));
    }

    @GetMapping("/voucher/{code}")
    public ResponseEntity<VoucherDTO> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping("/create/{shopId}")
    public ResponseEntity<VoucherDTO> createVoucher(@PathVariable Long shopId,@RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.addVoucher(dto, shopId));
    }

    @PutMapping("/{code}")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable String code, @RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.updateVoucher(code, dto));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable String code) {
        voucherService.deleteVoucher(code);
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
