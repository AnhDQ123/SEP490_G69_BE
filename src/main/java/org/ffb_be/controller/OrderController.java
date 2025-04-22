package org.ffb_be.controller;

import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.order.CountDTO;
import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.ReturnOrderDTO;
import org.ffb_be.dto.product.TopProductDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.service.order.OrderService;
import org.ffb_be.service.qr.QrService;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final QrService qrService;

    public OrderController(OrderService orderService, QrService qrService) {
        this.orderService = orderService;
        this.qrService = qrService;
    }

    @PostMapping("/add")
    public ResponseEntity<List<Map<String, Object>>> saveOrder(@RequestBody OrderDTO orderDTO) throws IOException {
        Order createdOrders = orderService.save(orderDTO);
        List<Map<String, Object>> responseList = new ArrayList<>();
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", createdOrders.getId());
            response.put("total", createdOrders.getTotal());
            response.put("status", createdOrders.getStatus().toString());
            responseList.add(response);
        return ResponseEntity.ok(responseList);
    }
    @GetMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam Long id) throws IOException {
        OrderDTO orders = orderService.viewOrder(id);

        if (orders==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy sản phẩm nào với danh sách ID đã cung cấp"));
        }

        return ResponseEntity.ok(orders);
    }
    @GetMapping("/status")
    public ResponseEntity<?> findByStatus(@RequestParam Long id ,
                                          @RequestParam OrderStatus status,
                                           Pageable pageable) throws IOException {
        Page<OrderDTO> orders = orderService.findAllByOwnerAndStatus(id,status,pageable);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy sản phẩm nào với danh sách ID đã cung cấp"));
        }
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/cancel")
    public void cancelOrder(@RequestParam Long id,@RequestParam String reason) throws IOException {
        orderService.cancelOrder(id,reason);
    }
    @PostMapping("/accept")
    public void acceptOrder(@RequestParam Long id) throws IOException {
        orderService.acceptOrder(id);
        orderService.assignShipper();
    }
    @PostMapping("/reject")
    public void rejectOrder(@RequestParam Long id) throws IOException {
        orderService.rejectOrder(id);
    }
    @PostMapping("/changeStatus")
    public void changeStatus(@RequestParam Long id,@RequestParam Long userId,@RequestParam OrderStatus status,@RequestParam("avatar") MultipartFile avatar) throws IOException {
        orderService.changeStatus(id,userId, status, avatar);
    }

    @GetMapping("/shop/status")
    public ResponseEntity<?> findByShopStatus(@RequestParam Long id ,@RequestParam OrderStatus status, Pageable pageable) throws IOException {
        return ResponseEntity.ok(orderService.findAllByShopAndStatus(id,status,pageable));
    }
    @GetMapping("/shop/pending")
    public ResponseEntity<?> findByShopPending(@RequestParam Long id , Pageable pageable) throws IOException {
        return ResponseEntity.ok(orderService.findAllByShopAndPending( id, pageable));
    }

    @GetMapping("/shipper")
    public ResponseEntity<?> findByShipper(@RequestParam Long id, Pageable pageable) throws IOException {
        return ResponseEntity.ok(orderService.findAllByShipper(id,pageable));
    }
    @PostMapping("/acceptShip")
    public void acceptShip(@RequestParam Long id,@RequestParam Long userId) throws IOException {
        orderService.acceptShipping(id,userId);
    }
    @PostMapping("/returnOrder")
    public void returnOrder(@RequestParam Long id,@RequestParam Long userId,@RequestParam String reason,@RequestParam("avatar") MultipartFile avatar) throws IOException {
        orderService.returnOrder(id,userId,reason, avatar);
    }

    @GetMapping("/viewReturn")
    public ReturnOrderDTO viewReturn(@RequestParam Long id) throws IOException {
        return orderService.viewReturnOrder(id);
    }
    @PostMapping("/acceptReturn")
    public void acceptReturn(@RequestParam Long id) throws IOException {
        orderService.acceptReturnOrder(id);
    }
    @PostMapping("/rejectReturn")
    public void rejectReturn(@RequestParam Long id) throws IOException {
        orderService.rejectReturnOrder(id);
    }
    @GetMapping("/count")
    public CountDTO countOrderByStatus(@RequestParam Long id ) {
        return orderService.countOrderByStatus(id);
    }

    @GetMapping
    public ResponseEntity<?> findAllByFilter(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate,
            @RequestParam(value = "orderCode", required = false ) String orderCode,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllByFilter(orderCode, status,startDate,endDate,pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
    @GetMapping("/count/day")
    public List<CountByDateDTO> getOrderCountByStatusAndDay(
            @RequestParam("status") String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        return orderService.getOrderCountByStatusAndDay(orderStatus, startDate, endDate);
    }
    @GetMapping("/count/month")
    public List<CountByMonthDTO> getOrderCountByStatusAndMonth(
            @RequestParam("status") String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(7);

        return orderService.getOrderCountByStatusAndMonth(orderStatus, startDate, endDate);
    }

    @GetMapping("/count/year")
    public List<CountByYearDTO> getOrderCountByStatusAndYear(
            @RequestParam("status") String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(3);

        return orderService.getOrderCountByStatusAndYear(orderStatus, startDate, endDate);
    }
    @GetMapping("/top-selling/today")
    public List<TopProductDTO> getTopSellingProductsToday() {
        return orderService.getTopSellingProductsToday();
    }
    @GetMapping("/top-selling/month")
    public List<TopProductDTO> getTopSellingProductsThisMonth() {
        return orderService.getTopSellingProductsThisMonth();
    }

    // API để lấy sản phẩm bán chạy nhất trong năm nay
    @GetMapping("/top-selling/year")
    public List<TopProductDTO> getTopSellingProductsThisYear() {
        return orderService.getTopSellingProductsThisYear();
    }
    @GetMapping("/count/orders")
    public Long countAllOrders() {
        return orderService.countAllOrders();
    }


    @GetMapping("/generateQr/{orderId}/{shopId}")
    public String generateQr(@PathVariable Long orderId, @PathVariable Long shopId) {
        return qrService.generateQrCode(orderId, shopId);
    }

    @PostMapping("/updatePaymentProof/{orderId}")
    public void updatePaymentProof(@PathVariable Long orderId,
                                   @RequestParam("paymentProof") MultipartFile paymentProof
    ) throws IOException {
        qrService.updatePaymentProof(orderId, paymentProof);
    }
    @PostMapping("/update")
    public void update(@RequestParam Long orderId,@RequestParam Long shipId,@RequestParam Long payId){
        orderService.update(orderId,shipId,payId);
    }
    @GetMapping("/return/pending")
    public Page<OrderDTO> getAllReturnPendingRequest(@RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                                     @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return orderService.findAllReturnPending(pageable);
    }
    @GetMapping("/return/rejected")
    public Page<OrderDTO> getAllReturnRejectedRequest(@RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                                      @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return orderService.findAllReturnRejected(pageable);
    }
    @GetMapping("/return/accepted")
    public Page<OrderDTO> getAllReturnedRequest(@RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                                @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return orderService.findAllReturned(pageable);
    }
    @GetMapping("/return/count")
    public Long countAllReturn() {
        return orderService.countAllByStatus(OrderStatus.RETURN_PENDING);
    }
    @PostMapping("/update/total")
    public void updateTotal(@RequestParam("total") BigDecimal total,@RequestParam("id") Long id) {
        orderService.updatePrice(id, total);
    }
}
