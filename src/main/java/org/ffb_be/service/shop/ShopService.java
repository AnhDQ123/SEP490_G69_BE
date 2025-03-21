package org.ffb_be.service.shop;

import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ShopService {

    Page<ShopDTO> getShops(String type, String status, String search, Pageable pageable);

    @Transactional
    void registerShop(
            Long userId,
            ShopRegisterDTO shopDTO,
            MultipartFile logo,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile registrationCert,
            MultipartFile foodSafetyCert,
            MultipartFile menu
    ) throws IOException;

    @Transactional
    void updateShop(
            Long shopId,
            ShopRegisterDTO shopDTO,
            MultipartFile logo,
            MultipartFile menu,
            MultipartFile registrationCert,
            MultipartFile foodSafetyCert,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack
    ) throws IOException;

    @Transactional
    void updateShopStatus(Long shopId, Status newStatus);

    boolean isShopOpen(Long shopId);

    ShopDTO getShopById(Long shopId);
}
