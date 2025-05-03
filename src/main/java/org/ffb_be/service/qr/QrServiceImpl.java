package org.ffb_be.service.qr;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.Shop;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.OrderRepository;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.ShopRepository;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    private final ProfileRepository profileRepository;

    private final ShopRepository shopRepository;

    private final CloudinaryUpload cloudinaryUpload;

    private final RestTemplate restTemplate = new RestTemplate();

    private final EncryptUtil encryptUtil;

    @Override
    public String generateQrCode(Long orderId, Long shopId) {
        Order order = orderRepository.findById(orderId).orElseThrow (() -> new NotFoundException("Order"));
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new NotFoundException("Shop"));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNo", decryptSafe(shop.getAccountNumber()));
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
                return (String) data.get("qrDataURL");
            }
            return null;
        }
        return null;
    }

    @Override
    public String getUserQrCode(Long userId, Long orderId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNo", decryptSafe(profile.getAccountNumber()));
        requestBody.put("accountName", profile.getName());
        requestBody.put("acqId", profile.getBankCode());
        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElseThrow (() -> new NotFoundException("Order"));
            if(!order.getOwner().getId().equals(userId)){
                throw new RuntimeException("Order does not belong to this user");
            }
            requestBody.put("amount", order.getTotal());
            requestBody.put("addInfo", "ORDER" + order.getOrderCode());
        }
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
                return (String) data.get("qrDataURL");
            }
            return null;
        }
        return null;
    }

    @Override
    public void updatePaymentProof(Long orderId, MultipartFile paymentProof) throws IOException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order"));
        if (paymentProof != null && !paymentProof.isEmpty()) {
            order.setPaymentProof(cloudinaryUpload.uploadFile(paymentProof));
        }
        orderRepository.save(order);
    }

    private String decryptSafe(String data) {
        return data != null ? encryptUtil.decrypt(data) : null;
    }
}
