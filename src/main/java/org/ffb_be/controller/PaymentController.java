package org.ffb_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Payment;
import org.ffb_be.service.payment.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<Payment>> getAll(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody @Valid Payment Payment) {
        return ResponseEntity.ok(paymentService.create(Payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody @Valid Payment Payment) {
        return ResponseEntity.ok(paymentService.update(id, Payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
