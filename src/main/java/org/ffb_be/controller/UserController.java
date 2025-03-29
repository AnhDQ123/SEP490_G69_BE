package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.service.user.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok( userService.findById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Validated @RequestBody UserCreateDTO user,
                                         BindingResult bindingResult) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        userService.create(user);
        return ResponseEntity.ok().body(user);
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@Validated @ModelAttribute("employee") UserUpdateDTO user,
                                         BindingResult bindingResult,
                                         @RequestParam("avatar") MultipartFile avatar) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data!");
        }
        userService.update(user, avatar);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/inactive")
    public void inactiveUser(@RequestParam Long id) throws IOException {
       userService.inactiveUser(id);
    }

    @PostMapping("/active")
    public void activeUser(@RequestParam Long id) throws IOException {
        userService.activeUser(id);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "search", defaultValue = "", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( userService.findAll(search,pageable));
    }
}
