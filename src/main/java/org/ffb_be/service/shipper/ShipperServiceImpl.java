package org.ffb_be.service.shipper;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.dto.auth.userDto.ShipperRegisterDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.RoleRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.ShipperMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final EncryptUtil encryptUtil;
    private final ShipperMapper shipperMapper;

    @Override
    public void registerShipper(
            Long userId,
            ShipperRegisterDTO shipperRegisterDTO,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile drivingLicenseFront,
            MultipartFile drivingLicenseBack,
            MultipartFile judicialRecord
    ) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        if(user.getRole().getName().equals("shipper")) {
            throw new BadRequestException("User đã đăng ký làm shipper.");
        }
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User"));

        String citizenIDFrontUrl = cloudinaryUpload.uploadFile(citizenIDFront);
        if (citizenIDFrontUrl != null) profile.setCitizenIDCardFront(citizenIDFrontUrl);

        String citizenIDBackUrl = cloudinaryUpload.uploadFile(citizenIDBack);
        if (citizenIDBackUrl != null) profile.setCitizenIDCardBack(citizenIDBackUrl);

        String drivingLicenseFrontUrl = cloudinaryUpload.uploadFile(drivingLicenseFront);
        if (drivingLicenseFrontUrl != null) profile.setDrivingLicenseFront(drivingLicenseFrontUrl);

        String drivingLicenseBackUrl = cloudinaryUpload.uploadFile(drivingLicenseBack);
        if (drivingLicenseBackUrl != null) profile.setDrivingLicenseBack(drivingLicenseBackUrl);

        String judicialRecordUrl = cloudinaryUpload.uploadFile(judicialRecord);
        if (judicialRecordUrl != null) profile.setJudicialRecord(judicialRecordUrl);

        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null && shipperRegisterDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null && shipperRegisterDTO.getDrivingLicenseExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Bằng lái xe đã hết hạn!");
        }

        profile.setCitizenIDNumber(encryptSafe(shipperRegisterDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
        profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
        user.setRole(roleRepository.getByName("pending_shipper"));
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public void approveShipper(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(user.getRole().getName(), "pending_shipper")) {
            throw new BadRequestException("User không ở trạng thái chờ duyệt.");
        }

        user.setRole(roleRepository.getByName("shipper"));
        userRepository.save(user);
    }

    @Override
    public void rejectShipper(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));

        if (!Objects.equals(user.getRole().getName(), "pending_shipper")) {
            throw new BadRequestException("User không ở trạng thái chờ duyệt.");
        }
        user.setRole(roleRepository.getByName("user"));
        userRepository.save(user);
    }

    @Override
    public void updateShipperInfo(
            Long userId,
            ShipperRegisterDTO shipperRegisterDTO,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack,
            MultipartFile drivingLicenseFront,
            MultipartFile drivingLicenseBack,
            MultipartFile judicialRecord
    ) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile"));

        if (!Objects.equals(user.getRole().getName(), "shipper")) {
            throw new BadRequestException("User không phải là shipper.");
        }

        if (citizenIDFront != null && !citizenIDFront.isEmpty()) {
            profile.setCitizenIDCardFront(cloudinaryUpload.uploadFile(citizenIDFront));
        }

        if (citizenIDBack != null && !citizenIDBack.isEmpty()) {
            profile.setCitizenIDCardBack(cloudinaryUpload.uploadFile(citizenIDBack));
        }

        if (drivingLicenseFront != null && !drivingLicenseFront.isEmpty()) {
            profile.setDrivingLicenseFront(cloudinaryUpload.uploadFile(drivingLicenseFront));
        }

        if (drivingLicenseBack != null && !drivingLicenseBack.isEmpty()) {
            profile.setDrivingLicenseBack(cloudinaryUpload.uploadFile(drivingLicenseBack));
        }

        if (judicialRecord != null && !judicialRecord.isEmpty()) {
            profile.setJudicialRecord(cloudinaryUpload.uploadFile(judicialRecord));
        }

        if (shipperRegisterDTO.getCitizenIDNumber() != null) {
            profile.setCitizenIDNumber(encryptSafe(shipperRegisterDTO.getCitizenIDNumber()));
        }

        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null) {
            profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null) {
            profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
        }

        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null && shipperRegisterDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null && shipperRegisterDTO.getDrivingLicenseExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Bằng lái xe đã hết hạn!");
        }

        profile.setCitizenIDNumber(encryptSafe(shipperRegisterDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
        profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
        user.setRole(roleRepository.getByName("pending_shipper"));
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public Page<ShipperInfoDTO> getShippersByStatus(String status, Pageable pageable) {
        Page<User> shippers;
        if ("active".equalsIgnoreCase(status)) {
            shippers = userRepository.findByRole_Name("shipper", pageable);
        } else if ("pending".equalsIgnoreCase(status)) {
            shippers = userRepository.findByRole_Name("pending_shipper", pageable);
        } else {
            throw new BadRequestException("Trạng thái không hợp lệ!");
        }

        return shippers.map(this::decryptDTO);
    }

    @Override
    public ShipperInfoDTO getShipperDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        return decryptDTO(user);
    }

    private ShipperInfoDTO decryptDTO(User user) {
        ShipperInfoDTO shipperInfoDTO = shipperMapper.toDTO(user);
        shipperInfoDTO.setCitizenIDNumber((shipperInfoDTO.getCitizenIDNumber()));
        return shipperInfoDTO;
    }

    private String decryptSafe(String data) {
        return data != null ? encryptUtil.decrypt(data) : null;
    }

    private String encryptSafe(String data) {
        return data != null ? encryptUtil.encrypt(data) : null;
    }
}
