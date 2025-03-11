package org.ffb_be.service.shop;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.Shop;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.ShopRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.ShopMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final EncryptUtil encryptUtil;

    @Override
    public Page<ShopDTO> getShops(String type, String status, String search, Pageable pageable) {
        Specification<Shop> spec = Specification.where(null);

        // Lọc theo status nếu có
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), status));
        }

        // Lọc theo loai hang ban nếu có
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sellType"), type));
        }

        // Tìm kiếm theo tên nếu có
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }

        Page<Shop> shops = shopRepository.findAll(spec, pageable);
        return shops.map(this::decryptShopDTO);
    }


    @Transactional
    @Override
    public void registerShop(
            Long userId,
            ShopRegisterDTO shopDTO,
            MultipartFile logo,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile registrationCert,
            MultipartFile foodSafetyCert,
            MultipartFile menu
    ) throws IOException {
        if (shopRepository.existsByOwnerId(userId)) {
            throw new BadRequestException("User đã có cửa hàng.");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User"));


        Shop shop = shopMapper.toEntity(shopDTO);
        shop.setOwner(owner);
        shop.setIsActive(Status.PENDING);
        shop.setCreatedAt(LocalDateTime.now());
        String logoUrl = cloudinaryUpload.uploadFile(logo);
        if (logoUrl != null) shop.setLogo(logoUrl);

        String citizenIDFrontUrl = cloudinaryUpload.uploadFile(citizenIDFront);
        if (citizenIDFrontUrl != null) profile.setCitizenIDCardFront(citizenIDFrontUrl);

        String citizenIDBackUrl = cloudinaryUpload.uploadFile(citizenIDBack);
        if (citizenIDBackUrl != null) profile.setCitizenIDCardBack(citizenIDBackUrl);

        String registrationCertUrl = cloudinaryUpload.uploadFile(registrationCert);
        if (registrationCertUrl != null) shop.setRegistrationCertificate(registrationCertUrl);

        String menuUrl = cloudinaryUpload.uploadFile(menu);
        if (menuUrl != null) shop.setMenu(menuUrl);

        String foodSafetyCertUrl = cloudinaryUpload.uploadFile(foodSafetyCert);
        if (foodSafetyCertUrl != null) shop.setFoodSafetyCertificate(foodSafetyCertUrl);
        if (shopDTO.getCitizenIDExpiredDate() != null && shopDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        // Mã hóa thông tin nhạy cảm
        shop.setAccountNumber(encryptSafe(shopDTO.getAccountNumber()));
        profile.setTaxCode(encryptSafe(shopDTO.getTaxCode()));
        profile.setCitizenIDNumber(encryptSafe(shopDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shopDTO.getCitizenIDExpiredDate());
        profileRepository.save(profile);
        shopRepository.save(shop);
    }

    @Override
    public ShopDTO getShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .map(this::decryptShopDTO)
                .orElseThrow(() -> new NotFoundException("Shop"));
    }


    private ShopDTO decryptShopDTO(Shop shop) {
        ShopDTO shopDTO = shopMapper.toDTO(shop);
        OwnerDTO ownerDTO = shopDTO.getOwner();
        if (ownerDTO != null) {
            BusinessProfileDTO profile = ownerDTO.getProfile();
            if (profile != null) {
                profile.setTax_code(decryptSafe(profile.getTax_code()));
                profile.setCitizenIDNumber(decryptSafe(profile.getCitizenIDNumber()));
                profile.setDrivingLicense(decryptSafe(profile.getDrivingLicense()));
            }
        }
        return shopDTO;
    }

    private String decryptSafe(String data) {
        return data != null ? encryptUtil.decrypt(data) : null;
    }

    private String encryptSafe(String data) {
        return data != null ? encryptUtil.encrypt(data) : null;
    }

}
