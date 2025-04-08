package org.ffb_be.service.shop;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.auth.ProfileDto.BusinessProfileDTO;
import org.ffb_be.dto.auth.userDto.OwnerDTO;
import org.ffb_be.dto.banner.BannerDTO;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.entity.*;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.*;
import org.ffb_be.utils.EncryptUtil;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.ShopMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


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
    private final RoleRepository roleRepository;
    private final TypesRepository typesRepository;
    private final ImageRepository imageRepository;
    @Override
    public Page<ShopDTO> getShops(String type, String status, String search, Pageable pageable) {
        Specification<Shop> spec = Specification.where(null);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
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

        Page<Shop> shops = shopRepository.findAll(spec, sortedPageable);
        return shops.map(this::decryptShopDTO);
    }


    @Transactional
    @Override
    public void registerShop(
            Long userId,
            ShopRegisterDTO shopDTO,
            MultipartFile logo,
            MultipartFile background,
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
                .orElseThrow(() -> new NotFoundException("Profile"));
        if (owner.getRole().getName().equals("shipper")) {
            throw new BadRequestException("User không thể tạo cửa hàng vì là shipper");
        }


        Shop shop = shopMapper.toEntity(shopDTO);
        shop.setOwner(owner);
        shop.setIsActive(Status.PENDING);
        shop.setCreatedAt(LocalDateTime.now());
        String logoUrl = cloudinaryUpload.uploadFile(logo);
        if (logoUrl != null) shop.setLogo(logoUrl);

        String backgroundUrl = cloudinaryUpload.uploadFile(background);
        if (backgroundUrl != null) shop.setBackgroundImage(backgroundUrl);

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
        shop.setBankCode(shopDTO.getBankCode());
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
            MultipartFile background,
            MultipartFile menu,
            MultipartFile registrationCert,
            MultipartFile foodSafetyCert,
            MultipartFile citizenIDFront,
            MultipartFile citizenIDBack
    ) throws IOException {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop"));

        Profile profile = profileRepository.getByUserId(shop.getOwner().getId())
                .orElseThrow(() -> new NotFoundException("User"));

        boolean requireApproval = false;

        // Áp dụng mapper để cập nhật các trường không null
        shopMapper.updateShopFromDTO(shopDTO, shop);

        // Xử lý thay đổi logo, menu (không cần phê duyệt)
        if (logo != null && !logo.isEmpty()) {
            shop.setLogo(cloudinaryUpload.uploadFile(logo));
        }

        if (background != null && !background.isEmpty()) {
            shop.setBackgroundImage(cloudinaryUpload.uploadFile(background));
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
            shop.setReason(null);
            shop.setIsActive(Status.PENDING);
        }
        shop.setIsActive(Status.ACTIVE);
        profileRepository.save(profile);
        shopRepository.save(shop);
    }

    @Override
    public void approveShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop"));
        if(shop.getIsActive() == Status.ACTIVE){
            throw new BadRequestException("Shop đã được duyệt rồi");
        }
        if(shop.getIsActive() != Status.PENDING){
            throw new BadRequestException("Shop không ở trạng thái chờ duyệt");
        }
        User user = userRepository.findById(shop.getOwner().getId()).orElseThrow(() -> new NotFoundException("User"));
        Role role=roleRepository.getByName("shopkeeper").orElseThrow(() -> new NotFoundException("Role"));
        user.setRole(role);
        userRepository.save(user);
        shop.setIsActive(Status.ACTIVE);
        shopRepository.save(shop);
    }

    @Override
    public void rejectShop(Long id, String reason) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop"));
        if(shop.getIsActive() == Status.REJECTED){
            throw new BadRequestException("Shop đã bị từ chối rồi");
        }
        if(shop.getIsActive() != Status.PENDING){
            throw new BadRequestException("Shop không ở trạng thái chờ duyệt");
        }
        shop.setIsActive(Status.REJECTED);
        shop.setReason(reason);
        shopRepository.save(shop);
    }

    @Transactional
    @Override
    public void updateShopStatus(Long shopId, Status newStatus, String reason) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop"));

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

    @Override
    public ShopDTO getShopByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User"));
        Shop shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new NotFoundException("User"));
        if(shop.getIsActive() == Status.INACTIVE){
            throw new BadRequestException("Cửa hàng đã bị vô hiệu hóa");
        }
        if(shop.getIsActive() == Status.PENDING) {
            throw new BadRequestException("Cửa hàng đang chờ duyệt");
        }
        return shopRepository.findByOwnerId(userId)
                .map(this::decryptShopDTO)
                .orElseThrow(() -> new NotFoundException("Shop"));
    }

    @Override
    public ResponseEntity<?> getShopByOwnerId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User không tồn tại");
        }

        Shop shop = shopRepository.findByOwnerId(user.getId()).orElse(null);
        if (shop == null) {
            return ResponseEntity.badRequest().body("Cửa hàng không tồn tại");
        }

        if (shop.getIsActive() == Status.INACTIVE) {
            return ResponseEntity.ok().body("Cửa hàng đã bị vô hiệu hóa");
        }
        if (shop.getIsActive() == Status.PENDING) {
            return ResponseEntity.ok().body("Cửa hàng đang chờ duyệt");
        }

        return ResponseEntity.ok(decryptShopDTO(shop));
    }



    private ShopDTO decryptShopDTO(Shop shop) {
        ShopDTO shopDTO = shopMapper.toDTO(shop);
        OwnerDTO ownerDTO = shopDTO.getOwner();
        if (ownerDTO != null) {
            BusinessProfileDTO profile = ownerDTO.getProfile();
            if (profile != null) {
                profile.setTaxCode((profile.getTaxCode()));
                profile.setCitizenIDNumber((profile.getCitizenIDNumber()));
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
    public List<CountByMonthDTO> getShopCountByMonth(Status status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = shopRepository.countShopsByStatusAndMonth(status, startDateTime, endDateTime);

        List<CountByMonthDTO> countByMonthDTOS = new ArrayList<>();

        // Chuyển đổi kết quả thành danh sách DTO
        for (Object[] result : results) {
            CountByMonthDTO countByMonthDTO = new CountByMonthDTO();

            // Get the month and year from the query result
            int month = (Integer) result[0]; // Month
            int year = (Integer) result[1]; // Year

            // Format the month as yyyy/MM
            String formattedMonth = String.format("%d/%02d", month, year); // Example: 2025/03
            countByMonthDTO.setMonth(formattedMonth);

            // Get the order count (it could be either Long or Integer)
            if (result[2] instanceof Long) {
                countByMonthDTO.setCount((Long) result[2]);
            } else {
                countByMonthDTO.setCount(((Integer) result[2]).longValue());
            }

            // Add the DTO to the result list
            countByMonthDTOS.add(countByMonthDTO);
        }
        return countByMonthDTOS;
    }

    // Đếm số lượng shop theo trạng thái và năm
    public List<CountByYearDTO> getShopCountByYear(Status status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = shopRepository.countShopsByStatusAndYear(status, startDateTime, endDateTime);

        List<CountByYearDTO> countByYearDTOS = new ArrayList<>();

        // Duyệt qua các kết quả trả về từ truy vấn
        for (Object[] result : results) {
            CountByYearDTO countByYearDTO = new CountByYearDTO();

            // Lấy năm từ kết quả truy vấn (result[0] chứa năm)
            int year = (Integer) result[0];
            countByYearDTO.setYear(year);

            // Lấy số lượng đơn hàng từ kết quả truy vấn (result[1] chứa số lượng đơn hàng)
            Long count = (Long) result[1];
            countByYearDTO.setCount(count);

            // Thêm đối tượng vào danh sách kết quả
            countByYearDTOS.add(countByYearDTO);
        }

        // Trả về danh sách kết quả
        return countByYearDTOS;
    }

    public List<CountByDateDTO> getShopCountByDayAndStatus(Status status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = shopRepository.countShopsByStatusAndDay(status, startDateTime, endDateTime);
        List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
            CountByDateDTO countByDateDTO = new CountByDateDTO();
            countByDateDTO.setDate((LocalDateTime) result[0]);
            countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
        }
        return countByDateDTOS;
    }
    public long countPendingShop() {
        return shopRepository.countPendingShop();
    }

    @Override
    public void rateShop(Long shopId,Double newRate) {
        Shop shop = shopRepository.findById(shopId).get();
        shop.setRate((shop.getRate()*shop.getViewCount()+newRate)/(shop.getViewCount()+1));
        shop.setViewCount(shop.getViewCount() + 1);
        shopRepository.save(shop);
    }

    @Override
    public void uploadBanner(Long shopId, MultipartFile banner) throws IOException {
        Shop shop=shopRepository.findById(shopId).get();
        Image image=new Image();
        String url = cloudinaryUpload.uploadFile(banner);
        image.setUrl(url);
        image.setRelatedId(shop.getId());
        image.setOwnerId(shop.getOwner().getId());
        image.setType(typesRepository.findById(8L).get());
        imageRepository.save(image);
    }

    @Override
    public List<BannerDTO> viewBannerByShop(Long shopId) {
        List<Image> images=imageRepository.findAllByRelatedIdAndType_Id(shopId,8L);
        List<BannerDTO> bannerDTOS=new ArrayList<>();
        for (Image image : images) {
            BannerDTO bannerDTO=new BannerDTO();
            bannerDTO.setUrl(image.getUrl());
            bannerDTO.setBannerId(image.getId());
            bannerDTO.setShopId(shopId);
            bannerDTO.setStatus(image.getStatus().toString());
            bannerDTOS.add(bannerDTO);
        }
        return bannerDTOS;
    }

    @Override
    public List<BannerDTO> homePageBanner() {
        List<Image> images=imageRepository.findAllByStatusAndType_Id(Status.ACTIVE,8L);
        List<BannerDTO> bannerDTOS=new ArrayList<>();
        for (Image image : images) {
            BannerDTO bannerDTO=new BannerDTO();
            bannerDTO.setUrl(image.getUrl());
            bannerDTO.setBannerId(image.getId());
            bannerDTO.setShopId(image.getRelatedId());
            bannerDTO.setStatus(image.getStatus().toString());
            bannerDTOS.add(bannerDTO);
        }
        return bannerDTOS;
    }


}
