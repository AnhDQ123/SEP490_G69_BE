package org.ffb_be.service.delivery;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.DeliveryMethod;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.DeliveryMethodRepository;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DeliveryMethodServiceImpl implements DeliveryMethodService {
    private final DeliveryMethodRepository deliveryMethodRepository;

    @Override
    public Page<DeliveryMethod> getAll(Pageable pageable) {
        return deliveryMethodRepository.findByStatus(Status.ACTIVE, pageable);
    }

    @Override
    public DeliveryMethod getById(Long id) {
        return deliveryMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức giao hàng"));
    }

    @Override
    public DeliveryMethod create(DeliveryMethod deliveryMethod) {
        if (deliveryMethod.getName() == null || deliveryMethod.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên phương thức giao hàng không được để trống");
        }
        if (deliveryMethod.getFee() == null || deliveryMethod.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí giao hàng không hợp lệ");
        }
        deliveryMethod.setStatus(Status.ACTIVE);
        return deliveryMethodRepository.save(deliveryMethod);
    }

    @Override
    public DeliveryMethod update(Long id, DeliveryMethod updatedDeliveryMethod) {
        DeliveryMethod deliveryMethod = deliveryMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức giao hàng"));

        if (deliveryMethod.getStatus() == Status.DELETED) {
            throw new RuntimeException("Cannot update a deleted delivery method");
        }

        if (updatedDeliveryMethod.getName() == null || updatedDeliveryMethod.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên phương thức giao hàng không được để trống");
        }
        if (updatedDeliveryMethod.getFee() == null || updatedDeliveryMethod.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí giao hàng không hợp lệ");
        }

        deliveryMethod.setName(updatedDeliveryMethod.getName());
        deliveryMethod.setDescription(updatedDeliveryMethod.getDescription());
        deliveryMethod.setFee(updatedDeliveryMethod.getFee());

        return deliveryMethodRepository.save(deliveryMethod);
    }

    @Override
    public void delete(Long id) {
        DeliveryMethod deliveryMethod = deliveryMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức giao hàng"));
        deliveryMethod.setStatus(Status.DELETED);
        deliveryMethodRepository.save(deliveryMethod);
    }
}
