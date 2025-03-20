package org.ffb_be.service.voucher;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.voucher.VoucherDTO;
import org.ffb_be.entity.Voucher;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.OrderRepository;
import org.ffb_be.repository.VoucherRepository;
import org.ffb_be.utils.enums.DiscountType;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.mapping.VoucherMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public List<VoucherDTO> getAllVouchers(Long shopId) {
        List<Voucher> vouchers = voucherRepository.getAllByShopId(shopId);
        return vouchers.stream().map(voucherMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public VoucherDTO getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Voucher"));
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public VoucherDTO addVoucher(VoucherDTO dto) {
        Voucher voucher = voucherMapper.toEntity(dto);
        voucher.setUsedVouchers(0);
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public VoucherDTO updateVoucher(Long id, VoucherDTO dto) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher"));
        voucher.setDiscountType(dto.getDiscountType());
        voucher.setDiscountValue(dto.getDiscountValue());
        voucher.setMinOrderValue(dto.getMinOrderValue());
        voucher.setTotalVouchers(dto.getTotalVouchers());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        voucher.setStatus(dto.getStatus());
        voucher.setMaxUsagePerCustomer(dto.getMaxUsagePerCustomer());
        voucher.setIsStackable(dto.getIsStackable());

        voucher = voucherRepository.save(voucher);
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher"));
        voucherRepository.delete(voucher);
    }

    @Override
    public BigDecimal applyVoucher(String code, BigDecimal orderTotal, Long userId) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        if (voucher.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("Voucher không hợp lệ");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(voucher.getStartDate()) || today.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        if (voucher.getUsedVouchers() >= voucher.getTotalVouchers()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt mức tối thiểu để áp dụng voucher");
        }

        int usageCount = orderRepository.countByOwnerIdAndVoucher(userId, voucher);

        if (usageCount >= voucher.getMaxUsagePerCustomer()) {
            throw new RuntimeException("Bạn đã hết số lần sử dụng voucher này rồi!");
        }

        BigDecimal discountAmount;
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = orderTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = voucher.getDiscountValue();
        }

        voucher.setUsedVouchers(voucher.getUsedVouchers() + 1);
        voucherRepository.save(voucher);

        return discountAmount.min(orderTotal);
    }

    @Override
    public Map<String, Integer> getVoucherUsageStats() {
        List<Object[]> results = orderRepository.getVoucherUsageStatistics();
        Map<String, Integer> stats = new HashMap<>();
        for (Object[] result : results) {
            stats.put((String) result[0], ((Long) result[1]).intValue());
        }
        return stats;
    }

}
