package org.ffb_be.service.user;


import jakarta.persistence.EntityNotFoundException;

import org.ffb_be.dto.auth.product.ProductCreateDTO;
import org.ffb_be.dto.auth.product.ProductResponseDTO;

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
import java.util.Map;


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

        public void save(ProductCreateDTO productCreateDTO, Map<String, List<MultipartFile>> files) throws IOException {
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


            if (files.containsKey("product") && !files.get("product").isEmpty()) {
                MultipartFile file = files.get("product").get(0);
                String url = cloudinaryUpload.uploadFile(file);
                product.setImage(url);
            }

            if (files.containsKey("option")) {
                List<MultipartFile> uploadedFiles = files.get("option");

                // Đảm bảo số lượng ảnh không vượt quá số lượng phần tử trong danh sách foodOptionList
                int size = Math.min(uploadedFiles.size(), foodOptions.size());
                for (int i = 0; i < size; i++) {
                    String url = cloudinaryUpload.uploadFile(uploadedFiles.get(i)); // Upload từng ảnh
                    foodOptions.get(i).setImage(url); // Gán ảnh cho từng FoodOption tương ứng
                }
            }

        productRepository.save(product);
        foodOptionRepository.saveAll(foodOptions);
    }

    @Override
    public Page<ProductResponseDTO> findAll(Long id,Pageable pageable) {
        return productRepository.findAllByShop_Id(id,pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
}
}
