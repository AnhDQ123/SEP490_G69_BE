package org.ffb_be.service.product;



import lombok.RequiredArgsConstructor;


import org.ffb_be.dto.discount.DiscountDTO2;
import org.ffb_be.dto.product.FoodOptionDTO;
import org.ffb_be.dto.product.ProductCreateDTO;
import org.ffb_be.dto.product.ProductResponseDTO;

import org.ffb_be.entity.*;
import org.ffb_be.repository.*;

import org.ffb_be.utils.enums.SellType;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final FoodOptionRepository foodOptionRepository;
    private final DiscountRepository discountRepository;
    private final TypesRepository typesRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
//    private final ProductESRepository productESRepository;
//    @Qualifier("elasticsearchTemplate")
//    private final ElasticsearchOperations elasticsearchOperations;

@Override
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
        product.setShop(shopRepository.findById(productCreateDTO.getShopId()).get());
        product.setSupplier(productCreateDTO.getSupplier());
        product.setCategory(category);
        product.setType(SellType.valueOf(productCreateDTO.getFoodType()));
        product.setStatus(Status.ACTIVE);
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
    public void update(Long id, ProductCreateDTO productCreateDTO, MultipartFile avatar, List<MultipartFile> option) throws IOException {
        Product product = productRepository.findById(id).get();
        Category category = categoryRepository.findByName(productCreateDTO.getCategory());
        List<FoodOption> foodOptions = new ArrayList<>();
        List<FoodOptionDTO> foodOptionDTOs = productCreateDTO.getFoodOption();
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setExpired_date(productCreateDTO.getExpiryDate());
        product.setQuantity(productCreateDTO.getQuantity());
        product.setManufacturer(productCreateDTO.getManufacturer());
        product.setShop(shopRepository.findById(productCreateDTO.getShopId()).get());
        product.setSupplier(productCreateDTO.getSupplier());
        product.setCategory(category);
        product.setType(SellType.valueOf(productCreateDTO.getFoodType()));
        product.setStatus(Status.ACTIVE);
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
    public void delete(Long id) throws IOException {
        Product product = productRepository.findById(id).get();
        product.setStatus(Status.INACTIVE);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponseDTO> findAllByShop(Long id,Pageable pageable) {
        return productRepository.findAllByShop_Id(id,pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setShopName(product.getShop().getName());
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setSupplier(product.getShop() != null ? product.getShop().getName() : "");
            productResponseDTO.setRate(product.getRate());
            productResponseDTO.setQuantity(product.getQuantity());
            productResponseDTO.setStatus(product.getStatus().toString());
            productResponseDTO.setDescription(product.getDescription());
            productResponseDTO.setFoodType(product.getType().toString());
            BigDecimal defaultprice = null;
            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
            List<Discount> discount=discountRepository.findAllByProduct_Id((product.getId()));
            if(discount!=null) {
                List<DiscountDTO2> discountDTOs=new ArrayList<>();
                for (Discount discount1:discount) {
                    DiscountDTO2 discountDTO=new DiscountDTO2();
                    discountDTO.setAmount(discount1.getDiscount_percentage());
                    discountDTO.setId(discount1.getId());
                    discountDTO.setStartDate(discount1.getStartDate());
                    discountDTO.setEndDate(discount1.getEndDate());
                    discountDTO.setStatus(discount1.getStatus().toString());
                    discountDTOs.add(discountDTO);
                }
                productResponseDTO.setDiscount(discountDTOs);
            }else {
                productResponseDTO.setDiscount(null);
            }
            for (FoodOption foodOption : foodOptions) {
                if (foodOption.getType().getId() == 2) {
                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                        defaultprice = foodOption.getPrice();
                    }
                }
            }
            productResponseDTO.setDefaultPrice(defaultprice != null ? defaultprice : BigDecimal.ZERO);
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
    }

    @Override
    public List<ProductResponseDTO> findPopularProducts() {
        List<Object[]> products = orderRepository.findTopSellingProducts();
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for (Object[] objects : products) {
            Product product = productRepository.findById((Long) objects[0]).get();
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setName(product.getName());
            productResponseDTO.setManufacturer(product.getManufacturer());
            productResponseDTO.setSupplier(product.getSupplier());
            productResponseDTO.setImage(product.getImage());
            productResponseDTO.setRate(product.getRate()); // thêm rate
            productResponseDTO.setQuantity(product.getQuantity()); // thêm quantity
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setShopName(product.getShop().getName());
            productResponseDTO.setDescription(product.getDescription());
            productResponseDTO.setFoodType(product.getType().toString());
            // Tính defaultPrice từ các FoodOption có type id = 2
            List<Discount> discount=discountRepository.findAllByProduct_Id((product.getId()));
            if(discount!=null) {
                List<DiscountDTO2> discountDTOs=new ArrayList<>();
                for (Discount discount1:discount) {
                    DiscountDTO2 discountDTO=new DiscountDTO2();
                    discountDTO.setAmount(discount1.getDiscount_percentage());
                    discountDTO.setId(discount1.getId());
                    discountDTO.setStartDate(discount1.getStartDate());
                    discountDTO.setEndDate(discount1.getEndDate());
                    discountDTO.setStatus(discount1.getStatus().toString());
                    discountDTOs.add(discountDTO);
                }
                productResponseDTO.setDiscount(discountDTOs);
            }else {
                productResponseDTO.setDiscount(null);
            }
            BigDecimal defaultprice = null;
            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
            for (FoodOption foodOption : foodOptions) {
                if (foodOption.getType().getId() == 2) {
                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                        defaultprice = foodOption.getPrice();
                    }
                }
            }
            productResponseDTO.setDefaultPrice(defaultprice != null ? defaultprice : BigDecimal.ZERO);

            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

//    @Override
//    public List<ProductResponseDTO> findFreshProducts() {
//        List<Product> products = productRepository.findFreshProducts();
//        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
//
//        for (Product product : products) {
//            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
//            productResponseDTO.setId(product.getId());
//            productResponseDTO.setName(product.getName());
//            productResponseDTO.setManufacturer(product.getManufacturer());
//            productResponseDTO.setImage(product.getImage());
//            productResponseDTO.setCategory(product.getCategory().getName());
//            productResponseDTO.setShopName(product.getShop().getName());
//            productResponseDTO.setRate(product.getRate());
//            productResponseDTO.setQuantity(product.getQuantity());
//
//            // Tính defaultPrice cho sản phẩm fresh
//            BigDecimal defaultprice = null;
//            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
//
//            for (FoodOption foodOption : foodOptions) {
//                if (foodOption.getType().getId() == 2) { // điều kiện tùy theo nghiệp vụ thực tế
//                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
//                        defaultprice = foodOption.getPrice();
//                    }
//                }
//            }
//
//            productResponseDTO.setDefaultPrice(defaultprice); // set giá vừa tính được
//            productResponseDTO.setImage(product.getImage());
//
//            productResponseDTOList.add(productResponseDTO);
//        }
//
//        return productResponseDTOList;
//    }
//
//    @Override
//    public List<ProductResponseDTO> findCookedProducts() {
//        List<Product> products = productRepository.findCookedProducts();
//        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
//
//        for (Product product : products) {
//            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
//            productResponseDTO.setId(product.getId());
//            productResponseDTO.setName(product.getName());
//            productResponseDTO.setManufacturer(product.getManufacturer());
//            productResponseDTO.setImage(product.getImage());
//            productResponseDTO.setCategory(product.getCategory().getName());
//            productResponseDTO.setSupplier(product.getSupplier());
//            productResponseDTO.setRate(product.getRate());
//            productResponseDTO.setQuantity(product.getQuantity());
//            productResponseDTO.setShopName(product.getShop().getName());
//
//            BigDecimal defaultprice = null;
//            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
//
//            for (FoodOption foodOption : foodOptions) {
//                if (foodOption.getType().getId() == 2) {
//                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
//                        defaultprice = foodOption.getPrice();
//                    }
//                }
//            }
//
//            // Thêm dòng này để set giá defaultPrice vào DTO
//            productResponseDTO.setDefaultPrice(defaultprice);
//
//            productResponseDTOList.add(productResponseDTO);
//        }
//
//        return productResponseDTOList;
//    }

    @Override
    public List<ProductResponseDTO> findByCategory(String cat) {
        Category category = categoryRepository.findByName(cat);
        List<Product> productList = productRepository.findAllByCategory(category);
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();

        for (Product product : productList) {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            productResponseDTO.setDescription(product.getDescription());
            productResponseDTO.setFoodType(product.getType().toString());
            BigDecimal defaultprice = null;
            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
            for (FoodOption foodOption : foodOptions) {
                if (foodOption.getType().getId() == 2) {
                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                        defaultprice = foodOption.getPrice();
                    }
                }
            }
            List<FoodOptionDTO> foodOptionDTOs = new ArrayList<>();
            for (FoodOption foodOption : foodOptions) {
                FoodOptionDTO dto = new FoodOptionDTO();
                dto.setType_id(foodOption.getType().getId());
                BeanUtils.copyProperties(foodOption, dto);
                foodOptionDTOs.add(dto);
            }
            productResponseDTO.setShopName(product.getShop().getName());
            List<Discount> discount=discountRepository.findAllByProduct_Id((product.getId()));
            if(discount!=null) {
                List<DiscountDTO2> discountDTOs=new ArrayList<>();
                for (Discount discount1:discount) {
                    DiscountDTO2 discountDTO=new DiscountDTO2();;
                    discountDTO.setAmount(discount1.getDiscount_percentage());
                    discountDTO.setId(discount1.getId());
                    discountDTO.setStartDate(discount1.getStartDate());
                    discountDTO.setEndDate(discount1.getEndDate());
                    discountDTO.setStatus(discount1.getStatus().toString());
                    discountDTOs.add(discountDTO);
                }
                productResponseDTO.setDiscount(discountDTOs);
            }else {
                productResponseDTO.setDiscount(null);
            }
            for(Discount discount1:discount){
                if(discount1.getStatus().equals(Status.ACTIVE)){
                    defaultprice=defaultprice.multiply(discount1.getDiscount_percentage());
                }
            }
            productResponseDTO.setDefaultPrice(defaultprice);
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTOList.add(productResponseDTO);
        }
        return productResponseDTOList;
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findById(id).get();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        BeanUtils.copyProperties(product, productResponseDTO);
        productResponseDTO.setDescription(product.getDescription());
        productResponseDTO.setFoodType(product.getType().toString());
        BigDecimal defaultprice = null;
        List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
        for (FoodOption foodOption : foodOptions) {
            if (foodOption.getType().getId() == 2) {
                if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                    defaultprice = foodOption.getPrice();
                }
            }
        }
        List<FoodOptionDTO> foodOptionDTOs = new ArrayList<>();
        for (FoodOption foodOption : foodOptions) {
            FoodOptionDTO dto = new FoodOptionDTO();
            dto.setType_id(foodOption.getType().getId());
            BeanUtils.copyProperties(foodOption, dto);
            foodOptionDTOs.add(dto);
        }
        productResponseDTO.setShopName(product.getShop().getName());
        List<Discount> discount=discountRepository.findAllByProduct_Id((product.getId()));
        if(discount!=null) {
            List<DiscountDTO2> discountDTOs=new ArrayList<>();
            for (Discount discount1:discount) {
                DiscountDTO2 discountDTO=new DiscountDTO2();;
                discountDTO.setAmount(discount1.getDiscount_percentage());
                discountDTO.setId(discount1.getId());
                discountDTO.setStartDate(discount1.getStartDate());
                discountDTO.setEndDate(discount1.getEndDate());
                discountDTO.setStatus(discount1.getStatus().toString());
                discountDTOs.add(discountDTO);
            }
            productResponseDTO.setDiscount(discountDTOs);
        }else {
            productResponseDTO.setDiscount(null);
        }
        for(Discount discount1:discount){
            if(discount1.getStatus().equals(Status.ACTIVE)){
                defaultprice=defaultprice.multiply(discount1.getDiscount_percentage());
            }
        }
        productResponseDTO.setDefaultPrice(defaultprice);
        Optional<Category> c=categoryRepository.findById(product.getCategory().getId());
        Category category = c.get();
        productResponseDTO.setFoodOption(foodOptionDTOs);
        productResponseDTO.setCategory(category.getName());
        return productResponseDTO;
    }

    @Override
    public List<ProductResponseDTO> findSimimlarProduct(String name) {
        String[] words = name.split("\\s+");
        List<ProductResponseDTO> productResponseDTOList = new ArrayList<>();
        for(String word:words) {
            List<Product> products = productRepository.findSimilarProducts(word);
            for (Product product : products) {
                ProductResponseDTO productResponseDTO = new ProductResponseDTO();
                productResponseDTO.setId(product.getId());
                productResponseDTO.setName(product.getName());
                productResponseDTO.setManufacturer(product.getManufacturer());
                productResponseDTO.setImage(product.getImage());
                productResponseDTO.setCategory(product.getCategory().getName());
                productResponseDTO.setSupplier(product.getShop() != null ? product.getShop().getName() : "");
                productResponseDTO.setRate(product.getRate());
                productResponseDTO.setQuantity(product.getQuantity());
                productResponseDTO.setDescription(product.getDescription());
                productResponseDTO.setFoodType(product.getType().toString());
                productResponseDTO.setShopName(product.getShop().getName());
                BigDecimal defaultprice = null;
                List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
                for (FoodOption foodOption : foodOptions) {
                    if (foodOption.getType().getId() == 2) {
                        if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                            defaultprice = foodOption.getPrice();
                        }
                    }
                }
                productResponseDTO.setDefaultPrice(defaultprice != null ? defaultprice : BigDecimal.ZERO);
                productResponseDTOList.add(productResponseDTO);
            }
        }
        return productResponseDTOList;
    }

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
            BigDecimal defaultprice = null;
            for (FoodOption foodOption : foodOptions) {
                if (foodOption.getType().getId() == 2) {
                    if (defaultprice == null || foodOption.getPrice().compareTo(defaultprice) < 0) {
                        defaultprice = foodOption.getPrice();
                    }
                }
            }
            List<FoodOptionDTO> foodOptionDTOs = new ArrayList<>();
            for (FoodOption foodOption : foodOptions) {
                FoodOptionDTO dto = new FoodOptionDTO();
                dto.setType_id(foodOption.getType().getId());
                BeanUtils.copyProperties(foodOption, dto);
                foodOptionDTOs.add(dto);
            }
            productResponseDTO.setShopName(product.getShop().getName());
            List<Discount> discount=discountRepository.findAllByProduct_Id((product.getId()));
            if(discount!=null) {
                List<DiscountDTO2> discountDTOs=new ArrayList<>();
                for (Discount discount1:discount) {
                    DiscountDTO2 discountDTO=new DiscountDTO2();;
                    discountDTO.setAmount(discount1.getDiscount_percentage());
                    discountDTO.setId(discount1.getId());
                    discountDTO.setStartDate(discount1.getStartDate());
                    discountDTO.setEndDate(discount1.getEndDate());
                    discountDTO.setStatus(discount1.getStatus().toString());
                    discountDTOs.add(discountDTO);
                }
                productResponseDTO.setDiscount(discountDTOs);
            }else {
                productResponseDTO.setDiscount(null);
            }
            for(Discount discount1:discount){
                if(discount1.getStatus().equals(Status.ACTIVE)){
                    defaultprice=defaultprice.multiply(discount1.getDiscount_percentage());
                }
            }
            productResponseDTO.setCategory(product.getCategory().getName());
            productResponseDTO.setDescription(product.getDescription());
            productResponseDTO.setFoodType(product.getType().toString());
            productResponseDTO.setShopName(product.getShop().getName());
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
    }
    public List<Object[]> findTopSellingProductsToday(Long shopId) {
        return productRepository.findTopSellingProductsToday(shopId);
    }

    // Tính danh sách sản phẩm bán chạy nhất trong tháng này cho cửa hàng cụ thể
    public List<Object[]> findTopSellingProductsThisMonth(Long shopId) {
        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());  // Ngày đầu tháng
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());    // Ngày cuối tháng

        // Chuyển các ngày thành LocalDateTime
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);
        return productRepository.findTopSellingProductsThisMonth(shopId,startOfMonth,endOfMonth);
    }

    // Tính danh sách sản phẩm bán chạy nhất trong năm này cho cửa hàng cụ thể
    public List<Object[]> findTopSellingProductsThisYear(Long shopId) {
        return productRepository.findTopSellingProductsThisYear(shopId);
    }

    @Override
    public List<ProductResponseDTO> findByCategoryByShop(Long shopId) {
        List<ProductResponseDTO> productResponseDTOList=findByCategory("Đồ uống");
        Shop shop=shopRepository.findById(shopId).get();
        List<ProductResponseDTO> productResponseDTOList2=new ArrayList<>();
        for(ProductResponseDTO productResponseDTO:productResponseDTOList) {
            if(productResponseDTO.getShopName().equals(shop.getName())) {
                productResponseDTOList2.add(productResponseDTO);
            }
        }
        return productResponseDTOList2;
    }

