package org.ffb_be.controller;

import org.ffb_be.service.user.OtpCacheService;
import org.ffb_be.service.user.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
public class SMSRestController {

    @Autowired
    private SMSService smsService;
    private OtpCacheService otpCacheService;
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendSms(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");

        String response = smsService.sendOtp(phone);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otpInput = request.get("otp");
        String storedOtp = otpCacheService.getOtp(phone);
        if (storedOtp != null && storedOtp.equals(otpInput)) {
            return ResponseEntity.ok("{\"CodeResult\": \"00\", \"Message\": \"OTP Verified\"}");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"CodeResult\": \"01\", \"Message\": \"OTP Incorrect or Expired\"}");
        }
    }
}
