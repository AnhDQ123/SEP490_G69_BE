package org.ffb_be.service.order;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface OrderService {
    List<Order> save(OrderDTO oderDTO) throws IOException;
    List<OrderDTO> viewOrder(List<Long> id) throws IOException;
    List<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status);
    void cancelOrder(Long id);
}
