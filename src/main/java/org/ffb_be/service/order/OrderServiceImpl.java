package org.ffb_be.service.order;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.dto.order.*;
import org.ffb_be.dto.product.TopProductDTO;
import org.ffb_be.entity.*;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.OrderStatus;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.OrderMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
    private final OrderMapper orderMapper;
    private final DiscountRepository discountRepository;


    public Page<OrderDTO> toDTO(Page<Order> orders,Pageable pageable) {
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setAddress(order.getShippingAddress());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            orderDTO.setOwnerName(userRepository.findById(orderDTO.getOwnerId()).get().getProfile().getName());
            orderDTO.setPhone(userRepository.findById(orderDTO.getOwnerId()).get().getPhone());
            orderDTO.setStatus(order.getStatus().toString());
            orderDTO.setReason(order.getReason());
            if (order.getVoucher() != null) {
                orderDTO.setVoucherId(order.getVoucher().getId());
                orderDTO.setVoucherAmount(order.getVoucher().getDiscountValue());
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
                Discount discount=discountRepository.findByProduct(orderItem.getProduct());
                if (discount!=null) {
                    orderItemDTO.setDiscountId(discount.getId());
                    orderItemDTO.setDiscount(discount.getDiscount_percentage());
                } else {
                    orderItemDTO.setDiscount(BigDecimal.ZERO);
                }
                orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
                orderItemDTO.setOrderId(order.getId());
                orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
                orderDTO.setShopId(shopRepository.findByProduct(orderItemDTO.getProductId()).getId());
                orderDTO.setShopAddress(shopRepository.findByProduct(orderItemDTO.getProductId()).getAddress());
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
            order.setShippingAddress(orderDTO.getAddress());
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
            order.setStatus(OrderStatus.PENDING);
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
            orderDTO.setAddress(order.getShippingAddress());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            orderDTO.setPhone(userRepository.findById(orderDTO.getOwnerId()).get().getPhone());
            orderDTO.setOwnerName(userRepository.findById(orderDTO.getOwnerId()).get().getProfile().getName());
            orderDTO.setReason(order.getReason());
            if(order.getVoucher() != null) {
                orderDTO.setVoucherId(order.getVoucher().getId());
                orderDTO.setVoucherAmount(order.getVoucher().getDiscountValue());
            }else orderDTO.setVoucherAmount(BigDecimal.ZERO);
            orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
            orderDTO.setStatus(order.getStatus().toString());
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
              Discount discount=discountRepository.findByProduct(orderItem.getProduct());
                if (discount!=null) {
                    orderItemDTO.setDiscountId(discount.getId());
                    orderItemDTO.setDiscount(discount.getDiscount_percentage());
                } else {
                    orderItemDTO.setDiscount(BigDecimal.ZERO);
                }
                orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
                orderItemDTO.setOrderId(order.getId());
                orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
                orderDTO.setShopId(shopRepository.findByProduct(orderItemDTO.getProductId()).getId());
                orderDTO.setShopAddress(shopRepository.findByProduct(orderItemDTO.getProductId()).getAddress());
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
    public void cancelOrder(Long id,String reason) {
        Order order = orderRepository.findById(id).get();
        order.setReason(reason);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
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
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void rejectOrder(Long id) {
        Order order=orderRepository.findById(id).get();
        order.setStatus(OrderStatus.REJECTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void changeStatus(Long id,Long userId, OrderStatus status, MultipartFile avatar) throws IOException {
        Order order=orderRepository.findById(id).get();
        Image image=new Image();
        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("Uploading Avatar: " + avatar.getOriginalFilename());
            String url = cloudinaryUpload.uploadFile(avatar);
            image.setUrl(url);
            image.setRelatedId(order.getId());
            image.setOwnerId(userId);
            image.setType(typesRepository.findById(3l).get());
            imageRepository.save(image);
            System.out.println("Avatar URL: " + url);
        }
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void acceptShipping(Long id,Long userId) {
        Order order=orderRepository.findById(id).get();
        order.setShipper(userRepository.findById(userId).get());
        order.setStatus(OrderStatus.SHIPPING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllByShipper(Long id, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByShipper_Id(id, pageable);
        return toDTO(orders,pageable);
    }

    @Override
    @Transactional
    public void assignShipper() {
        List<Order> orders = orderRepository.findAllByStatus(OrderStatus.PROCESSING);
        List<User> availableShippers = userRepository.findAllAvailableShippers();

        if (orders.isEmpty() || availableShippers.isEmpty()) return;

        int shipperIndex = 0;
        int totalShippers = availableShippers.size();

        for (Order order : orders) {
            User shipper = availableShippers.get(shipperIndex);
            order.setShipper(shipper);
            orderRepository.save(order);

            // Gán tiếp cho shipper tiếp theo
            shipperIndex = (shipperIndex + 1) % totalShippers;
        }
    }

    @Override
    public void returnOrder(Long id,Long userId,String reason, MultipartFile avatar) throws IOException {
        Order order=orderRepository.findById(id).get();
        order.setReason(reason);
        Image image=new Image();
        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("Uploading Avatar: " + avatar.getOriginalFilename());
            String url = cloudinaryUpload.uploadFile(avatar);
            image.setUrl(url);
            image.setRelatedId(order.getId());
            image.setOwnerId(userId);
            image.setType(typesRepository.findById(3l).get());
            imageRepository.save(image);
            System.out.println("Avatar URL: " + url);
        }
        order.setStatus(OrderStatus.RETURN_PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public ReturnOrderDTO viewReturnOrder(Long id) throws IOException {
        OrderDTO orderDTO=viewOrder(id);
        List<Image> imageList=imageRepository.findAllByRelatedIdAndType_Id(orderDTO.getId(),3l);
        List<ImageDTO> imageDTOList=new ArrayList<>();
        for (Image image : imageList) {
            ImageDTO imageDTO=new ImageDTO();
            imageDTO.setUrl(image.getUrl());
            imageDTO.setRelatedId(orderDTO.getId());
            imageDTO.setId(image.getId());
            imageDTO.setOwnerId(image.getOwnerId());
            imageDTO.setTypeId(3l);
            imageDTOList.add(imageDTO);
        }
        ReturnOrderDTO returnOrderDTO=new ReturnOrderDTO();
        returnOrderDTO.setImage(imageDTOList);
        returnOrderDTO.setOrder(orderDTO);
        return returnOrderDTO;
    }

    @Override
    public void acceptReturnOrder(Long id) {
        Order order=orderRepository.findById(id).get();
        order.setStatus(OrderStatus.RETURNED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllByFilter(String orderCode, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, orderCode, pageable);
        return orders.map(this::toDTO);
    }

    @Override
    public OrderDTO getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order"));
        return toDTO(order);
    }

    public OrderDTO toDTO(Order order) {
        OrderDTO orderDTO = orderMapper.toDTO(order);
        orderDTO.setShopName(shopRepository.findByProduct(order.getOrderItems().get(0).getProduct().getId()).getName());
        orderDTO.setShopId(shopRepository.findByProduct(order.getOrderItems().get(0).getProduct().getId()).getId());
        orderDTO.setImage(shopRepository.findByProduct(order.getOrderItems().get(0).getProduct().getId()).getBackgroundImage());
        return orderDTO;
    }

    @Override
    public void rejectReturnOrder(Long id) {
        Order order=orderRepository.findById(id).get();
        order.setStatus(OrderStatus.RETURN_REJECTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public CountDTO countOrderByStatus(Long id) {
        List<Object[]> results = orderRepository.countOrdersByStatusForShop(id);
        CountDTO dto = new CountDTO();
        for (Object[] row : results) {
            OrderStatus status = (OrderStatus) row[0];
            int count = ((Long) row[1]).intValue();
            switch (status) {
                case PENDING -> dto.setPending(count);
                case PROCESSING -> dto.setProcessing(count);
                case SHIP_PENDING -> dto.setShipPending(count);
                case SHIPPING -> dto.setShipping(count);
                case DELIVERED -> dto.setDelivered(count);
                case CANCELLED -> dto.setCancelled(count);
                case RETURNED -> dto.setReturned(count);
                case REJECTED -> dto.setRejected(count);
                case RETURN_PENDING -> dto.setReturnPending(count);
                case RETURN_REJECTED -> dto.setReturnRejected(count);
            }
        }
        return dto;
    }
        public List<CountByDateDTO> getOrderCountByStatusAndYear(OrderStatus status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.countOrdersByStatusAndYear(status, startDateTime, endDateTime);
            List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
                CountByDateDTO countByDateDTO = new CountByDateDTO();
                countByDateDTO.setDate((LocalDateTime) result[0]);
                countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
            }
        return countByDateDTOS;
    }
    public List<CountByDateDTO> getOrderCountByStatusAndMonth(OrderStatus status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.countOrdersByStatusAndMonth(status, startDateTime, endDateTime);
        List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
            CountByDateDTO countByDateDTO = new CountByDateDTO();
            countByDateDTO.setDate((LocalDateTime) result[0]);
            countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
        }
        return countByDateDTOS;
    }
    public List<CountByDateDTO> getOrderCountByStatusAndDay(OrderStatus status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.countOrdersByStatusAndDay(status, startDateTime, endDateTime);
        List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
            CountByDateDTO countByDateDTO = new CountByDateDTO();
            countByDateDTO.setDate((LocalDateTime) result[0]);
            countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
        }
        return countByDateDTOS;
    }
    public  List<TopProductDTO> getTopSellingProductsToday() {
        LocalDate currentDate = LocalDate.now();

        // Lấy thời gian bắt đầu và kết thúc của ngày hôm nay
        LocalDateTime startOfDay = currentDate.atStartOfDay();  // 00:00:00
        LocalDateTime endOfDay = currentDate.atTime(23, 59, 59);
        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> results = orderRepository.findTopSellingProductsToday(startOfDay,endOfDay,pageable);

        List<TopProductDTO> topSellingProducts = new ArrayList<>();

        // Chuyển đổi kết quả thành Map với key là "productName" và value là "totalQuantity"
        for (Object[] result : results) {
            TopProductDTO topProductDTO = new TopProductDTO();
            topProductDTO.setName((String) result[1]);
            topProductDTO.setTotalQuantity((Long) result[2]);
            topProductDTO.setTotalValue((BigDecimal) result[3]);
            topSellingProducts.add(topProductDTO);
        }

        return topSellingProducts;
    }
    public  List<TopProductDTO> getTopSellingProductsThisMonth() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> results = orderRepository.findTopSellingProductsThisMonth(pageable);

        List<TopProductDTO> topSellingProducts = new ArrayList<>();

        // Chuyển đổi kết quả thành Map với key là "productName" và value là "totalQuantity"
        for (Object[] result : results) {
            TopProductDTO topProductDTO = new TopProductDTO();
            topProductDTO.setName((String) result[1]);
            topProductDTO.setTotalQuantity((Long) result[2]);
            topProductDTO.setTotalValue((BigDecimal) result[3]);
            topSellingProducts.add(topProductDTO);
        }

        return topSellingProducts;
    }

    // Phương thức để lấy danh sách sản phẩm bán chạy nhất trong năm nay
    public List<TopProductDTO> getTopSellingProductsThisYear() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> results = orderRepository.findTopSellingProductsThisYear(pageable);
        List<TopProductDTO> topSellingProducts = new ArrayList<>();
        // Chuyển đổi kết quả thành Map với key là "productName" và value là "totalQuantity"
        for (Object[] result : results) {
            TopProductDTO topProductDTO = new TopProductDTO();
            topProductDTO.setName((String) result[1]);
            topProductDTO.setTotalQuantity((Long) result[2]);
            topProductDTO.setTotalValue((BigDecimal) result[3]);
            topSellingProducts.add(topProductDTO);
        }

        return topSellingProducts;
    }
    // Đếm số lượng đơn hàng
    public Long countAllOrders() {
        return orderRepository.countAllOrders();
    }

    public List<Object[]> countShopOrdersByStatusAndDay(String status, LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return orderRepository.countShopOrdersByStatusAndDay(status, startDateTime, endDateTime, shopId);
    }
    public List<Object[]> countShopOrdersByStatusAndMonth(String status, LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return orderRepository.countShopOrdersByStatusAndMonth(status, startDateTime, endDateTime, shopId);
    }

    // Đếm số lượng đơn hàng theo năm cho cửa hàng cụ thể
    public List<Object[]> countShopOrdersByStatusAndYear(String status, LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return orderRepository.countShopOrdersByStatusAndYear(status, startDateTime, endDateTime, shopId);
    }
    public Map<Long, Double> calculateShopRevenueByDay(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByDay(startDateTime, endDateTime, shopId);

        Map<Long, Double> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            Long shopIdResult = (Long) result[0];
            Double revenue = (Double) result[1];
            shopRevenue.put(shopIdResult, revenue);
        }
        return shopRevenue;
    }

    // Tính tổng doanh thu theo tháng
    public Map<String, Double> calculateShopRevenueByMonth(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByMonth(startDateTime, endDateTime, shopId);

        Map<String, Double> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            String monthYear = result[0] + "-" + String.format("%02d", result[1]);  // Format: YYYY-MM
            Double revenue = (Double) result[2];
            shopRevenue.put(monthYear, revenue);
        }
        return shopRevenue;
    }

    // Tính tổng doanh thu theo năm
    public Map<Integer, Double> calculateShopRevenueByYear(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByYear(startDateTime, endDateTime, shopId);

        Map<Integer, Double> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            Integer year = (Integer) result[0];
            Double revenue = (Double) result[1];
            shopRevenue.put(year, revenue);
        }
        return shopRevenue;
    }
}



