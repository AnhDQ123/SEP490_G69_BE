package org.ffb_be.service.shipper;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.ShipperInfoDTO;
import org.ffb_be.dto.auth.userDto.ShipperRegisterDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.Role;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.RoleRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.DeliveryStatus;
import org.ffb_be.utils.enums.ShipperStatus;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.ShipperMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        if(user.getShipperStatus() == ShipperStatus.PENDING) {
            throw new BadRequestException("Shipper đang chở được duyệt không thể gửi đăng ký.");
        }
        if(user.getRole().getName().equals("shopkeeper")) {
            throw new BadRequestException("User đã đăng ký làm chủ cửa hàng không thể đăng ký làm shipper.");
        }
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        profile.setName(shipperRegisterDTO.getName());
        profile.setGender(shipperRegisterDTO.getGender());
        profile.setDob(shipperRegisterDTO.getDob());
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
        user.setShipperStatus(ShipperStatus.PENDING);
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public void approveShipper(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User"));

        if (!Objects.equals(user.getRole().getName(), "shipper")) {
            throw new BadRequestException("User đã là shipper.");
        }
        if (user.getShipperStatus() != ShipperStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể duyệt shipper đang ở trạng thái chờ.");
        }
        Role role = roleRepository.getByName("shipper")
                .orElseThrow(() ->new NotFoundException("Role"));
        user.setRole(role);
        user.setShipperStatus(ShipperStatus.ACTIVE);
        user.setDeliveryStatus(DeliveryStatus.AVAILABLE);
        userRepository.save(user);
    }

    @Override
    public void rejectShipper(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));

        if (user.getShipperStatus() != ShipperStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối shipper đang ở trạng thái chờ.");
        }
        Role role = roleRepository.getByName("user")
                .orElseThrow(() ->new NotFoundException("Role"));
        user.setRole(role);
        user.setShipperStatus(null);
        user.setRejectReason(reason);
        userRepository.save(user);
    }

    @Override
    public void shipperStatus(Long userId, ShipperStatus status, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        if(status == ShipperStatus.INACTIVE) {
            user.setRejectReason(reason);
        }
        if(status == ShipperStatus.ACTIVE) {
            user.setRejectReason(null);
        }
        user.setShipperStatus(status);
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

        profile.setCitizenIDNumber((shipperRegisterDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
        profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
        user.setRejectReason(null);
        user.setShipperStatus(ShipperStatus.PENDING);
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public Page<ShipperInfoDTO> getShippersByStatus(String status, String search, Pageable pageable) {
        Specification<User> spec = Specification.where((root, query, cb) -> {
            Join<User, Role> roleJoin = root.join("role", JoinType.INNER);
            return cb.equal(roleJoin.get("name"), "shipper");
        });

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("shipperStatus"), status));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<User, Profile> profileJoin = root.join("profile", JoinType.LEFT);
                Predicate phonePredicate = cb.like(root.get("phone"), "%" + search + "%");
                Predicate namePredicate = cb.like(cb.lower(profileJoin.get("name")), "%" + search.toLowerCase() + "%");
                return cb.or(phonePredicate, namePredicate);
            });
        }

        Page<User> shippers = userRepository.findAll(spec, pageable);
        return shippers.map(this::decryptDTO);
    }

    @Override
    public ShipperInfoDTO getShipperDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        return decryptDTO(user);
    }

    @Override
    public void changeIsBusy(Long userId) {
        User user=userRepository.findById(userId).get();
        user.setDeliveryStatus(DeliveryStatus.BUSY);
        userRepository.save(user);
    }

    private ShipperInfoDTO decryptDTO(User user) {
        ShipperInfoDTO shipperInfoDTO = shipperMapper.toDTO(user);
        shipperInfoDTO.setCitizenIDNumber((shipperInfoDTO.getCitizenIDNumber()));
        shipperInfoDTO.setId(user.getId());
        return shipperInfoDTO;
    }

    private String decryptSafe(String data) {
        return data != null ? encryptUtil.decrypt(data) : null;
    }

    private String encryptSafe(String data) {
        return data != null ? encryptUtil.encrypt(data) : null;
    }
}
