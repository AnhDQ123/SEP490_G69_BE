package org.ffb_be.service.product;



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
import java.util.Optional;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final FoodOptionRepository foodOptionRepository;
    private final DiscountRepository discountRepository;
    private final TypesRepository typesRepository;
    private final OrderRepository orderRepository;
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CloudinaryUpload cloudinaryUpload, FoodOptionRepository foodOptionRepository, DiscountRepository discountRepository, TypesRepository typesRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryUpload = cloudinaryUpload;
        this.foodOptionRepository = foodOptionRepository;
        this.discountRepository = discountRepository;
        this.typesRepository = typesRepository;
        this.orderRepository = orderRepository;
    }

    public void save(ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException {
        Product product = new Product();
        Category category = categoryRepository.findByName(productCreateDTO.getCategory());
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
    public List<ProductResponseDTO> findPopularProducts() {
        List<Object[]> products=orderRepository.findTopSellingProducts();
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Object[] objects : products) {
            Product product=productRepository.findById((Long) objects[0]).get();
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

    @Override
    public List<ProductResponseDTO> findFreshProducts() {
        List<Product> products = productRepository.findFreshProducts();
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Product product : products) {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

    @Override
    public List<ProductResponseDTO> findCookedProducts() {
        List<Product> products = productRepository.findCookedProducts();
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Product product : products) {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

    @Override
    public List<ProductResponseDTO> findByCategory(String cat) {
        Category category = categoryRepository.findByName(cat);
        List<Product> productList=productRepository.findAllByCategory(category);
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            if (product.getDiscount() != null && product.getDiscount().getId() != null) {
                Discount d = discountRepository.findById(product.getDiscount().getId());
                productResponseDTO.setDiscount(d.getDiscount_percentage());
            } else {
                productResponseDTO.setDiscount(BigDecimal.ZERO);
            }

            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        Product product = productOptional.get();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
        List<FoodOptionDTO> foodOptionDTOs = new ArrayList<>();
        for (FoodOption foodOption : foodOptions) {
            FoodOptionDTO dto = new FoodOptionDTO();
            dto.setType_id(foodOption.getType().getId());
            BeanUtils.copyProperties(foodOption, dto);
            foodOptionDTOs.add(dto);
        }
        BeanUtils.copyProperties(product, productResponseDTO);
        if (product.getDiscount() != null && product.getDiscount().getId() != null) {
            Discount d = discountRepository.findById(product.getDiscount().getId());
            productResponseDTO.setDiscount(d.getDiscount_percentage());
        } else {
            productResponseDTO.setDiscount(BigDecimal.ZERO);
        }

        Optional<Category> c=categoryRepository.findById(product.getCategory().getId());
        Category category = c.get();
        productResponseDTO.setFoodOption(foodOptionDTOs);
        productResponseDTO.setCategory(category.getName());
        return productResponseDTO;
    }

    @Override
    public List<ProductResponseDTO> findSimimlarProduct(String name) {
        List<Product> products=productRepository.findSimilarProducts(name);
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Product product : products) {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
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
