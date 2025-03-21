package org.ffb_be.service.order;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface OrderService {
    List<Order> save(OrderDTO oderDTO) throws IOException;
    OrderDTO viewOrder(Long id) throws IOException;
    Page<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status, Pageable pageable);
    void cancelOrder(Long id);
    Page<OrderDTO> findAllByShopAndStatus(Long id,OrderStatus status,Pageable pageable);
    void acceptOrder(Long id);
    void rejectOrder(Long id);
    void changeStatus(Long id, OrderStatus status, MultipartFile avatar) throws IOException;
    void accecptShipping(Long id,Long userId);
    Page<OrderDTO>findAllByShipper(Long id, Pageable pageable);
}
