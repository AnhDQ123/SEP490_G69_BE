package org.ffb_be.service.shipper;

import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.dto.auth.userDto.ShipperRegisterDTO;
import org.ffb_be.utils.enums.ShipperStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ShipperService {
    void registerShipper(
            Long userId,
            ShipperRegisterDTO shipperRegisterDTO,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile drivingLicenseFront,
            MultipartFile drivingLicenseBack,
            MultipartFile judicialRecord
    ) throws IOException;

    void approveShipper(Long userId);

    void rejectShipper(Long userId, String reason);

    void shipperStatus(Long userId, ShipperStatus status, String reason);

    void updateShipperInfo(
            Long userId,
            ShipperRegisterDTO shipperRegisterDTO,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile drivingLicenseFront,
            MultipartFile drivingLicenseBack,
            MultipartFile judicialRecord
    ) throws IOException;

    Page<ShipperInfoDTO> getShippersByStatus(String status, String search, Pageable pageable);

    ShipperInfoDTO getShipperDetail(Long userId);
}
