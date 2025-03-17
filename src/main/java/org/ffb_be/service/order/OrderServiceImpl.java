package org.ffb_be.service.order;

import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.OrderItemDTO;
import org.ffb_be.dto.order.OrderItemOptionDTO;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.OrderItem;
import org.ffb_be.entity.OrderItemOption;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final UserRepository userRepository;
    private final DeliveryMethodRepository deliveryMethodRepository;
    private final PaymentRepository paymentRepository;
    private final ShopRepository shopRepository;
    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, FoodOptionRepository foodOptionRepository, OrderItemRepository orderItemRepository, OrderItemOptionRepository orderItemOptionRepository, UserRepository userRepository, DeliveryMethodRepository deliveryMethodRepository, PaymentRepository paymentRepository, ShopRepository shopRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.foodOptionRepository = foodOptionRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemOptionRepository = orderItemOptionRepository;
        this.userRepository = userRepository;
        this.deliveryMethodRepository = deliveryMethodRepository;
        this.paymentRepository = paymentRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public void save(OrderDTO orderDTO) throws IOException {
        // Nhóm sản phẩm theo Shop
        Map<Long, List<OrderItemDTO>> shopOrderItems = new HashMap<>();

        for (OrderItemDTO orderItemDTO : orderDTO.getOrderItem()) {
            Long shopId = shopRepository.findByProduct(orderItemDTO.getProductId()).getId();
            shopOrderItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(orderItemDTO);
        }

        // Duyệt từng shop để tạo Order riêng
        for (Map.Entry<Long, List<OrderItemDTO>> entry : shopOrderItems.entrySet()) {
            Long shopId = entry.getKey();
            List<OrderItemDTO> orderItemDTOList = entry.getValue();

            // Tạo đơn hàng mới cho từng shop
            Order order = new Order();
            order.setOwner(userRepository.findById(orderDTO.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
            order.setShipper(userRepository.findById(orderDTO.getShipperId())
                    .orElseThrow(() -> new RuntimeException("Shipper not found")));
            order.setStatus(OrderStatus.PENDING);
            order.setDeliveryMethod(deliveryMethodRepository.findById(orderDTO.getShipMethodId())
                    .orElseThrow(() -> new RuntimeException("Delivery method not found")));
            order.setPaymentMethod(paymentRepository.findById(orderDTO.getPaymentMethodId())
                    .orElseThrow(() -> new RuntimeException("Payment method not found")));
            order.setTotal(BigDecimal.ZERO);
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order); // Lưu Order trước để có ID

            BigDecimal totalOrderPrice = BigDecimal.ZERO;

            // Duyệt qua từng OrderItem của shop này
            for (OrderItemDTO orderItemDTO : orderItemDTOList) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(productRepository.findById(orderItemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found")));
                orderItem.setQuantity(orderItemDTO.getQuantity());
                orderItem.setCreatedAt(LocalDateTime.now());

                // **Lưu OrderItem trước để có ID**
                orderItem = orderItemRepository.save(orderItem);

                List<OrderItemOption> orderItemOptions = new ArrayList<>();
                for (OrderItemOptionDTO orderItemOptionDTO : orderItemDTO.getOrderItemOptions()) {
                    OrderItemOption orderItemOption = new OrderItemOption();

                    if (orderItemOptionDTO.getTypeId() == 2) {
                        BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId())
                                .orElseThrow(() -> new RuntimeException("Food Option not found"))
                                .getPrice();
                        orderItem.setUnitPrice(unitPrice);
                    }

                    orderItemOption.setOrderItem(orderItem);
                    orderItemOption.setQuantity(orderItemOptionDTO.getQuantity());
                    orderItemOption.setUnitPrice(foodOptionRepository.findById(orderItemOptionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found")).getPrice());
                    orderItemOption.setTotalPrice(orderItemOption.getUnitPrice()
                            .multiply(BigDecimal.valueOf(orderItemOption.getQuantity()))
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
                    orderItemOption.setFoodOption(foodOptionRepository.findById(orderItemOptionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found")));

                    totalOrderPrice = totalOrderPrice.add(orderItemOption.getTotalPrice());
                    orderItemOptions.add(orderItemOption);
                }

                // **Lưu tất cả OrderItemOptions vào DB sau khi OrderItem có ID**
                orderItemOptionRepository.saveAll(orderItemOptions);

                // **Lưu OrderItem ngay sau khi hoàn thành**
                orderItemRepository.save(orderItem);
            }

            // Cập nhật tổng tiền của Order (bao gồm phí ship)
            totalOrderPrice = totalOrderPrice.add(order.getDeliveryMethod().getFee());
            order.setShipping_address(orderDTO.getAddress());
            order.setTotal(totalOrderPrice);
            orderRepository.save(order);
        }
    }




}
