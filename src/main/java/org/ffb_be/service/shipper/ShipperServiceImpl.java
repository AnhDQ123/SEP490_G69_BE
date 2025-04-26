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
import org.ffb_be.repository.OrderRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final OrderRepository orderRepository;

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
        if (citizenIDFront == null || citizenIDBack == null || drivingLicenseFront == null || drivingLicenseBack == null || judicialRecord == null) {
            throw new BadRequestException("Tất cả các giấy tờ phải được tải lên.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        if(user.getRole().getName().equals("Shipper")) {
            throw new BadRequestException("User đã đăng ký làm shipper.");
        }else
        if(user.getShipperStatus() == ShipperStatus.PENDING) {
            throw new BadRequestException("Shipper đang chở được duyệt không thể gửi đăng ký.");
        }
        if(!user.getRole().getName().equals("User")) {
            throw new BadRequestException("Người dùng không đạt điều kiện để đăng ký làm shipper.");
        }
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("profile"));
        profile.setName(shipperRegisterDTO.getName());
        profile.setGender(shipperRegisterDTO.getGender());
        profile.setDob(shipperRegisterDTO.getDob());

        // Upload các giấy tờ lên Cloudinary và xử lý lỗi nếu có
        String citizenIDFrontUrl = uploadFile(citizenIDFront, "Giấy chứng minh nhân dân (mặt trước)");
        String citizenIDBackUrl = uploadFile(citizenIDBack, "Giấy chứng minh nhân dân (mặt sau)");
        String drivingLicenseFrontUrl = uploadFile(drivingLicenseFront, "Bằng lái xe (mặt trước)");
        String drivingLicenseBackUrl = uploadFile(drivingLicenseBack, "Bằng lái xe (mặt sau)");
        String judicialRecordUrl = uploadFile(judicialRecord, "Giấy xác nhận lý lịch tư pháp");

        // Kiểm tra ngày hết hạn giấy tờ
        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null &&
                shipperRegisterDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null &&
                shipperRegisterDTO.getDrivingLicenseExpiredDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Bằng lái xe đã hết hạn!");
        }

        // Cập nhật các thông tin vào profile
        profile.setCitizenIDCardFront(citizenIDFrontUrl);
        profile.setCitizenIDCardBack(citizenIDBackUrl);
        profile.setDrivingLicenseFront(drivingLicenseFrontUrl);
        profile.setDrivingLicenseBack(drivingLicenseBackUrl);
        profile.setJudicialRecord(judicialRecordUrl);

        profile.setCitizenIDNumber(encryptSafe(shipperRegisterDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
        profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
        profile.setAccountNumber(encryptSafe(shipperRegisterDTO.getAccountNumber()));
        profile.setBankCode(shipperRegisterDTO.getBankCode());

        // Đặt trạng thái shipper là PENDING
        user.setShipperStatus(ShipperStatus.PENDING);
        Role role=roleRepository.findById(4L).get();
        user.setRole(role);
        // Lưu các thay đổi vào database
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public void approveShipper(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User"));

        if (!Objects.equals(user.getRole().getName(), "Shipper")) {
            throw new BadRequestException("User đã là shipper.");
        }

        if (user.getShipperStatus() != ShipperStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể duyệt shipper đang ở trạng thái chờ.");
        }
        Role role = roleRepository.getByName("Shipper")
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
        Role role = roleRepository.getByName("User")
                .orElseThrow(() ->new NotFoundException("Role"));
        user.setRole(role);
        user.setShipperStatus(null);
        user.setRejectReason(reason);
        userRepository.save(user);
    }

    @Override
    public void shipperStatus(Long userId, ShipperStatus status, String reason) {
        // Tìm người dùng từ userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));

        // Kiểm tra nếu trạng thái hiện tại là ACTIVE và cố gắng chuyển sang ACTIVE
        if (user.getShipperStatus() == ShipperStatus.ACTIVE && status == ShipperStatus.ACTIVE) {
            throw new IllegalStateException("Update status failed");
        }

        // Kiểm tra nếu trạng thái hiện tại là INACTIVE và cố gắng chuyển sang INACTIVE
        if (user.getShipperStatus() == ShipperStatus.INACTIVE && status == ShipperStatus.INACTIVE) {
            throw new IllegalStateException("Update status failed");
        }

        // Nếu trạng thái là INACTIVE, thêm lý do từ người dùng
        if (status == ShipperStatus.INACTIVE) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reason is required when setting status to INACTIVE");
            }
            user.setRejectReason(reason);
        }

        // Nếu trạng thái là ACTIVE, xóa lý do từ người dùng
        if (status == ShipperStatus.ACTIVE) {
            user.setRejectReason(null);
        }

        // Cập nhật trạng thái mới cho người dùng
        user.setShipperStatus(status);

        // Lưu người dùng vào cơ sở dữ liệu
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
                .orElseThrow(() -> new NotFoundException("User không tồn tại."));
        Profile profile = profileRepository.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile không tồn tại."));

        // Kiểm tra nếu user là shipper
        if (!"Shipper".equals(user.getRole().getName())) {
            throw new BadRequestException("User không phải là shipper.");
        }

        // Biến kiểm tra xem có yêu cầu phê duyệt không
        boolean requireApproval = false;

        // Cập nhật ảnh nếu có và kiểm tra có cần phê duyệt không
        if (citizenIDFront != null && !citizenIDFront.isEmpty()) {
            profile.setCitizenIDCardFront(uploadFile(citizenIDFront, "Giấy chứng minh nhân dân (mặt trước)"));
            requireApproval = true;
        }

        if (citizenIDBack != null && !citizenIDBack.isEmpty()) {
            profile.setCitizenIDCardBack(uploadFile(citizenIDBack, "Giấy chứng minh nhân dân (mặt trước)"));
            requireApproval = true;
        }

        if (drivingLicenseFront != null && !drivingLicenseFront.isEmpty()) {
            profile.setDrivingLicenseFront(uploadFile(drivingLicenseFront, "Bằng lái xe (mặt trước)"));
            requireApproval = true;
        }

        if (drivingLicenseBack != null && !drivingLicenseBack.isEmpty()) {
            profile.setDrivingLicenseBack(uploadFile(drivingLicenseBack, "Bằng lái xe (mặt sau)"));
            requireApproval = true;
        }

        if (judicialRecord != null && !judicialRecord.isEmpty()) {
            profile.setJudicialRecord(uploadFile(judicialRecord, "Giấy xác nhận lý lịch tư pháp"));
            requireApproval = true;
        }

        // Cập nhật thông tin khác nếu có
        if (shipperRegisterDTO.getCitizenIDNumber() != null) {
            profile.setCitizenIDNumber(encryptSafe(shipperRegisterDTO.getCitizenIDNumber()));
            requireApproval = true;
        }

        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null) {
            profile.setCitizenIDExpiredDate(shipperRegisterDTO.getCitizenIDExpiredDate());
            requireApproval = true;
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null) {
            profile.setDrivingLicenseExpiredDate(shipperRegisterDTO.getDrivingLicenseExpiredDate());
            requireApproval = true;
        }

        if (shipperRegisterDTO.getAccountNumber() != null && !shipperRegisterDTO.getAccountNumber().isEmpty()) {
            profile.setAccountNumber(encryptSafe(shipperRegisterDTO.getAccountNumber()));
            requireApproval = true;
        }

        if (shipperRegisterDTO.getBankCode() != null && !shipperRegisterDTO.getBankCode().isEmpty()) {
            profile.setBankCode(shipperRegisterDTO.getBankCode());
            requireApproval = true;
        }

        // Kiểm tra ngày hết hạn
        if (shipperRegisterDTO.getCitizenIDExpiredDate() != null && shipperRegisterDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        if (shipperRegisterDTO.getDrivingLicenseExpiredDate() != null && shipperRegisterDTO.getDrivingLicenseExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Bằng lái xe đã hết hạn!");
        }

        // Nếu có thay đổi, yêu cầu phê duyệt
        if (requireApproval) {
            user.setRejectReason(null);
            user.setShipperStatus(ShipperStatus.PENDING); // Trạng thái chờ duyệt
        }

        // Lưu lại profile và user
        profileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public Page<ShipperInfoDTO> getShippersByStatus(String status, String search, Pageable pageable) {
        Specification<User> spec = Specification.where((root, query, cb) -> {
            Join<User, Role> roleJoin = root.join("role", JoinType.INNER);
            return cb.equal(roleJoin.get("name"), "Shipper");
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

    @Override
    public void changeIsAvailable(Long userId) {
        User user=userRepository.findById(userId).get();
        user.setDeliveryStatus(DeliveryStatus.AVAILABLE);
        userRepository.save(user);
    }

    @Override
    public BigDecimal shipperBalance(Long userId) {
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        long a=orderRepository.countOrdersByShipper_Id(userId,startOfThisMonth,endOfThisMonth);
        BigDecimal b=BigDecimal.valueOf(10000);
        return b.multiply(BigDecimal.valueOf(a));
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

    private String uploadFile(MultipartFile file, String fileType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(fileType + " không được phép để trống.");
        }
        String fileUrl = cloudinaryUpload.uploadFile(file);
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new BadRequestException(fileType + " tải lên không thành công.");
        }
        return fileUrl;
    }
}
