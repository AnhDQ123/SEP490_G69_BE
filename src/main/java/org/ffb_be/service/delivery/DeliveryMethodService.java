package org.ffb_be.service.delivery;

import org.ffb_be.dto.delivery.DeliveryDTO;
import org.ffb_be.entity.DeliveryMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryMethodService {
    Page<DeliveryDTO> getAll(Pageable pageable);

    DeliveryDTO getById(Long id);

    DeliveryMethod create(DeliveryDTO deliveryMethod);

    DeliveryMethod update(Long id, DeliveryDTO updatedDeliveryMethod);

    void delete(Long id);
}
