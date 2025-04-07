package org.ffb_be.service.product;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.discount.DiscountDTO2;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.dto.product.recommendation.FeedbackDataDTO;
import org.ffb_be.dto.product.recommendation.FoodDataDTO;
import org.ffb_be.dto.product.recommendation.OrderDataDTO;
import org.ffb_be.dto.product.recommendation.OrderItemDataDTO;
import org.ffb_be.entity.Discount;
import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.*;
import org.ffb_be.utils.mapping.FeedbackMapper;
import org.ffb_be.utils.mapping.OrderMapper;
import org.ffb_be.utils.mapping.ProductMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final FeedbackRepository feedbackRepository;
    private final DiscountRepository discountRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final FeedbackMapper feedbackMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rcm_url}")
    private String PYTHON_API_URL;

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Override
    public List<ProductResponseDTO> getRecommendations(Long userId, String productType, int top) {
        String url = String.format("%s/recommend/%d/%s/%d", PYTHON_API_URL, userId, productType, top);

        // 1. Tạo JWT token
        String token = Jwts.builder()
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 phút
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes(StandardCharsets.UTF_8))
                .compact();

        // 2. Tạo header với Authorization
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 3. Gửi request kèm header
        ResponseEntity<List<Map<String, Long>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = response.getBody().stream()
                .map(map -> map.get("productId"))
                .collect(Collectors.toList());

        // Tìm sản phẩm trong DB
        List<Product> products = productRepository.findByIdIn(productIds);

        // Sắp xếp lại danh sách theo thứ tự ban đầu của productIds
        Map<Long, Product> productMap = new LinkedHashMap<>();
        productIds.forEach(id -> productMap.put(id, null));
        for (Product p : products) {
            productMap.put(p.getId(), p);
        }

        return productMap.values().stream()
                .filter(Objects::nonNull)
                .map(this::getDto)
                .collect(Collectors.toList());

    }


    @Override
    public Map<String, Object> getAllData() {
        Map<String, Object> response = new HashMap<>();

        List<OrderDataDTO> orderDataDTOS = orderRepository.findAll()
                .stream()
                .map(orderMapper::toDTOData)
                .toList();

        List<FoodDataDTO> foodDataDTOS = productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .toList();

        List<OrderItemDataDTO> orderItemDataDTOS = orderItemRepository.findAll()
                .stream()
                .map(orderMapper::toDTO)
                .toList();

        List<FeedbackDataDTO> feedbackDTOs = feedbackRepository.findAll()
                .stream()
                .map(feedbackMapper::toDTO)
                .toList();

        response.put("orders", orderDataDTOS);
        response.put("order_details", orderItemDataDTOS);
        response.put("products", foodDataDTOS);
        response.put("feedbacks", feedbackDTOs);

        return response;
    }

    private ProductResponseDTO getDto(Product product) {
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
        return productResponseDTO;
    }
}
