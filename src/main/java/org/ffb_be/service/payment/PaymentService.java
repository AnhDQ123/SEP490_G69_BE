package org.ffb_be.service.payment;
import org.ffb_be.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<Payment> getAll(Pageable pageable);

    Payment getById(Long id);

    Payment create(Payment paymentMethod);

    Payment update(Long id, Payment updatedPaymentMethod);

    void delete(Long id);
}
