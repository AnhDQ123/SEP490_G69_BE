package org.ffb_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.DeliveryMethod;
import org.ffb_be.service.delivery.DeliveryMethodService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
@CrossOrigin("*")
public class DeliveryController {
    private final DeliveryMethodService deliveryMethodService;

    @GetMapping
    public ResponseEntity<Page<DeliveryMethod>> getAll(Pageable pageable) {
        return ResponseEntity.ok(deliveryMethodService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryMethod> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryMethodService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DeliveryMethod> create(@RequestBody @Valid DeliveryMethod deliveryMethod) {
        return ResponseEntity.ok(deliveryMethodService.create(deliveryMethod));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryMethod> update(@PathVariable Long id, @RequestBody @Valid DeliveryMethod deliveryMethod) {
        return ResponseEntity.ok(deliveryMethodService.update(id, deliveryMethod));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
