package org.ffb_be.service.shop;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.entity.Shop;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ShopRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.mapping.ShopMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final UserRepository userRepository;
    private final EncryptUtil encryptUtil;

    @Override
    public Page<ShopDTO> getShops(Pageable pageable) {
        return shopRepository.findAll(pageable).map(this::decryptShopDTO);
    }

    @Transactional
    @Override
    public ShopRegisterDTO registerShop(Long userId, ShopRegisterDTO shopDTO) {
        if (shopRepository.existsByOwnerId(userId)) {
            throw new BadRequestException("User đã có cửa hàng.");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        Shop shop = shopMapper.toEntity(shopDTO);
        shop.setOwner(owner);
        shop.setIsActive(Status.PENDING);

        shop.setRegistrationCertificate(encryptSafe(shop.getRegistrationCertificate()));
        shop.setFoodSafetyCertificate(encryptSafe(shop.getFoodSafetyCertificate()));

        shopRepository.save(shop);

        return shopMapper.toRegisterDTO(shop);
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
                profile.setCitizenIDCardFront(decryptSafe(profile.getCitizenIDCardFront()));
                profile.setCitizenIDCardBack(decryptSafe(profile.getCitizenIDCardBack()));
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
