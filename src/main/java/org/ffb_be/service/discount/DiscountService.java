package org.ffb_be.service.discount;

import org.ffb_be.dto.discount.DiscountDTO;
import org.ffb_be.utils.enums.Status;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscountService {
    void save(DiscountDTO discount,Long productId);
    DiscountDTO findById(Long id);

    List<DiscountDTO> findAllByShopIdAndStatus(Long shopId,Status status);
    void checkAndUpdateDiscountStatus();
}