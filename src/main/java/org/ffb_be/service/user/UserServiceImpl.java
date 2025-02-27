package org.ffb_be.service.user;

import jakarta.persistence.NonUniqueResultException;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserResponseDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.entity.Profile;
import org.ffb_be.entity.Role;
import org.ffb_be.entity.User;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.RoleRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryUpload cloudinaryUpload;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, CloudinaryUpload cloudinaryUpload, RoleRepository roleRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryUpload = cloudinaryUpload;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
    }
    public void create(UserCreateDTO userCreateDTO) throws IOException {
        User user = new User();
        BeanUtils.copyProperties(userCreateDTO, user);
        userRepository.findByEmail(userCreateDTO.getEmail()).ifPresent((x)->{
            throw new NonUniqueResultException("Email already exists!");
        });
        userRepository.findByPhone(userCreateDTO.getPhone()).ifPresent((x)->{
            throw new NonUniqueResultException("Phone already exists!");
        });
        Role role = roleRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setUsername(userCreateDTO.getPhone());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        userRepository.save(user);
    }



    @Override
    public Page<UserResponseDTO> findAll(String search, Pageable pageable) {
        return userRepository.findByAllField(search, pageable).map(user -> {
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            BeanUtils.copyProperties(user, userResponseDTO);
            userResponseDTO.setAvatar(user.getProfile().getAvatar());
            userResponseDTO.setName(user.getProfile().getName());
            return userResponseDTO;
        });
    }


    public void update(UserUpdateDTO userUpdateDTO, MultipartFile avatar) throws IOException {
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        Profile p = profileRepository.findById(userUpdateDTO.getId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        p.setAddress(userUpdateDTO.getAddress());
        p.setName(userUpdateDTO.getName());
        p.setGender(userUpdateDTO.getGender());
        p.setDob(userUpdateDTO.getDob());
        String url = cloudinaryUpload.uploadFile(avatar);
        p.setAvatar(url);
        user.setProfile(p);
        userRepository.save(user);
    }


}
