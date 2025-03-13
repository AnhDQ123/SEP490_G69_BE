package org.ffb_be.service.product;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.entity.Feedback;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.OrderItem;
import org.ffb_be.entity.Product;
import org.ffb_be.repository.FeedbackRepository;
import org.ffb_be.repository.OrderItemRepository;
import org.ffb_be.repository.OrderRepository;
import org.ffb_be.repository.ProductRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PYTHON_API_URL = "http://localhost:5000";

    @Override
    public List<ProductResponseDTO> getRecommendations(Long userId, String productType, int top) {
        String url = String.format("%s/recommend/%d/%s/%d", PYTHON_API_URL, userId, productType, top);

        ResponseEntity<List<Long>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Long>>() {}
        );

        List<Long> productIds = response.getBody();
        List<Product> products = productRepository.findByIdIn(productIds);

        return products.stream().map(this::getDto).collect(Collectors.toList());
    }

    @Override
    public void sendDataToPython() {
        List<Product> products = productRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<OrderItem> orderDetails = orderItemRepository.findAll();
        List<Feedback> feedbacks = feedbackRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("products", products);
        data.put("orders", orders);
        data.put("order_details", orderDetails);
        data.put("feedbacks", feedbacks);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(PYTHON_API_URL + "/post-data", data, String.class);
            System.out.println("Dữ liệu đã được gửi thành công! Phản hồi từ Python: " + response.getBody());
        } catch (HttpStatusCodeException e) {
            System.err.println("Lỗi HTTP: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            System.err.println("Lỗi kết nối đến Python API: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
        }
    }

    private ProductResponseDTO getDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .manufacturer(product.getManufacturer())
                .image(product.getImage())
                .category(product.getCategory().getName())
                .supplier(product.getSupplier())
                .shopName(product.getShop().getName())
                .build();
    }
}
