package org.ffb_be.controller;

import org.ffb_be.dto.auth.ProfileDto.ProfileDTO;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.repository.ProfileRepository;
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
    private final ProfileRepository profileRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, ProfileRepository profileRepository, UserService userService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {

        Optional<ProfileDTO> optionalUser = profileRepository.findByUserId(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
        ProfileDTO profile = optionalUser.get();
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@Validated @RequestBody UserCreateDTO user,
                                         BindingResult bindingResult) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        userService.create(user);
        return ResponseEntity.ok().body(user);
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@Validated @ModelAttribute("employee") UserUpdateDTO user,
                                         BindingResult bindingResult,
                                         @RequestParam("avatar") MultipartFile avatar) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        userService.update(user, avatar);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "search", defaultValue = "", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( userService.findAll(search,pageable));
    }
}
