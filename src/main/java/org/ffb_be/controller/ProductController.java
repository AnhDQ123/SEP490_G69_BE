package org.ffb_be.controller;

import org.ffb_be.dto.auth.product.FoodOptionDTO;
import org.ffb_be.dto.auth.product.ProductCreateDTO;
import org.ffb_be.dto.auth.product.ProductResponseDTO;
import org.ffb_be.entity.Category;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.CategoryRepository;
import org.ffb_be.repository.DiscountRepository;
import org.ffb_be.repository.FoodOptionRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.service.user.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    public ProductController(ProductService productService, ProductRepository productRepository, FoodOptionRepository foodOptionRepository, DiscountRepository discountRepository, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.foodOptionRepository = foodOptionRepository;
        this.discountRepository = discountRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/shop/{id}")
    public ResponseEntity<?> getAllByShop(@PathVariable Long id,
                                    @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( productService.findAll(id,pageable));
    }
    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
        Product product = productOptional.get();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
        List<FoodOptionDTO> foodOptionDTOs = new ArrayList<>();

        for (FoodOption foodOption : foodOptions) {
            FoodOptionDTO dto = new FoodOptionDTO();
            BeanUtils.copyProperties(foodOption, dto);
            foodOptionDTOs.add(dto);
        }
        BeanUtils.copyProperties(product, productResponseDTO);
        Discount d= discountRepository.findById(product.getDiscount().getId());
        Optional<Category> c=categoryRepository.findById(product.getCategory().getId());
        Category category = c.get();
        productResponseDTO.setDiscount(d.getDiscount_percentage());
        productResponseDTO.setFoodOption(foodOptionDTOs);
        productResponseDTO.setCategory(category.getName());
        return ResponseEntity.ok(productResponseDTO);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addEmployee(@Validated @ModelAttribute("product") ProductCreateDTO productCreateDTO,
                                         BindingResult bindingResult,
                                         @RequestParam("avatar") Map<String, List<MultipartFile>> files) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        productService.save(productCreateDTO, files);
        return ResponseEntity.ok().body(productCreateDTO);

    }
}
