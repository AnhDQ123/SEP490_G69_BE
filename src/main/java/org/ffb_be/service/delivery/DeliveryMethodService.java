package org.ffb_be.service.delivery;

import org.ffb_be.entity.DeliveryMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryMethodService {
    Page<DeliveryMethod> getAll(Pageable pageable);

    DeliveryMethod getById(Long id);

    DeliveryMethod create(DeliveryMethod deliveryMethod);

    DeliveryMethod update(Long id, DeliveryMethod updatedDeliveryMethod);

    void delete(Long id);
}
