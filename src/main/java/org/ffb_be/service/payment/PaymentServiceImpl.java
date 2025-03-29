package org.ffb_be.service.payment;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ffb_be.entity.Payment;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.PaymentRepository;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    @Override
    public Page<Payment> getAll(Pageable pageable) {
        return paymentRepository.findByStatus(Status.ACTIVE, pageable);
    }

    @Override
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức thanh toán"));
    }

    @Override
    public Payment create(@Valid Payment paymentMethod) {
        if (paymentMethod.getName() == null || paymentMethod.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên phương thức thanh toán không được để trống");
        }
        if (paymentMethod.getFee() == null || paymentMethod.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí thanh toán không hợp lệ");
        }
        paymentMethod.setStatus(Status.ACTIVE);
        return paymentRepository.save(paymentMethod);
    }

    @Override
    public Payment update(Long id, @Valid Payment updatedPaymentMethod) {
        Payment paymentMethod = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức thanh toán"));

        if (paymentMethod.getStatus() == Status.DELETED) {
            throw new RuntimeException("Cannot update a deleted payment method");
        }

        if (updatedPaymentMethod.getName() == null || updatedPaymentMethod.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên phương thức thanh toán không được để trống");
        }
        if (updatedPaymentMethod.getFee() == null || updatedPaymentMethod.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí thanh toán không hợp lệ");
        }

        paymentMethod.setName(updatedPaymentMethod.getName());
        paymentMethod.setDescription(updatedPaymentMethod.getDescription());
        paymentMethod.setFee(updatedPaymentMethod.getFee());

        return paymentRepository.save(paymentMethod);
    }

    @Override
    public void delete(Long id) {
        Payment paymentMethod = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        paymentMethod.setStatus(Status.DELETED);
        paymentRepository.save(paymentMethod);
    }
}
