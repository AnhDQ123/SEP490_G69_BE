package org.ffb_be.service.user;

import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserResponseDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    void create(UserCreateDTO employeeCreateDTO) throws IOException;
    void update(UserUpdateDTO employeeCreateDTO, MultipartFile avatar) throws IOException;
    Page<UserResponseDTO> findAll(String search, Pageable pageable);
}

