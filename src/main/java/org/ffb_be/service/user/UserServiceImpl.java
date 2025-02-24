package org.ffb_be.service.user;

import jakarta.persistence.NonUniqueResultException;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.entity.User;
import org.ffb_be.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setUsername(userCreateDTO.getPhone());
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        userRepository.save(user);
    }


}
