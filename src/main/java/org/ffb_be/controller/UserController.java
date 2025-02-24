package org.ffb_be.controller;

import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.entity.User;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.service.user.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
        User user = optionalUser.get();
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@Validated @ModelAttribute("user") UserCreateDTO user,
                                         BindingResult bindingResult) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        userService.create(user);
        return ResponseEntity.ok().body(user);
    }


}
