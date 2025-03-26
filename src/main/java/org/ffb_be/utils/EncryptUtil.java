package org.ffb_be.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptUtil {
    private final SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";


    public EncryptUtil(@Value("${aes.secret.key}") String key) {
        if (key.length() != 16 && key.length() != 24 && key.length() != 32) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes long.");
        }
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    // Hàm mã hóa AES với CBC
    public String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Tạo IV ngẫu nhiên
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Ghép IV + dữ liệu mã hóa -> Base64
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu", e);
        }
    }

    // Hàm giải mã AES với CBC
    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Lấy IV từ dữ liệu đã mã hóa
            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Giải mã dữ liệu
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(combined, iv.length, combined.length - iv.length);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã dữ liệu", e);
        }
    }

//    public static void main(String[] args) {
//        String key = "abcdefghijklmnop"; // Key phải 16, 24 hoặc 32 bytes
//        EncryptUtil encryptUtil = new EncryptUtil(key);
//
//        String originalText = "Hello, AES Test!";
//        String encryptedText = encryptUtil.encrypt(originalText);
//        String decryptedText = encryptUtil.decrypt(encryptedText);
//
//        System.out.println("Chuỗi gốc: " + originalText);
//        System.out.println("Mã hóa: " + encryptedText);
//        System.out.println("Giải mã: " + decryptedText);
//    }


}
