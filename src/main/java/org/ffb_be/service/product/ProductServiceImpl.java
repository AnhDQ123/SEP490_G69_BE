package org.ffb_be.service.product;

import jakarta.persistence.EntityNotFoundException;

import org.ffb_be.dto.product.ProductCreateDTO;
import org.ffb_be.dto.product.ProductResponseDTO;

import org.ffb_be.entity.Category;
import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.CategoryRepository;
import org.ffb_be.repository.FoodOptionRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final FoodOptionRepository foodOptionRepository;
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CloudinaryUpload cloudinaryUpload, FoodOptionRepository foodOptionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryUpload = cloudinaryUpload;
        this.foodOptionRepository = foodOptionRepository;
    }

    public void save(ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException {
        Product product = new Product();
        Category category = categoryRepository.findById(productCreateDTO.getCategory_id())
                .orElseThrow(() -> new EntityNotFoundException("Category not found!"));

        List<FoodOption> foodOptions = productCreateDTO.getFoodOption();
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setExpired_date(productCreateDTO.getExpiryDate());
        product.setQuantity(productCreateDTO.getQuantity());
        product.setManufacturer(productCreateDTO.getManufacturer());
        product.setSupplier(productCreateDTO.getSupplier());
        product.setCategory(category);
        product.setFoodOptions(foodOptions);

        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("Uploading Avatar: " + avatar.getOriginalFilename());
            String url = cloudinaryUpload.uploadFile(avatar);
            product.setImage(url);
            System.out.println("Avatar URL: " + url);
        }

        if (option != null && !option.isEmpty()) {
            System.out.println("Uploading " + option.size() + " option images");
            for (int i = 0; i < option.size(); i++) {
                String url = cloudinaryUpload.uploadFile(option.get(i));
                foodOptions.get(i).setImage(url);
            }
        }

        productRepository.save(product);
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
            org.ffb_be.dto.product.ProductResponseDTO productResponseDTO = new org.ffb_be.dto.product.ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
    }
}
