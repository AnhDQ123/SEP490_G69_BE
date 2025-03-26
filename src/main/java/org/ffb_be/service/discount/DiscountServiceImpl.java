package org.ffb_be.service.discount;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.DiscountRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.utils.enums.Status;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscountServiceImpl implements DiscountService{
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    @Override
    public void save(Discount discount) {
        Discount discountEntity = new Discount();
        discountEntity.setId(discount.getId());
        discountEntity.setDiscount_percentage(discount.getDiscount_percentage());
        discountEntity.setStart_date(discount.getStart_date());
        discountEntity.setEnd_date(discount.getEnd_date());
        discountEntity.setStatus(Status.PENDING);
        if (discount.getStart_date() != null &&
                discount.getStart_date().isBefore(LocalDate.now())) {
            discount.setStatus(Status.ACTIVE);
        }
        discountRepository.save(discountEntity);
    }

    @Override
    public void addToProducts(Long id, Long productId) {
        Discount discount=discountRepository.findById(id);
        Product product=productRepository.findById(productId).get();
        product.setDiscount(discount);
        productRepository.save(product);
    }

    @Override
    public Discount findById(Long id) {

        return null;
    }

    @Override
    public List<Discount> findAll() {
        return List.of();
    }

    @Override
    public List<Discount> findByStatus(Status status) {
        return List.of();
    }


}
