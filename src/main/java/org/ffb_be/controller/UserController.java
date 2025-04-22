package org.ffb_be.controller;


import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.auth.userDto.UserCreateDTO;
import org.ffb_be.dto.auth.userDto.UserUpdateDTO;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.entity.Shop;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.ProfileRepository;
import org.ffb_be.repository.ShopRepository;
import org.ffb_be.repository.UserRepository;
import org.ffb_be.service.shop.ShopService;
import org.ffb_be.service.user.UserService;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final ShopRepository shopRepository;
    private final ShopService shopService;
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
    public ResponseEntity<?> updateProfile(@Validated @ModelAttribute UserUpdateDTO user,
                                         BindingResult bindingResult,
                                           MultipartFile avatar) throws IOException {
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

    @GetMapping("/count/year")
    public List<CountByYearDTO> getUserCountByYear(
            @RequestParam("status") String status) {
        Status status1 = Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(3);

        return userService.getUserCountByYearAndStatus(startDate, endDate,status1);
    }

    @GetMapping("/count/month")
    public List<CountByMonthDTO> getUserCountByMonth(
            @RequestParam("status") String status) {
        Status status1 = Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(7);

        return userService.getUserCountByMonthAndStatus(startDate, endDate,status1);
    }
    @GetMapping("/count/day")
    public List<CountByDateDTO> getUserCountByDayAndStatus(
            @RequestParam("status") String status) {
        Status status1 = Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return userService.getUserCountByDayAndStatus(startDate, endDate, status1);
    }
    @GetMapping("/count/shop")
    public long getUserHaveShopCount() {
        return userService.countUsersHaveShop();
    }
    @GetMapping("/count/shipper")
    public long getUserAreShipperCount() {
        return userService.countUsersAreShipper();
    }
    @GetMapping("/count/pendingshipper")
    public long getPendingShipper() {
        return userService.countPendingShipper();
    }
    @GetMapping("/count/all")
    public long getAllUserCount() {
        return userService.countAllUser();
    }

    @GetMapping("/shop")
    public ResponseEntity<?> getUserShop(@RequestParam("id") Long id) {
        try {
            return ResponseEntity.ok(shopService.getShopByOwnerId(id));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.ok().body(e.getMessage());
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getRoleProfile(id));
    }
    @PutMapping("/changePassword")
    public void changePassword(@RequestParam Long id,@RequestParam String oldPassword,@RequestParam String newPassword,@RequestParam String confirmPassword) {
        userService.changePassword(id, oldPassword, newPassword,confirmPassword);
    }
    @PutMapping("/forgot")
    public void forgotPassword(@RequestParam String phone ,@RequestParam String password,@RequestParam String confirmPassword) {
        userService.forgotPassword(phone,password,confirmPassword);
    }
    @GetMapping("/change/rate")
    public long getUserRate() {
        return userService.userChangeRate();
    }
}
