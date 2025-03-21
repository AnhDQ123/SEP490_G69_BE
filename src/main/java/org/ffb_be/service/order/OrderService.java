package org.ffb_be.service.order;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface OrderService {
    List<Order> save(OrderDTO oderDTO) throws IOException;
    List<OrderDTO> viewOrder(List<Long> id) throws IOException;
    List<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status);
    void cancelOrder(Long id);
    List<OrderDTO> findAllByShopAndStatus(Long id,OrderStatus status);
    void acceptOrder(Long id);
    void rejectOrder(Long id);
    void changeStatus(Long id, OrderStatus status, MultipartFile avatar) throws IOException;
}
