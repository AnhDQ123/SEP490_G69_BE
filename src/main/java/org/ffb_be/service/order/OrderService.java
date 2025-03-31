package org.ffb_be.service.order;

import org.apache.commons.lang3.tuple.Pair;
import org.ffb_be.dto.order.CountDTO;
import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.ReturnOrderDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface OrderService {
    List<Order> save(OrderDTO oderDTO) throws IOException;
    OrderDTO viewOrder(Long id) throws IOException;
    Page<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status, Pageable pageable);
    void cancelOrder(Long id,String reason);
    Page<OrderDTO> findAllByShopAndStatus(Long id,OrderStatus status,Pageable pageable);
    void acceptOrder(Long id);
    void rejectOrder(Long id);
    void changeStatus(Long id,Long userId, OrderStatus status, MultipartFile avatar) throws IOException;
    void acceptShipping(Long id,Long userId);
    Page<OrderDTO>findAllByShipper(Long id, Pageable pageable);
    void assignShipper();
    void returnOrder(Long id,Long userId,String reason,MultipartFile avatar) throws IOException;
    ReturnOrderDTO viewReturnOrder(Long id) throws IOException;
    void acceptReturnOrder(Long id);
    void rejectReturnOrder(Long id);
    CountDTO countOrderByStatus(Long id);

    Page<OrderDTO> findAllByFilter(String orderCode, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    OrderDTO getOrder(Long id);
    Map<Integer, Long> getOrderCountByStatusAndYear(String status, LocalDate startDate, LocalDate endDate);
    Map<String, Long> getOrderCountByStatusAndMonth(String status, LocalDate startDate, LocalDate endDate);
    Map<LocalDateTime, Long> getOrderCountByStatusAndDay(String status, LocalDate startDate, LocalDate endDate);
    Map<String, Pair<Long, BigDecimal>> getTopSellingProductsThisMonth();
    Map<String, Pair<Long, BigDecimal>> getTopSellingProductsThisYear();
    Map<String, Pair<Long, BigDecimal>> getTopSellingProductsToday();
    Long countAllOrders();
    List<Object[]> countShopOrdersByStatusAndYear(String status, LocalDate startDate, LocalDate endDate, Long shopId);
    List<Object[]> countShopOrdersByStatusAndMonth(String status, LocalDate startDate, LocalDate endDate, Long shopId);
    List<Object[]> countShopOrdersByStatusAndDay(String status, LocalDate startDate, LocalDate endDate, Long shopId);
    Map<Integer, Double> calculateShopRevenueByYear(LocalDate startDate, LocalDate endDate, Long shopId);
    Map<String, Double> calculateShopRevenueByMonth(LocalDate startDate, LocalDate endDate, Long shopId);
    Map<Long, Double> calculateShopRevenueByDay(LocalDate startDate, LocalDate endDate, Long shopId);
}
