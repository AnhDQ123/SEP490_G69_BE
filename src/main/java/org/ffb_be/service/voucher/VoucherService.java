package org.ffb_be.service.voucher;

import org.ffb_be.dto.voucher.VoucherDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VoucherService {

    List<VoucherDTO> getAllVouchers(Long shopId);

    VoucherDTO getVoucherByCode(String code);

    VoucherDTO getVoucherById(Long id);

    VoucherDTO addVoucher(VoucherDTO dto);

    VoucherDTO updateVoucher(Long id, VoucherDTO dto);

    void deleteVoucher(Long id);

    BigDecimal applyVoucher(String code, BigDecimal orderTotal, Long userId);

    Map<String, Integer> getVoucherUsageStats();
}
