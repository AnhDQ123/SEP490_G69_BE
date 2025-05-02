package org.ffb_be.utils.enums.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.ffb_be.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryUpload {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // Nếu file rỗng thì không upload
        }
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("url"); // Returns the uploaded image URL
    }

    public List<String> uploadFiles(MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            urls.add((String) uploadResult.get("url"));
        }
        return urls;
    }

    public void deleteImagesFromCloudinary(List<String> urls) {
        for (String url : urls) {
            String publicId = extractPublicIdFromUrl(url);
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                System.err.println("Failed to delete image: " + publicId);
            }
        }
    }

    public String safeUpload(MultipartFile file, String fileName) throws IOException {
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BadRequestException(fileName + " vượt quá dung lượng tối đa 5MB.");
            }
            try {
                return uploadFile(file);
            } catch (Exception e) {
                throw new IOException("Không thể upload " + fileName);
            }
        }
        return null;
    }

    private String extractPublicIdFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")); // Lấy ID từ URL ảnh
    }
}