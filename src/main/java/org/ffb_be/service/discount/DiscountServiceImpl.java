package org.ffb_be.service.discount;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.discount.DiscountDTO;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.DiscountRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.utils.enums.Status;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
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
        if (discount.getStartDate() != null &&
                discount.getStartDate().isBefore(LocalDateTime.now())) {
            discountEntity.setStatus(Status.ACTIVE);
        }
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
    public List<DiscountDTO> findAllByShopIdAndStatus(Long shopId,Status status) {
        List<Discount> discounts=discountRepository.findAllByShop_IdAndStatus(shopId,status);
        List<DiscountDTO> discountDTOs=new ArrayList<>();
        for (Discount discount:discounts) {
            DiscountDTO discountDTO=new DiscountDTO();
            Product product=productRepository.findById(discount.getProduct().getId()).get();
            ProductResponseDTO productResponseDTO=new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            discountDTO.setAmount(discount.getDiscount_percentage());
            discountDTO.setId(discount.getId());
            discountDTO.setStartDate(discount.getStartDate());
            discountDTO.setEndDate(discount.getEndDate());
            discountDTO.setStatus(discount.getStatus().toString());
            discountDTO.setProductResponseDTO(productResponseDTO);
            discountDTOs.add(discountDTO);
        }
        return discountDTOs;
    }



}
