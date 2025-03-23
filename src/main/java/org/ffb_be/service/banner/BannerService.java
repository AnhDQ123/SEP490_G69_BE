package org.ffb_be.service.banner;

import org.ffb_be.dto.image.ImageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BannerService {
    Page<ImageDTO> getBanners(Pageable pageable);

    void createBanner(ImageDTO banner, MultipartFile file) throws IOException;

    void updateBanner(Long id, ImageDTO image, MultipartFile file) throws IOException;

    void delete(Long id);
}
