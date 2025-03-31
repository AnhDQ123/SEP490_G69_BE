package org.ffb_be.service.user;

import org.ffb_be.dto.auth.ProfileDto.ProfileDTO;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserResponseDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

import java.util.Map;

public interface UserService {
    void create(UserCreateDTO employeeCreateDTO) throws IOException;

    void activeUser(Long id);

    void update(UserUpdateDTO employeeCreateDTO, MultipartFile avatar) throws IOException;
    Page<UserResponseDTO> findAll(String search, Pageable pageable);
    ProfileDTO findById(Long id);
    void inactiveUser(Long id);
    Map<LocalDate, Long> getUserCountByDayAndStatus(LocalDate startDate, LocalDate endDate, Status status);
    Map<String, Long> getUserCountByMonthAndStatus(LocalDate startDate, LocalDate endDate, Status status);
    Map<Integer, Long> getUserCountByYearAndStatus(LocalDate startDate, LocalDate endDate,Status status);
    long countUsersAreShipper();
    long countUsersHaveShop();
    long countPendingShipper();
    long countAllUser();
    String hasShop(Long id);
}

