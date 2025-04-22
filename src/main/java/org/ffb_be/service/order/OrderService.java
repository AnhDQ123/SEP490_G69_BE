package org.ffb_be.service.order;

import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.order.CountDTO;
import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.ReturnOrderDTO;
import org.ffb_be.dto.payment.ShipPaymentDTO;
import org.ffb_be.dto.product.TopProductDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface OrderService {
    Page<OrderDTO> findAllReturnPending( Pageable pageable);
    Page<OrderDTO> findAllReturnRejected( Pageable pageable);
    Page<OrderDTO> findAllReturned( Pageable pageable);
    Order save(OrderDTO oderDTO) throws IOException;
    OrderDTO viewOrder(Long id) throws IOException;
    Page<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status, Pageable pageable);
    void cancelOrder(Long id,String reason);
    Page<OrderDTO> findAllByShopAndStatus(Long id,OrderStatus status,Pageable pageable);
    List<ShipPaymentDTO> findAllShipPaymentByShopId(Long id);
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
    Long countAllByStatus(OrderStatus status);
    Page<OrderDTO> findAllByFilter(String orderCode, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    void updatePrice(Long id, BigDecimal price);
    OrderDTO getOrder(Long id);
    List<CountByYearDTO> getOrderCountByStatusAndYear(OrderStatus status, LocalDate startDate, LocalDate endDate);
    List<CountByMonthDTO> getOrderCountByStatusAndMonth(OrderStatus status, LocalDate startDate, LocalDate endDate);
    List<CountByDateDTO> getOrderCountByStatusAndDay(OrderStatus status, LocalDate startDate, LocalDate endDate);
    List<TopProductDTO> getTopSellingProductsThisMonth();
    List<TopProductDTO> getTopSellingProductsThisYear();
    List<TopProductDTO> getTopSellingProductsToday();
    Long countAllOrders();
    List<Object[]> countShopOrdersByStatusAndYear(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId);
    List<Object[]> countShopOrdersByStatusAndMonth(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId);
    List<Object[]> countShopOrdersByStatusAndDay(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId);
    Map<Integer, BigDecimal> calculateShopRevenueByYear(LocalDate startDate, LocalDate endDate, Long shopId);
    Map<String, BigDecimal> calculateShopRevenueByMonth(LocalDate startDate, LocalDate endDate, Long shopId);
    Map<Date, BigDecimal> calculateShopRevenueByDay(LocalDate startDate, LocalDate endDate, Long shopId);
    Page<OrderDTO> findAllByShopAndPending(Long id,Pageable pageable);

}
