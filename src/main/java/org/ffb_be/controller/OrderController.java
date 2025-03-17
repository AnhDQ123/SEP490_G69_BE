package org.ffb_be.controller;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        // Trả về danh sách tất cả Order đã tạo
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
}
