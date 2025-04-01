package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.shop.ShopDTO;
import org.ffb_be.dto.shop.ShopRegisterDTO;
import org.ffb_be.service.order.OrderService;
import org.ffb_be.service.shop.ShopService;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin("*")
@AllArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final OrderService orderService;
    @GetMapping
    public ResponseEntity<Page<ShopDTO>> getShops(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(value = "type",required = false) String type,
            Pageable pageable) {

        Page<ShopDTO> shops = shopService.getShops(type, status, search, pageable);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDTO> getShopById(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }

    @GetMapping("/byUser/{userId}")
    public ResponseEntity<ShopDTO> getShopByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(shopService.getShopByUserId(userId));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerShop(
            @RequestParam Long userId,
            @Validated @ModelAttribute ShopRegisterDTO shopDTO,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "background", required = false) MultipartFile background,
            @RequestParam(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestParam(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestParam(value = "registrationCert", required = false) MultipartFile registrationCert,
            @RequestParam(value = "foodSafetyCert", required = false) MultipartFile foodSafetyCert,
            @RequestParam(value = "menu", required = false) MultipartFile menu,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        // Gửi toàn bộ file lên Service, kể cả file rỗng
        shopService.registerShop(userId, shopDTO, logo, background, citizenIDFront, citizenIDBack, registrationCert, foodSafetyCert, menu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/{shopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateShop(
            @PathVariable Long shopId,
            @Validated @ModelAttribute ShopRegisterDTO shopDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "background", required = false) MultipartFile background,
            @RequestPart(value = "citizenIDFront", required = false) MultipartFile citizenIDFront,
            @RequestPart(value = "citizenIDBack", required = false) MultipartFile citizenIDBack,
            @RequestPart(value = "registrationCert", required = false) MultipartFile registrationCert,
            @RequestPart(value = "foodSafetyCert", required = false) MultipartFile foodSafetyCert,
            @RequestPart(value = "menu", required = false) MultipartFile menu,
            BindingResult result
    ) throws IOException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        shopService.updateShop(shopId, shopDTO, logo, background,citizenIDFront, citizenIDBack, registrationCert, foodSafetyCert, menu);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/isOpen")
    public ResponseEntity<Boolean> isShopOpen(@RequestParam Long shopId) {
        boolean isOpen = shopService.isShopOpen(shopId);
        return ResponseEntity.ok(isOpen);
    }

    @PutMapping("/{shopId}/active")
    public ResponseEntity<Void> activeShop(@PathVariable Long shopId) {
        shopService.updateShopStatus(shopId, Status.ACTIVE, "");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/inactive")
    public ResponseEntity<Void> inactiveShop(
            @PathVariable Long shopId,
            @RequestParam String reason
    ) {
        shopService.updateShopStatus(shopId, Status.INACTIVE, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/reject")
    public ResponseEntity<Void> rejectShop(
            @PathVariable Long shopId,
            @RequestParam String reason
    ) {
        shopService.rejectShop(shopId, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shopId}/approve")
    public ResponseEntity<Void> approveShop(@PathVariable Long shopId) {
        shopService.approveShop(shopId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/count/day")
    public List<CountByDateDTO> getShopCountByDayAndStatus(
            @RequestParam("status") String status) {
        Status status1=Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        return shopService.getShopCountByDayAndStatus(status1, startDate, endDate);
    }

    @GetMapping("/count/year")
    public List<CountByYearDTO> getShopCountByYear(
            @RequestParam("status") String status) {
        Status status1=Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(3);

        return shopService.getShopCountByYear(status1, startDate, endDate);
    }
    @GetMapping("/count/month")
    public List<CountByMonthDTO> getShopCountByMonth(
            @RequestParam("status") String status) {
        Status status1=Status.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(7);

        return shopService.getShopCountByMonth(status1, startDate, endDate);
    }
    @GetMapping("/count/pending")
    public long getShopPending() {
        return shopService.countPendingShop();
    }
    @GetMapping("/revenue/day")
    public Map<Long, Double> calculateShopRevenueByDay(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.calculateShopRevenueByDay(start, end, shopId);
    }

    // API để tính tổng doanh thu theo tháng cho cửa hàng cụ thể
    @GetMapping("/revenue/month")
    public Map<String, Double> calculateShopRevenueByMonth(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.calculateShopRevenueByMonth(start, end, shopId);
    }

    // API để tính tổng doanh thu theo năm cho cửa hàng cụ thể
    @GetMapping("/revenue/year")
    public Map<Integer, Double> calculateShopRevenueByYear(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.calculateShopRevenueByYear(start, end, shopId);
    }
    @GetMapping("/count/order/day")
    public List<Object[]> countOrdersByStatusAndDay(
            @RequestParam("status") String status,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.countShopOrdersByStatusAndDay(status, start, end, shopId);
    }

    // API để đếm số lượng đơn hàng theo tháng cho cửa hàng cụ thể
    @GetMapping("/count/order/month")
    public List<Object[]> countOrdersByStatusAndMonth(
            @RequestParam("status") String status,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.countShopOrdersByStatusAndMonth(status, start, end, shopId);
    }

    // API để đếm số lượng đơn hàng theo năm cho cửa hàng cụ thể
    @GetMapping("/count/order/year")
    public List<Object[]> countOrdersByStatusAndYear(
            @RequestParam("status") String status,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("shopId") Long shopId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return orderService.countShopOrdersByStatusAndYear(status, start, end, shopId);
    }
}