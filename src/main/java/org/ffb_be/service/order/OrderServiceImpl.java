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
    public List<Order> save(OrderDTO orderDTO) throws IOException {
        Map<Long, List<OrderItemDTO>> shopOrderItems = new HashMap<>();

        for (OrderItemDTO orderItemDTO : orderDTO.getOrderItem()) {
            Long shopId = shopRepository.findByProduct(orderItemDTO.getProductId()).getId();
            shopOrderItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(orderItemDTO);
        }

        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<Long, List<OrderItemDTO>> entry : shopOrderItems.entrySet()) {
            List<OrderItemDTO> orderItemDTOList = entry.getValue();

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
            orderRepository.save(order);

            BigDecimal totalOrderPrice = BigDecimal.ZERO;

            for (OrderItemDTO orderItemDTO : orderItemDTOList) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(productRepository.findById(orderItemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found")));
                orderItem.setQuantity(orderItemDTO.getQuantity());
                orderItem.setCreatedAt(LocalDateTime.now());

                orderItem = orderItemRepository.save(orderItem);

                List<OrderItemOption> orderItemOptions = new ArrayList<>();
                for (OrderItemOptionDTO orderItemOptionDTO : orderItemDTO.getOrderItemOptions()) {
                    OrderItemOption orderItemOption = new OrderItemOption();
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

                orderItemOptionRepository.saveAll(orderItemOptions);
            }

            totalOrderPrice = totalOrderPrice.add(order.getDeliveryMethod().getFee());
            order.setTotal(totalOrderPrice);
            orderRepository.save(order);

            createdOrders.add(order);
        }

        return createdOrders;  // Trả về danh sách tất cả Order đã được tạo
    }

    @Override
    public List<OrderDTO> viewOrder(List<Long> id) throws IOException {
        List<Order> orders = orderRepository.findAllById(id); // Load tất cả đơn hàng trước
        List<OrderDTO> orderDTOs = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setAddress(order.getShipping_address());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            // Kiểm tra null trước khi lấy ID
            if(order.getVoucher() != null) {
                orderDTO.setVoucherId(order.getVoucher().getId());
                orderDTO.setVoucherAmount(order.getVoucher().getDiscount_percentage());
            }else orderDTO.setVoucherAmount(BigDecimal.ZERO);

            orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
            orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
            orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);

            // Danh sách Order Items
            List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
            BigDecimal orderItemTotal = BigDecimal.ZERO;
            for (OrderItem orderItem : order.getOrderItems()) {
                OrderItemDTO orderItemDTO = new OrderItemDTO();
                orderItemDTO.setId(orderItem.getId());
                orderItemDTO.setQuantity(orderItem.getQuantity());
                orderItemDTO.setProductId(orderItem.getProduct().getId());
                orderItemDTO.setTotal(orderItem.getTotalPrice());
                if(orderItem.getProduct().getDiscount() != null){
                    orderItemDTO.setDiscoundId(orderItem.getProduct().getDiscount().getId());
                    orderItemDTO.setDiscount(orderItem.getProduct().getDiscount().getDiscount_percentage());
                }else orderItemDTO.setDiscount(BigDecimal.ZERO);

                orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
                orderItemDTO.setOrderId(order.getId());
                orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
                orderDTO.setImage(shopRepository.findByProduct(orderItemDTO.getProductId()).getBackgroundImage());
                orderItemDTO.setProductName(productRepository.findById(orderItemDTO.getProductId()).get().getName());
                orderItemDTO.setImage(productRepository.findById(orderItemDTO.getProductId()).get().getImage());
                // Danh sách Order Item Options
                List<OrderItemOptionDTO> orderItemOptionDTOList = new ArrayList<>();
                BigDecimal orderItemOptionTotal = BigDecimal.ZERO;
                for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                    OrderItemOptionDTO orderItemOptionDTO = new OrderItemOptionDTO();
                    orderItemOptionDTO.setId(orderItemOption.getId());
                    orderItemOptionDTO.setOptionId(orderItemOption.getFoodOption().getId());
                    orderItemOptionDTO.setQuantity(orderItemOption.getQuantity());
                    orderItemOptionDTO.setOrderItemId(orderItem.getId());
                    orderItemOptionDTO.setPrice(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice());
                    orderItemOptionDTO.setTypeId(orderItemOption.getFoodOption().getType().getId());
                    if (orderItemOptionDTO.getTypeId() == 2) {
                        BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId())
                                .orElseThrow(() -> new RuntimeException("Food Option not found"))
                                .getPrice();
                        orderItemDTO.setPrice(unitPrice);
                        orderItemDTO.setTotal(unitPrice.multiply(BigDecimal.valueOf(orderItemDTO.getQuantity())));
                        orderItemOptionDTO.setQuantity(orderItemDTO.getQuantity());
                    }
                    // Kiểm tra null trước khi lấy optionId

                    orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                    orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());
                    if(orderItemOptionDTO.getTypeId() != 2){
                        orderItemOptionDTO.setTotal(orderItemOptionDTO.getPrice().multiply(BigDecimal.valueOf(orderItemOptionDTO.getQuantity())));
                    }
                    orderItemOptionTotal=orderItemOptionTotal.add(orderItemOptionDTO.getTotal());
                    orderItemOptionDTOList.add(orderItemOptionDTO);
                }
                orderItemTotal = orderItemTotal.add(orderItemOptionTotal);
                orderItemDTO.setTotal(orderItemTotal.multiply(BigDecimal.ONE.subtract(orderItemDTO.getDiscount())));
                orderItemTotal=orderItemDTO.getTotal();
                orderItemDTO.setOrderItemOptions(orderItemOptionDTOList);
                orderItemDTOList.add(orderItemDTO);
            }
            orderTotal = orderTotal.add(orderItemTotal);
            orderDTO.setTotal(orderTotal.multiply(BigDecimal.ONE.subtract(orderDTO.getVoucherAmount())));
            orderDTO.setOrderItem(orderItemDTOList);
            orderDTOs.add(orderDTO);
        }
        return orderDTOs;
    }



}
