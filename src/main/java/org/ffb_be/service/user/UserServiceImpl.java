package org.ffb_be.service.user;

import jakarta.persistence.NonUniqueResultException;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.auth.ProfileDto.ProfileDTO;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserResponseDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.Role;
import org.ffb_be.entity.Shop;
import org.ffb_be.entity.User;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.RoleRepository;
import org.ffb_be.repository.ShopRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryUpload cloudinaryUpload;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final ShopRepository shopRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, CloudinaryUpload cloudinaryUpload, RoleRepository roleRepository, ProfileRepository profileRepository, ShopRepository shopRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryUpload = cloudinaryUpload;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
        this.shopRepository = shopRepository;
    }
    public Long create(UserCreateDTO userCreateDTO) throws IOException {
        User user = new User();
        BeanUtils.copyProperties(userCreateDTO, user);

        userRepository.findByEmail(userCreateDTO.getEmail()).ifPresent((x)->{
            throw new NonUniqueResultException("Email already exists!");
        });

        Role role = roleRepository.findById(1L).get();
        if(!role.getName().equals("operator")) {
            userRepository.findByPhone(userCreateDTO.getPhone()).ifPresent((x)->{
                throw new NonUniqueResultException("Phone already exists!");
            });
        }

        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setUsername(userCreateDTO.getPhone());
        user.setRole(role);
        Status status = Status.ACTIVE;
        user.setStatus(status);
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profileRepository.save(profile);
        return user.getId();
    }



    @Override
    public Page<UserResponseDTO> findAll(String search, Pageable pageable) {
        return userRepository.findByAllField(search, pageable).map(user -> {
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            BeanUtils.copyProperties(user, userResponseDTO);
            userResponseDTO.setId(user.getId());
            userResponseDTO.setStatus(user.getStatus().toString());
            if (user.getProfile() != null) {
                userResponseDTO.setAvatar(user.getProfile().getAvatar());
            } else {
                userResponseDTO.setAvatar(null);
            }
            userResponseDTO.setName(user.getProfile().getName());
            userResponseDTO.setRole(user.getRole().getName());
            userResponseDTO.setCreated_at(user.getCreatedAt().toString());
            return userResponseDTO;
        });
    }

    @Override
    public ProfileDTO findById(Long id) {
        Profile profile = profileRepository.findByUserId2(id).orElse(null);
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setStatus(userRepository.findById(id).get().getStatus());
        profileDTO.setEmail(userRepository.findById(id).get().getEmail());
        if(profile.getAvatar() != null) {
            profileDTO.setAvatar(profile.getAvatar());
        }
        profileDTO.setRole(userRepository.findById(id).get().getRole().getName());
        if(profile.getName() != null) {
            profileDTO.setName(profile.getName());
        }
        if(profile.getUser() != null) {
            profileDTO.setId(profile.getUser().getId());
        }
        if(profile.getAvatar() != null) {
            profileDTO.setAvatar(profile.getAvatar());
        }
        if(profile.getDob() != null) {
            profileDTO.setDob(profile.getDob());
        }
        if(profile.getCitizenIDExpiredDate() != null) {
            profileDTO.setCitizenIDExpiredDate(profile.getCitizenIDExpiredDate());
        }
        if(profile.getCitizenIDNumber() != null) {
            profileDTO.setCitizenIDNumber(profile.getCitizenIDNumber());
        }
        if(profile.getCitizenIDCardBack() != null) {
            profileDTO.setCitizenIDCardBack(profile.getCitizenIDCardBack());
        }
        if(profile.getCitizenIDCardFront() != null) {
            profileDTO.setCitizenIDCardFront(profile.getCitizenIDCardFront());
        }
        if(profile.getAddress() != null) {
            profileDTO.setAddress(profile.getAddress());
        }
        if(profile.getGender() != null) {
            profileDTO.setGender(profile.getGender());
        }
        if(profile.getTaxCode() != null) {
            profileDTO.setTax_code(profile.getTaxCode());
        }
        if(userRepository.findById(id).get().getPhone()!=null) {
            profileDTO.setPhone(userRepository.findById(id).get().getPhone());
        }
        if(userRepository.findById(id).get().getCreatedAt() != null) {
            profileDTO.setCreatedAt(userRepository.findById(id).get().getCreatedAt());
        }
        return profileDTO;
    }

    @Override
    public void inactiveUser(Long id) {
        User user=userRepository.findById(id).orElse(null);
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public void activeUser(Long id) {
        User user=userRepository.findById(id).orElse(null);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }


    public void update(UserUpdateDTO userUpdateDTO, MultipartFile avatar) throws IOException {
        User user = userRepository.findByPhone(userUpdateDTO.getPhone()).get();
        Profile p = profileRepository.findByUserId2(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        p.setAddress(userUpdateDTO.getAddress());
        p.setName(userUpdateDTO.getName());
        p.setGender(userUpdateDTO.getGender());
        p.setDob(userUpdateDTO.getDob());
        String url = cloudinaryUpload.uploadFile(avatar);
        p.setAvatar(url);
        profileRepository.save(p);
        user.setProfile(p);
        userRepository.save(user);
    }

    public List<CountByDateDTO> getUserCountByDayAndStatus(LocalDate startDate, LocalDate endDate, Status status) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = userRepository.countUsersByDayAndStatus(startDateTime, endDateTime, status);

        List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
            CountByDateDTO countByDateDTO = new CountByDateDTO();
            countByDateDTO.setDate((LocalDateTime) result[0]);
            countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
        }
        return countByDateDTOS;
    }

    public List<CountByMonthDTO> getUserCountByMonthAndStatus(LocalDate startDate, LocalDate endDate, Status status) {

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = userRepository.countUsersByMonthAndStatus(startDateTime, endDateTime, status);
        List<CountByMonthDTO> countByMonthDTOS = new ArrayList<>();

        // Chuyển đổi kết quả thành danh sách DTO
        for (Object[] result : results) {
            CountByMonthDTO countByMonthDTO = new CountByMonthDTO();

            // Get the month and year from the query result
            int month = (Integer) result[1]; // Month
            int year = (Integer) result[0]; // Year

            // Format the month as yyyy/MM
            String formattedMonth = String.format("%d/%02d", year, month); // Example: 2025/03
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

    public List<CountByYearDTO> getUserCountByYearAndStatus(LocalDate startDate, LocalDate endDate, Status status) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = userRepository.countUsersByYearAndStatus(startDateTime, endDateTime, status);

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

    public long countUsersAreShipper() {
        return userRepository.countUsersAreShipper();
    }
    public long countUsersHaveShop() {
        return shopRepository.countShop();
    }
    public long countPendingShipper() {
        return userRepository.countPendingShipper();
    }
    public long countAllUser() {
        return userRepository.countAllUser();
    }

    @Override
    public String hasShop(Long id) {
        User user=userRepository.findById(id).orElse(null);
        Shop shop=shopRepository.findByOwnerId(user.getId()).orElse(null);
        if (shop!=null&&shop.getIsActive().equals(Status.PENDING)) {
            String pending="pending";
            return  pending;
        }
        if (shop!=null&&shop.getIsActive().equals(Status.ACTIVE)) {
            String active="active";
            return active;
        }
        if (shop!=null&&shop.getIsActive().equals(Status.INACTIVE)) {
            String inactive="inactive";
            return inactive;
        }
        return null;
    }

    @Override
    public void add() {
        User user=new User();
        user.setUsername("0123456789");
        user.setPassword("123456");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
