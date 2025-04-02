package org.ffb_be.service.delivery;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.delivery.DeliveryDTO;
import org.ffb_be.entity.DeliveryMethod;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.DeliveryMethodRepository;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.mapping.DeliveryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryMethodServiceImpl implements DeliveryMethodService {
    private final DeliveryMethodRepository deliveryMethodRepository;
    private final DeliveryMapper deliveryMapper;

    @Override
    public Page<DeliveryDTO> getAll(Pageable pageable) {
        Page<DeliveryMethod> deliveryMethods = deliveryMethodRepository.findByStatus(Status.ACTIVE, pageable);
        return deliveryMethods.map(deliveryMapper::toDTO);
    }

    @Override
    public DeliveryDTO getById(Long id) {
        DeliveryMethod deliveryMethod = deliveryMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phương thức giao hàng"));
        return deliveryMapper.toDTO(deliveryMethod);
    }

    @Override
    public DeliveryMethod create(DeliveryDTO deliveryMethod) {
        if (deliveryMethod.getName() == null || deliveryMethod.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên phương thức giao hàng không được để trống");
        }
        if (deliveryMethod.getFee() == null || deliveryMethod.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí giao hàng không hợp lệ");
        }
        deliveryMethod.setStatus(Status.ACTIVE);
        DeliveryMethod delivery = deliveryMapper.toEntity(deliveryMethod);
        delivery.setCreatedAt(LocalDateTime.now());
        return deliveryMethodRepository.save(delivery);
    }

    @Override
    public DeliveryMethod update(Long id, DeliveryDTO updatedDeliveryMethod) {
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
        deliveryMethod = deliveryMapper.toEntity(updatedDeliveryMethod);
        deliveryMethod.setUpdatedAt(LocalDateTime.now());
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
