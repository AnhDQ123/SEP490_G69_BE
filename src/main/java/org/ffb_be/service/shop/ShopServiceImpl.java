package org.ffb_be.service.shop;

import lombok.AllArgsConstructor;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
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
import org.ffb_be.service.map.MapService;
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
    private final ProductRepository productRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final EncryptUtil encryptUtil;
    private final RoleRepository roleRepository;
    private final TypesRepository typesRepository;
    private final ImageRepository imageRepository;
    private final MapService mapService;
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
        if ("Shipper".equals(owner.getRole().getName())) {
            throw new BadRequestException("User không thể tạo cửa hàng vì là shipper");
        }

        double[] latLng = mapService.getLatLngFromAddress(shopDTO.getAddress());

        Shop shop = shopMapper.toEntity(shopDTO);
        shop.setOwner(owner);
        shop.setLatitude(latLng[0]);
        shop.setLongitude(latLng[1]);
        shop.setIsActive(Status.PENDING);
        shop.setCreatedAt(LocalDateTime.now());

        shop.setLogo(cloudinaryUpload.safeUpload(logo, "Logo"));
        shop.setBackgroundImage(cloudinaryUpload.safeUpload(background, "Background"));
        profile.setCitizenIDCardFront(cloudinaryUpload.safeUpload(citizenIDFront, "CMND mặt trước"));
        profile.setCitizenIDCardBack(cloudinaryUpload.safeUpload(citizenIDBack, "CMND mặt sau"));
        shop.setRegistrationCertificate(cloudinaryUpload.safeUpload(registrationCert, "Giấy đăng ký kinh doanh"));
        shop.setMenu(cloudinaryUpload.safeUpload(menu, "Menu"));
        shop.setFoodSafetyCertificate(cloudinaryUpload.safeUpload(foodSafetyCert, "Giấy chứng nhận ATTP"));

        if (shopDTO.getCitizenIDExpiredDate() != null && shopDTO.getCitizenIDExpiredDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("Giấy tờ tùy thân đã hết hạn!");
        }

        Role role = roleRepository.getByName("Shopkeeper")
                .orElseThrow(() -> new NotFoundException("Role"));
        owner.setRole(role);

        shop.setAccountNumber(encryptSafe(shopDTO.getAccountNumber()));
        shop.setBankCode(shopDTO.getBankCode());
        profile.setTaxCode(encryptSafe(shopDTO.getTaxCode()));
        profile.setCitizenIDNumber(encryptSafe(shopDTO.getCitizenIDNumber()));
        profile.setCitizenIDExpiredDate(shopDTO.getCitizenIDExpiredDate());

        userRepository.save(owner);
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

        shopMapper.updateShopFromDTO(shopDTO, shop);

        double[] latLng = mapService.getLatLngFromAddress(shopDTO.getAddress());
        shop.setLatitude(latLng[0]);
        shop.setLongitude(latLng[1]);

        if (logo != null && !logo.isEmpty()) {
            shop.setLogo(cloudinaryUpload.safeUpload(logo, "Logo"));
        }
        if (background != null && !background.isEmpty()) {
            shop.setBackgroundImage(cloudinaryUpload.safeUpload(background, "Background"));
        }
        if (menu != null && !menu.isEmpty()) {
            shop.setMenu(cloudinaryUpload.safeUpload(menu, "Menu"));
        }

        if (shopDTO.getAccountNumber() != null && !encryptSafe(shopDTO.getAccountNumber()).equals(shop.getAccountNumber())) {
            shop.setAccountNumber(encryptSafe(shopDTO.getAccountNumber()));
            requireApproval = true;
        }
        if (shopDTO.getBankCode() != null && !shopDTO.getBankCode().equals(shop.getBankCode())) {
            shop.setBankCode(shopDTO.getBankCode());
            requireApproval = true;
        }
        if (registrationCert != null && !registrationCert.isEmpty()) {
            shop.setRegistrationCertificate(cloudinaryUpload.safeUpload(registrationCert, "Giấy đăng ký kinh doanh"));
            requireApproval = true;
        }
        if (foodSafetyCert != null && !foodSafetyCert.isEmpty()) {
            shop.setFoodSafetyCertificate(cloudinaryUpload.safeUpload(foodSafetyCert, "Giấy chứng nhận ATTP"));
            requireApproval = true;
        }

        if (shopDTO.getTaxCode() != null && !encryptSafe(shopDTO.getTaxCode()).equals(profile.getTaxCode())) {
            profile.setTaxCode(encryptSafe(shopDTO.getTaxCode()));
            requireApproval = true;
        }
        if (shopDTO.getCitizenIDNumber() != null && !encryptSafe(shopDTO.getCitizenIDNumber()).equals(profile.getCitizenIDNumber())) {
            profile.setCitizenIDNumber(encryptSafe(shopDTO.getCitizenIDNumber()));
            requireApproval = true;
        }
        if (shopDTO.getCitizenIDExpiredDate() != null && !shopDTO.getCitizenIDExpiredDate().equals(profile.getCitizenIDExpiredDate())) {
            profile.setCitizenIDExpiredDate(shopDTO.getCitizenIDExpiredDate());
            requireApproval = true;
        }

        if (citizenIDFront != null && !citizenIDFront.isEmpty()) {
            profile.setCitizenIDCardFront(cloudinaryUpload.safeUpload(citizenIDFront, "CMND mặt trước"));
            requireApproval = true;
        }
        if (citizenIDBack != null && !citizenIDBack.isEmpty()) {
            profile.setCitizenIDCardBack(cloudinaryUpload.safeUpload(citizenIDBack, "CMND mặt sau"));
            requireApproval = true;
        }

        if (requireApproval) {
            shop.setReason(null);
            shop.setIsActive(Status.PENDING);
            List<Product> products = productRepository.getByShop_Id(shop.getId());
            if (products != null) {
                for (Product product : products) {
                    product.setStatus(Status.PENDING);
                    productRepository.save(product);
                }
            }
        }

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
        Role role=roleRepository.getByName("Shopkeeper").orElseThrow(() -> new NotFoundException("Role"));
        user.setRole(role);
        userRepository.save(user);
        shop.setIsActive(Status.ACTIVE);
        shop.setIsOpening(false);
        shop.setIsShipping(false);
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
        // Fetch the shop by its ID
        if(newStatus != Status.ACTIVE && newStatus != Status.INACTIVE){
            throw new BadRequestException("Không thể thay đổi trạng thái này");
        }
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        // Check if the shop is already in the desired status
        if (shop.getIsActive() == newStatus) {
            // If the status is already the same, throw an exception
            throw new BadRequestException("Shop đã ở trạng thái này rồi");
        }

        // Update the shop's status
        shop.setIsActive(newStatus);
        // Save the updated shop object
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
        image.setStatus(Status.PENDING);
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

    @Override
    public Long shopId(Long productId) {
        Shop shop=shopRepository.findById(productId).get();
        return shop.getId();
    }

    @Override
    public void changeIsShipping(Long shopId) {
        Shop shop=shopRepository.findById(shopId).get();
        shop.setIsShipping(!shop.getIsShipping());
        shopRepository.save(shop);
    }

    @Override
    public void changeIsOpen(Long shopId) {
        Shop shop=shopRepository.findById(shopId).get();
        shop.setIsOpening(!shop.getIsOpening());
        shopRepository.save(shop);
    }

    @Override
    public double shopChangeRate() {
        LocalDateTime startOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double c=shopRepository.countActiveShopByMonth(startOfLastMonth, endOfLastMonth);
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double d=shopRepository.countActiveShopByMonth(startOfThisMonth, endOfThisMonth);
        return (d-c)/c*100;
    }


}
