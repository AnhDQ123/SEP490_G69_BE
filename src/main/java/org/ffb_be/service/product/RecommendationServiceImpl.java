package org.ffb_be.service.product;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.dto.product.recommendation.FeedbackDataDTO;
import org.ffb_be.dto.product.recommendation.FoodDataDTO;
import org.ffb_be.dto.product.recommendation.OrderDataDTO;
import org.ffb_be.dto.product.recommendation.OrderItemDataDTO;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.FeedbackRepository;
import org.ffb_be.repository.OrderItemRepository;
import org.ffb_be.repository.OrderRepository;
import org.ffb_be.repository.ProductRepository;
import org.ffb_be.utils.mapping.FeedbackMapper;
import org.ffb_be.utils.mapping.OrderMapper;
import org.ffb_be.utils.mapping.ProductMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final FeedbackRepository feedbackRepository;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final FeedbackMapper feedbackMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_API_URL = "http://localhost:5000";

    @Override
    public List<ProductResponseDTO> getRecommendations(Long userId, String productType, int top) {
        String url = String.format("%s/recommend/%d/%s/%d", PYTHON_API_URL, userId, productType, top);

        ResponseEntity<List<Map<String, Long>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Long>>>() {}
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy danh sách productId từ JSON
        List<Long> productIds = response.getBody().stream()
                .map(map -> map.get("productId"))
                .collect(Collectors.toList());

        // Tìm sản phẩm trong DB
        List<Product> products = productRepository.findByIdIn(productIds);

        // Chuyển đổi sang DTO
        return products.stream()
                .map(this::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAllData() {
        Map<String, Object> response = new HashMap<>();

        List<OrderDataDTO> orderDataDTOS = orderRepository.findAll()
                .stream()
                .map(orderMapper::toDTO)
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
        productResponseDTO.setImage(product.getImage());
        if (product.getCategory() != null) {
            productResponseDTO.setCategory(product.getCategory().getName());
        }
        productResponseDTO.setSupplier(product.getSupplier());
        return productResponseDTO;
    }
}
