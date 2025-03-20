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
import java.time.LocalTime;

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
        if (shopDTO.getCitizenIDExpiredDate() != null && shopDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
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

    @Transactional
    @Override
    public void updateShop(
            Long shopId,
            ShopRegisterDTO shopDTO,
            MultipartFile logo,
            MultipartFile menu,
            MultipartFile registrationCert,
            MultipartFile foodSafetyCert,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack
    ) throws IOException {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop không tồn tại"));

        Profile profile = profileRepository.getByUserId(shop.getOwner().getId())
                .orElseThrow(() -> new NotFoundException("User"));

        boolean requireApproval = false;

        // Áp dụng mapper để cập nhật các trường không null
        shopMapper.updateShopFromDTO(shopDTO, shop);

        // Xử lý thay đổi logo, menu (không cần phê duyệt)
        if (logo != null && !logo.isEmpty()) {
            shop.setLogo(cloudinaryUpload.uploadFile(logo));
        }
        if (menu != null && !menu.isEmpty()) {
            shop.setMenu(cloudinaryUpload.uploadFile(menu));
        }

        // Xử lý các trường cần phê duyệt lại
        if (shopDTO.getAccountNumber() != null && !shopDTO.getAccountNumber().equals(shop.getAccountNumber())) {
            shop.setAccountNumber(encryptSafe(shopDTO.getAccountNumber()));
            requireApproval = true;
        }
        if (shopDTO.getBankCode() != null && !shopDTO.getBankCode().equals(shop.getBankCode())) {
            shop.setBankCode(shopDTO.getBankCode());
            requireApproval = true;
        }
        if (registrationCert != null && !registrationCert.isEmpty()) {
            shop.setRegistrationCertificate(cloudinaryUpload.uploadFile(registrationCert));
            requireApproval = true;
        }
        if (foodSafetyCert != null && !foodSafetyCert.isEmpty()) {
            shop.setFoodSafetyCertificate(cloudinaryUpload.uploadFile(foodSafetyCert));
            requireApproval = true;
        }
        if (shopDTO.getTaxCode() != null && !shopDTO.getTaxCode().equals(profile.getTaxCode())) {
            profile.setTaxCode(encryptSafe(shopDTO.getTaxCode()));
            requireApproval = true;
        }
        if (shopDTO.getCitizenIDNumber() != null && !shopDTO.getCitizenIDNumber().equals(profile.getCitizenIDNumber())) {
            profile.setCitizenIDNumber(encryptSafe(shopDTO.getCitizenIDNumber()));
            requireApproval = true;
        }
        if (shopDTO.getCitizenIDExpiredDate() != null && !shopDTO.getCitizenIDExpiredDate().equals(profile.getCitizenIDExpiredDate())) {
            profile.setCitizenIDExpiredDate(shopDTO.getCitizenIDExpiredDate());
            requireApproval = true;
        }

        // Xử lý citizen ID card
        if (citizenIDFront != null && !citizenIDFront.isEmpty()) {
            profile.setCitizenIDCardFront(cloudinaryUpload.uploadFile(citizenIDFront));
            requireApproval = true;
        }
        if (citizenIDBack != null && !citizenIDBack.isEmpty()) {
            profile.setCitizenIDCardBack(cloudinaryUpload.uploadFile(citizenIDBack));
            requireApproval = true;
        }

        // Nếu có thay đổi yêu cầu phê duyệt, cập nhật trạng thái shop
        if (requireApproval) {
            shop.setIsActive(Status.PENDING);
        }

        profileRepository.save(profile);
        shopRepository.save(shop);
    }

    @Transactional
    @Override
    public void updateShopStatus(Long shopId, Status newStatus) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop không tồn tại"));

        if (shop.getIsActive() == newStatus) {
            throw new BadRequestException("Shop đã ở trạng thái này rồi");
        }

        shop.setIsActive(newStatus);
        shopRepository.save(shop);
    }

    @Override
    public boolean isShopOpen(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        LocalTime now = LocalTime.now();
        LocalTime openTime = shop.getOpenTime();
        LocalTime closeTime = shop.getCloseTime();

        // Trường hợp mở 24/24
        if (openTime.equals(closeTime)) {
            return true;
        }

        // Trường hợp mở qua đêm
        boolean isOvernight = closeTime.isBefore(openTime);

        if (isOvernight) {
            return now.isAfter(openTime) || now.isBefore(closeTime);
        } else {
            return now.isAfter(openTime) && now.isBefore(closeTime);
        }
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
                profile.setTaxCode(decryptSafe(profile.getTaxCode()));
                profile.setCitizenIDNumber(decryptSafe(profile.getCitizenIDNumber()));
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
