package org.ffb_be.controller;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.ReturnOrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.service.order.OrderService;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/add")
    public ResponseEntity<List<Map<String, Object>>> saveOrder(@RequestBody OrderDTO orderDTO) throws IOException {
        List<Order> createdOrders = orderService.save(orderDTO);

        if (createdOrders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Không thể tạo đơn hàng")));
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Order order : createdOrders) {
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("total", order.getTotal());
            response.put("status", order.getStatus().toString());
            responseList.add(response);
        }
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
    public ResponseEntity<?> findByStatus(@RequestParam Long id ,@RequestParam OrderStatus status,@RequestParam Pageable pageable) throws IOException {
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

}
