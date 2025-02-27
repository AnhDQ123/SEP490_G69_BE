package org.ffb_be.service.user;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class SMSService {



    @Value("${API_KEY}")
    private String key;

    @Value("${SECRET_KEY}")
    private String secret;

    @Value("${API_URL}")
    private String API_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    private final OtpCacheService otpCacheService;

    public SMSService(OtpCacheService otpCacheService) {
        this.otpCacheService = otpCacheService;
    }

    public String sendOtp(String phone) {

            // Loại bỏ dấu `+` khỏi số điện thoại
            String formattedPhone = formatPhoneNumber(phone);

            // Tạo OTP ngẫu nhiên
            String otp = generateOtp();
            String message = "Ma OTP cua ban la: " + otp;
            otpCacheService.putOtp(phone, otp);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tạo body dạng JSON
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ApiKey", key);
            requestBody.put("SecretKey", secret);
            requestBody.put("Phone", formattedPhone);
            requestBody.put("Content", message);
            requestBody.put("SmsType", 4);

            // Tạo HttpEntity với requestBody dạng JSON
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, String.class);
            return response.getBody();
    }

    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("+84")) {
            return phone.substring(1);  // Loại bỏ dấu `+`
        }
        if (phone.startsWith("0")) {
            return "84" + phone.substring(1); // Chuyển `098` -> `8498`
        }
        return phone;
    }
}
