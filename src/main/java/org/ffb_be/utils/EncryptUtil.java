package org.ffb_be.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptUtil {
    private final SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public EncryptUtil(@Value("${aes.secret.key}") String key) {
        this.secretKey = new SecretKeySpec(hashKey(key), ALGORITHM);
    }

    private byte[] hashKey(String key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return sha.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo khóa AES", e);
        }
    }

    public String encrypt(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) return null;
        try {
            byte[] combined = Base64.getUrlDecoder().decode(encryptedData);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

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
