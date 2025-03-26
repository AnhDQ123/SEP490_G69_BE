package org.ffb_be.service.banner;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Types;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ImageRepository;
import org.ffb_be.repository.TypesRepository;
import org.ffb_be.utils.enums.TypesCategory;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.ImageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {
    private final ImageRepository imageRepository;
    private final TypesRepository typesRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final ImageMapper imageMapper;

    @Override
    public Page<ImageDTO> getBanners(Pageable pageable) {
        Page<Image> banner = imageRepository.findAllByType_Category(TypesCategory.BANNER,pageable);
        return banner.map(imageMapper::toDTO);
    }

    @Override
    public ImageDTO getBannerById(Long id) {
        Image banner = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner"));
        return imageMapper.toDTO(banner);
    }

    @Override
    public void createBanner(ImageDTO image, MultipartFile file) throws IOException {
        Types type = typesRepository.findByCategory(TypesCategory.BANNER)
                .orElseThrow(() -> new NotFoundException("Type"));
        Image banner = imageMapper.toEntity(image);
        if (file != null){
            String url = cloudinaryUpload.uploadFile(file);
            banner.setUrl(url);
        }
        banner.setCreatedAt(LocalDateTime.now());
        banner.setType(type);
        imageRepository.save(banner);
    }

    @Override
    public void updateBanner(Long id, ImageDTO image, MultipartFile file) throws IOException {
        Image banner = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner"));
        imageMapper.updateEntity(image,banner);
        if (file != null){
            String url = cloudinaryUpload.uploadFile(file);
            banner.setUrl(url);
        }
        banner.setUpdatedAt(LocalDateTime.now());
        imageRepository.save(banner);
    }

    @Override
    public void delete(Long id) {
        Image banner = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner"));
        imageRepository.deleteById(id);
    }
}
