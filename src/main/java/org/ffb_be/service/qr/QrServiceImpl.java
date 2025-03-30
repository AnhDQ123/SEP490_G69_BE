package org.ffb_be.service.qr;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.Shop;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.OrderRepository;
import org.ffb_be.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class QrServiceImpl implements QrService {

    @Value("${vietqr.client-id}")
    private String clientId;

    @Value("${vietqr.api-key}")
    private String apiKey;

    private final OrderRepository orderRepository;

    private final ShopRepository shopRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateQrCode(Long orderId, Long shopId) {
        Order order = orderRepository.findById(orderId).orElseThrow (() -> new NotFoundException("Order"));
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new NotFoundException("Shop"));
        if (order.getQrCode() != null) {
            return order.getQrCode();
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNo", shop.getAccountNumber());
        requestBody.put("accountName", shop.getName());
        requestBody.put("acqId", shop.getBankCode());
        requestBody.put("amount", order.getTotal());
        requestBody.put("addInfo", "ORDER" + order.getOrderCode());
        requestBody.put("template", "compact2");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.vietqr.io/v2/generate", HttpMethod.POST, requestEntity, Map.class
        );
        System.out.println("Response: " + response.getBody());
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data != null) {
                String qrDataURL = (String) data.get("qrDataURL");
                order.setQrCode(qrDataURL);
                orderRepository.save(order);
                return qrDataURL;
            }
            return null;
        }
        return null;
    }

}
