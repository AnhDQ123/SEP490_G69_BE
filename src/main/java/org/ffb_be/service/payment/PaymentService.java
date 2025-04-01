package org.ffb_be.service.payment;
import org.ffb_be.dto.payment.PaymentDTO;
import org.ffb_be.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<PaymentDTO> getAll(Pageable pageable);

    PaymentDTO getById(Long id);

    Payment create(PaymentDTO paymentMethod);

    Payment update(Long id, PaymentDTO updatedPaymentMethod);

    void delete(Long id);
}
