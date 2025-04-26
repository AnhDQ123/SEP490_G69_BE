package org.ffb_be.service.discount;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.discount.DiscountDTO;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.DiscountRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.utils.enums.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscountServiceImpl implements DiscountService{
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    @Override
    public void save(DiscountDTO discount,Long productId) {
        Discount discountEntity = new Discount();
        discountEntity.setId(discount.getId());
        discountEntity.setDiscount_percentage(discount.getAmount());
        discountEntity.setStartDate(discount.getStartDate());
        discountEntity.setEndDate(discount.getEndDate());
        discountEntity.setStatus(Status.PENDING);
        if (discount.getStartDate().isEqual(LocalDate.now())) {
            discountEntity.setStatus(Status.ACTIVE);
        }
        discountEntity.setShop(productRepository.findById(productId).get().getShop());
        Product product=productRepository.findById(productId).get();
        discountEntity.setProduct(product);
        discountRepository.save(discountEntity);
    }

    @Override
    public DiscountDTO findById(Long id) {
        Discount discount=discountRepository.findById(id);
        DiscountDTO discountDTO=new DiscountDTO();
        discountDTO.setAmount(discount.getDiscount_percentage());
        discountDTO.setId(discount.getId());
        discountDTO.setStartDate(discount.getStartDate());
        discountDTO.setEndDate(discount.getEndDate());
        discountDTO.setStatus(discount.getStatus().toString());
        return discountDTO;
    }

    @Override
    public void update(DiscountDTO discount, Long productId) {
        Discount discountEntity=discountRepository.findById(discount.getId());
        discountEntity.setId(discount.getId());
        discountEntity.setDiscount_percentage(discount.getAmount());
        discountEntity.setStartDate(discount.getStartDate());
        discountEntity.setEndDate(discount.getEndDate());
        discountEntity.setStatus(Status.PENDING);
        if (discount.getStartDate().isEqual(LocalDate.now())) {
            discountEntity.setStatus(Status.ACTIVE);
        }
        Product product=productRepository.findById(productId).get();
        discountEntity.setProduct(product);
        discountRepository.save(discountEntity);
    }

    @Override
    public void delete(Long id) {
        Discount discountEntity=discountRepository.findById(id);
        discountEntity.setStatus(Status.INACTIVE);
        discountRepository.save(discountEntity);
    }

    @Override
    public List<DiscountDTO> findAllByShopIdAndStatus(Long shopId, Status status) {
        List<Discount> discounts = discountRepository.findAllByShop_IdAndStatus(shopId, status);
        List<DiscountDTO> discountDTOs = new ArrayList<>();
        for (Discount discount : discounts) {
            DiscountDTO discountDTO = new DiscountDTO();

            // Check if product is not null before accessing its fields
            Product product = discount.getProduct();
            if (product != null) {
                ProductResponseDTO productResponseDTO = new ProductResponseDTO();
                productResponseDTO.setId(product.getId());
                productResponseDTO.setName(product.getName());
                productResponseDTO.setManufacturer(product.getManufacturer());
                productResponseDTO.setSupplier(product.getSupplier());
                productResponseDTO.setImage(product.getImage());
                productResponseDTO.setCategory(product.getCategory().getName());
                discountDTO.setProductResponseDTO(productResponseDTO);
            }

            discountDTO.setAmount(discount.getDiscount_percentage());
            discountDTO.setId(discount.getId());
            discountDTO.setStartDate(discount.getStartDate());
            discountDTO.setEndDate(discount.getEndDate());
            discountDTO.setStatus(discount.getStatus().toString());
            discountDTOs.add(discountDTO);
        }
        return discountDTOs;
    }


    @Scheduled(cron = "0 0 0 * * ?")  // Lên lịch chạy mỗi ngày lúc nửa đêm
    public void checkAndUpdateDiscountStatus() {
        LocalDate today = LocalDate.now();

        List<Discount> discounts = discountRepository.findAll(); // Lấy tất cả discounts

        for (Discount discount : discounts) {
            // Nếu ngày hôm nay là startDate thì đổi trạng thái thành activated
            if (discount.getStartDate().equals(today) && !discount.getStatus().equals(Status.ACTIVE)) {
                discount.setStatus(Status.ACTIVE);
                discountRepository.save(discount);
            }
            if (discount.getEndDate().equals(today) && !discount.getStatus().equals(Status.INACTIVE)) {
                discount.setStatus(Status.INACTIVE);
                discountRepository.save(discount);
            }
        }
    }



}
