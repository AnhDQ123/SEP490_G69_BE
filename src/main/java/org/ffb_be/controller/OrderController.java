package org.ffb_be.controller;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.service.order.OrderService;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> checkout(@RequestParam List<Long> ids) throws IOException {
        List<OrderDTO> orders = orderService.viewOrder(ids);

        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy sản phẩm nào với danh sách ID đã cung cấp"));
        }

        return ResponseEntity.ok(orders);
    }
    @GetMapping("/checkout")
    public ResponseEntity<?> findByStatus(@RequestParam OrderStatus status) throws IOException {
        List<OrderDTO> orders = orderService.findAllByStatus(status);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy sản phẩm nào với danh sách ID đã cung cấp"));
        }

        return ResponseEntity.ok(orders);
    }

}
