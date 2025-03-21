package org.ffb_be.service.order;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.order.OrderDTO;
import org.ffb_be.dto.order.OrderItemDTO;
import org.ffb_be.dto.order.OrderItemOptionDTO;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Order;
import org.ffb_be.entity.OrderItem;
import org.ffb_be.entity.OrderItemOption;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.OrderStatus;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
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
    private final CloudinaryUpload cloudinaryUpload;
    private final TypesRepository typesRepository;
    private final ImageRepository imageRepository;


    public Page<OrderDTO> toDTO(Page<Order> orders,Pageable pageable) {
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setAddress(order.getShipping_address());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            if (order.getVoucher() != null) {
                orderDTO.setVoucherId(order.getVoucher().getId());
                orderDTO.setVoucherAmount(order.getVoucher().getDiscount_percentage());
            } else {
                orderDTO.setVoucherAmount(BigDecimal.ZERO);
            }
            orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
            orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
            orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);
            List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
            BigDecimal orderItemTotal = BigDecimal.ZERO;
            for (OrderItem orderItem : order.getOrderItems()) {
                OrderItemDTO orderItemDTO = new OrderItemDTO();
                orderItemDTO.setId(orderItem.getId());
                orderItemDTO.setQuantity(orderItem.getQuantity());
                orderItemDTO.setProductId(orderItem.getProduct().getId());
                orderItemDTO.setTotal(orderItem.getTotalPrice());
                if (orderItem.getProduct().getDiscount() != null) {
                    orderItemDTO.setDiscoundId(orderItem.getProduct().getDiscount().getId());
                    orderItemDTO.setDiscount(orderItem.getProduct().getDiscount().getDiscount_percentage());
                } else {
                    orderItemDTO.setDiscount(BigDecimal.ZERO);
                }
                orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
                orderItemDTO.setOrderId(order.getId());
                orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
                orderDTO.setImage(shopRepository.findByProduct(orderItemDTO.getProductId()).getBackgroundImage());
                orderItemDTO.setProductName(productRepository.findById(orderItemDTO.getProductId()).get().getName());
                orderItemDTO.setImage(productRepository.findById(orderItemDTO.getProductId()).get().getImage());
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
                        orderItemTotal = orderItemTotal.add(orderItemDTO.getTotal());
                        orderItemOptionDTO.setQuantity(orderItemDTO.getQuantity());
                    }
                    orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                    orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());
                    if (orderItemOptionDTO.getTypeId() != 2) {
                        orderItemOptionDTO.setTotal(orderItemOptionDTO.getPrice().multiply(BigDecimal.valueOf(orderItemOptionDTO.getQuantity())));
                        orderItemOptionTotal = orderItemOptionTotal.add(orderItemOptionDTO.getTotal());
                    }
                    orderItemOptionDTOList.add(orderItemOptionDTO);
                }
                orderItemTotal = orderItemTotal.add(orderItemOptionTotal);
                orderItemDTO.setTotal(orderItemTotal.multiply(BigDecimal.ONE.subtract(orderItemDTO.getDiscount())));
                orderItemDTO.setOrderItemOptions(orderItemOptionDTOList);
                orderItemDTOList.add(orderItemDTO);
            }
            BigDecimal orderTotal = orderItemTotal;
            orderDTO.setTotal(orderTotal.multiply(BigDecimal.ONE.subtract(orderDTO.getVoucherAmount())));
            orderDTO.setOrderItem(orderItemDTOList);
            orderDTOs.add(orderDTO);
        }
        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
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
    public OrderDTO viewOrder(Long id) throws IOException {
        Order order = orderRepository.findById(id).get(); // Load tất cả đơn hàng trước
        OrderDTO orderDTO = new OrderDTO();
        BigDecimal orderTotal = BigDecimal.ZERO;
            orderDTO.setId(order.getId());
            orderDTO.setAddress(order.getShipping_address());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            if(order.getVoucher() != null) {
                orderDTO.setVoucherId(order.getVoucher().getId());
                orderDTO.setVoucherAmount(order.getVoucher().getDiscount_percentage());
            }else orderDTO.setVoucherAmount(BigDecimal.ZERO);
            orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
            orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
            orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);
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
                        orderItemTotal=orderItemTotal.add(orderItemDTO.getTotal());
                        orderItemOptionDTO.setQuantity(orderItemDTO.getQuantity());
                    }
                    orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                    orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());
                    if(orderItemOptionDTO.getTypeId() != 2){
                        orderItemOptionDTO.setTotal(orderItemOptionDTO.getPrice().multiply(BigDecimal.valueOf(orderItemOptionDTO.getQuantity())));
                        orderItemOptionTotal=orderItemOptionTotal.add(orderItemOptionDTO.getTotal());
                    }
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
        return orderDTO;
    }


    @Override
    public Page<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOwner_IdAndStatus(id, status, pageable);
        return toDTO(orders,pageable);
    }


    @Override
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id).get();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllByShopAndStatus(Long id, OrderStatus status,Pageable pageable) {
        Page<Order> orderList=orderRepository.findOrdersByShopIdAndStatus(id, status,pageable);
        return toDTO(orderList,pageable);
    }

    @Override
    public void acceptOrder(Long id) {
        Order order=orderRepository.findById(id).get();
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    @Override
    public void rejectOrder(Long id) {
        Order order=orderRepository.findById(id).get();
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    @Override
    public void changeStatus(Long id, OrderStatus status, MultipartFile avatar) throws IOException {
        Order order=orderRepository.findById(id).get();
        Image image=new Image();
        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("Uploading Avatar: " + avatar.getOriginalFilename());
            String url = cloudinaryUpload.uploadFile(avatar);
            image.setUrl(url);
            image.setRelatedId(order.getId());
            image.setType(typesRepository.findById(3l).get());
            imageRepository.save(image);
            System.out.println("Avatar URL: " + url);
        }
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    public void accecptShipping(Long id,Long userId) {
        Order order=orderRepository.findById(id).get();
        order.setShipper(userRepository.findById(userId).get());
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllByShipper(Long id, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByShipper_Id(id, pageable);
        return toDTO(orders,pageable);
    }


}
