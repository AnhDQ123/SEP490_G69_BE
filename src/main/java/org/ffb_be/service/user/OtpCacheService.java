package org.ffb_be.service.user;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpCacheService {
    // Cache lưu OTP với thời gian sống là 5 phút
    private final Cache<String, String> otpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public void putOtp(String phone, String otp) {
        otpCache.put(phone, otp);
    }

    public String getOtp(String phone) {
        return otpCache.getIfPresent(phone);
    }

    public void removeOtp(String phone) {
        otpCache.invalidate(phone);
    }
}
