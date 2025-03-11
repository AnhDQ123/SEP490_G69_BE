package org.ffb_be.service.product;

import jakarta.persistence.EntityNotFoundException;

import org.ffb_be.dto.product.FoodOptionDTO;
import org.ffb_be.dto.product.ProductCreateDTO;
import org.ffb_be.dto.product.ProductResponseDTO;

import org.ffb_be.entity.Category;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final FoodOptionRepository foodOptionRepository;
    private final DiscountRepository discountRepository;
    private final TypesRepository typesRepository;
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CloudinaryUpload cloudinaryUpload, FoodOptionRepository foodOptionRepository, DiscountRepository discountRepository, TypesRepository typesRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryUpload = cloudinaryUpload;
        this.foodOptionRepository = foodOptionRepository;
        this.discountRepository = discountRepository;
        this.typesRepository = typesRepository;
    }

    public void save(ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException {
        Product product = new Product();
        Category category = categoryRepository.findById(productCreateDTO.getCategory_id())
                .orElseThrow(() -> new EntityNotFoundException("Category not found!"));
        List<FoodOption> foodOptions = new ArrayList<>();
        List<FoodOptionDTO> foodOptionDTOs = productCreateDTO.getFoodOption();

        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setExpired_date(productCreateDTO.getExpiryDate());
        product.setQuantity(productCreateDTO.getQuantity());
        product.setManufacturer(productCreateDTO.getManufacturer());
        product.setSupplier(productCreateDTO.getSupplier());
        product.setCategory(category);

        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("Uploading Avatar: " + avatar.getOriginalFilename());
            String url = cloudinaryUpload.uploadFile(avatar);
            product.setImage(url);
            System.out.println("Avatar URL: " + url);
        }
        productRepository.save(product);
        for(FoodOptionDTO foodOptionDTO : foodOptionDTOs) {
            foodOptionDTO.setProduct_id(product.getId());
        }
        if (option != null && !option.isEmpty()) {
            System.out.println("Uploading " + option.size() + " option images");
            for (int i = 0; i < option.size(); i++) {
                String url = cloudinaryUpload.uploadFile(option.get(i));
                foodOptionDTOs.get(i).setImage(url);
            }
        }
        for (FoodOptionDTO foodOptionDTO : foodOptionDTOs) {
            FoodOption foodOption = new FoodOption();
            foodOption.setId(foodOptionDTO.getId());
            foodOption.setName(foodOptionDTO.getName());
            foodOption.setImage(foodOptionDTO.getImage());
            foodOption.setPrice(foodOptionDTO.getPrice());
            foodOption.setType(typesRepository.findById(foodOptionDTO.getType_id()).get());
            foodOption.setFood(productRepository.findById(foodOptionDTO.getProduct_id()).get());
            foodOptions.add(foodOption);
        }
        product.setFoodOptions(foodOptions);
        foodOptionRepository.saveAll(foodOptions);
    }

    @Override
    public Page<ProductResponseDTO> findAllByShop(Long id,Pageable pageable) {
        return productRepository.findAllByShop_Id(id,pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
    }
    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            if (product.getDiscount() != null && product.getDiscount().getId() != null) {
                Discount d = discountRepository.findById(product.getDiscount().getId());
                productResponseDTO.setDiscount(d.getDiscount_percentage());
            } else {
                productResponseDTO.setDiscount(BigDecimal.ZERO);
            }
            productResponseDTO.setCategory(product.getCategory().getName());
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
    }
}
