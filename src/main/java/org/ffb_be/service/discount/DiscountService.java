package org.ffb_be.service.discount;

import org.ffb_be.entity.Discount;
import org.ffb_be.utils.enums.OrderStatus;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscountService {
    void save(Discount discount);

    void addToProducts(Long id, Long productId);

    Discount findById(Long id);

    List<Discount> findAll();

    List<Discount> findByStatus(Status status);
}