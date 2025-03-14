package org.ffb_be.service.product;

import org.ffb_be.dto.product.ProductResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface RecommendationService {
    List<ProductResponseDTO> getRecommendations(Long userId, String productType, int top);

    Map<String, Object> getAllData();
}
