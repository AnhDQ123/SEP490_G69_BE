package org.ffb_be.service.shop;

import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ShopService {
    Page<ShopDTO> getShops(Pageable pageable);

    @Transactional
    ShopRegisterDTO registerShop(Long userId, ShopRegisterDTO shopDTO);

    ShopDTO getShopById(Long shopId);
}
