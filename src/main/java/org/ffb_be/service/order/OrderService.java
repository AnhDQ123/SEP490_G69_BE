package org.ffb_be.service.order;

import org.ffb_be.dto.order.OrderDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface OrderService {
    void save(OrderDTO oderDTO) throws IOException;
}