//    @Override
//    public List<ProductResponseDTO> searchHighlySimilarProducts(String query, int page, int size) {
//
//
//        // Search with 90% similarity threshold
//        Page<Product> products = productESRepository.findHighlySimilarProducts(
//                query,
//                PageRequest.of(page - 1, size)
//        );
//
//
//        // 3. Lấy đầy đủ thông tin từ database
//        List<Long> productIds = products.getContent()
//                .stream()
//                .map(Product::getId)
//                .distinct()
//                .toList();
//
//        return productRepository.findAllById(productIds)
//                .stream()
//                .map(product -> {
//                    ProductResponseDTO dto = new ProductResponseDTO();
//                    // Copy các thuộc tính cơ bản
//                    BeanUtils.copyProperties(product, dto);
//                    dto.setDescription(product.getDescription());
//                    dto.setFoodType(product.getType().toString());
//
//                    // Xử lý food options
//                    List<FoodOption> foodOptions = foodOptionRepository.findFoodOptionsByFood(product);
//                    BigDecimal defaultPrice = calculateDefaultPrice(foodOptions);
//                    List<FoodOptionDTO> foodOptionDTOs = convertFoodOptionsToDTOs(foodOptions);
//
//                    // Xử lý discounts
//                    List<Discount> discounts = discountRepository.findAllByProduct_Id(product.getId());
//                    List<DiscountDTO2> discountDTOs = convertDiscountsToDTOs(discounts);
//
//                    // Áp dụng discount nếu có
//                    if (!discounts.isEmpty()) {
//                        defaultPrice = applyActiveDiscounts(defaultPrice, discounts);
//                    }
//
//                    // Set các giá trị vào DTO
//                    dto.setDefaultPrice(defaultPrice);
//                    dto.setFoodOption(foodOptionDTOs);
//                    dto.setDiscount(discountDTOs);
//                    dto.setShopName(product.getShop() != null ? product.getShop().getName() : null);
//                    dto.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
//
//                    return dto;
//                })
//                .toList();
//    }
//
//    private BigDecimal calculateDefaultPrice(List<FoodOption> foodOptions) {
//        return foodOptions.stream()
//                .filter(fo -> fo.getType() != null && fo.getType().getId() == 2)
//                .map(FoodOption::getPrice)
//                .min(BigDecimal::compareTo)
//                .orElse(BigDecimal.ZERO);
//    }
//
//    private List<FoodOptionDTO> convertFoodOptionsToDTOs(List<FoodOption> foodOptions) {
//        return foodOptions.stream()
//                .map(fo -> {
//                    FoodOptionDTO dto = new FoodOptionDTO();
//                    dto.setType_id(fo.getType().getId());
//                    BeanUtils.copyProperties(fo, dto);
//                    return dto;
//                })
//                .toList();
//    }
//
//    private List<DiscountDTO2> convertDiscountsToDTOs(List<Discount> discounts) {
//        return discounts.stream()
//                .map(d -> {
//                    DiscountDTO2 dto = new DiscountDTO2();
//                    dto.setId(d.getId());
//                    dto.setAmount(d.getDiscount_percentage());
//                    dto.setStartDate(d.getStartDate());
//                    dto.setEndDate(d.getEndDate());
//                    dto.setStatus(d.getStatus().toString());
//                    return dto;
//                })
//                .toList();
//    }
//
//    private BigDecimal applyActiveDiscounts(BigDecimal price, List<Discount> discounts) {
//        BigDecimal finalPrice = price;
//        for (Discount discount : discounts) {
//            if (discount.getStatus().equals(Status.ACTIVE)) {
//                finalPrice = finalPrice.multiply(
//                        BigDecimal.ONE.subtract(discount.getDiscount_percentage().divide(BigDecimal.valueOf(100)))
//                                .setScale(2, RoundingMode.HALF_UP));
//            }
//        }
//        return finalPrice;
//    }
//
//    @Transactional
//    public String syncAllProducts() {
//        // Lấy toàn bộ sản phẩm từ MySQL (có thể thêm phân trang nếu dữ liệu lớn)
//        List<Product> dbProducts = productRepository.findAll();
//
//        // Chuyển đổi sang ProductES
//        List<ProductES> esProducts = dbProducts.stream()
//                .map(this::convertToProductES)
//                .collect(Collectors.toList());
//
//        // Xóa index cũ và tạo lại (tuỳ nhu cầu)
//        productESRepository.deleteAll();
//
//        // Lưu vào Elasticsearch
//        productESRepository.saveAll(esProducts);
//
//        return "Đã đồng bộ " + esProducts.size() + " sản phẩm";
//    }
//
//    private ProductES convertToProductES(Product product) {
//        return ProductES.builder()
//                .id(product.getId())
//                .name(product.getName())
//                .description(product.getDescription())
//                .manufacturer(product.getManufacturer())
//                // Có thể thêm các trường khác nếu cần
//                .build();
//    }
}
