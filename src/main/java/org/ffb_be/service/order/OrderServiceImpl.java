package org.ffb_be.service.order;


import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.discount.DiscountDTO2;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.dto.order.*;
import org.ffb_be.dto.payment.ShipPaymentDTO;
import org.ffb_be.dto.product.TopProductDTO;
import org.ffb_be.entity.*;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.DeliveryStatus;
import org.ffb_be.utils.enums.OrderStatus;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.OrderMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final VoucherRepository voucherRepository;


    public Page<OrderDTO> toDTO(Page<Order> orders,Pageable pageable) {
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setAddress(order.getShippingAddress());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOwnerId(order.getOwner().getId());
            orderDTO.setVoucherAmount(order.getVoucher() != null ?order.getVoucher().getDiscountValue().multiply(order.getTotal()): null);
            orderDTO.setOwnerName(userRepository.findById(orderDTO.getOwnerId()).get().getProfile().getName());
            orderDTO.setPhone(userRepository.findById(orderDTO.getOwnerId()).get().getPhone());
            orderDTO.setStatus(order.getStatus().toString());
            orderDTO.setReason(order.getReason());
            orderDTO.setOrderCode(order.getOrderCode());
            orderDTO.setCreatedAt(order.getCreatedAt());
            orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
            orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
            orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);
            orderDTO.setProofImage(order.getPaymentProof());
            List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                OrderItemDTO orderItemDTO = new OrderItemDTO();
                orderItemDTO.setId(orderItem.getId());
                orderItemDTO.setQuantity(orderItem.getQuantity());
                orderItemDTO.setProductId(orderItem.getProduct().getId());
                orderItemDTO.setTotal(orderItem.getTotalPrice());
                orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
                orderItemDTO.setOrderId(order.getId());
                orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
                orderDTO.setShopId(shopRepository.findByProduct(orderItemDTO.getProductId()).getId());
                orderDTO.setShopAddress(shopRepository.findByProduct(orderItemDTO.getProductId()).getAddress());
                orderDTO.setImage(shopRepository.findByProduct(orderItemDTO.getProductId()).getBackgroundImage());
                orderItemDTO.setProductName(productRepository.findById(orderItemDTO.getProductId()).get().getName());
                orderItemDTO.setImage(productRepository.findById(orderItemDTO.getProductId()).get().getImage());
                List<OrderItemOptionDTO> orderItemOptionDTOList = new ArrayList<>();
                for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                    OrderItemOptionDTO orderItemOptionDTO = new OrderItemOptionDTO();
                    orderItemOptionDTO.setId(orderItemOption.getId());
                    orderItemOptionDTO.setOptionId(orderItemOption.getFoodOption().getId());
                    orderItemOptionDTO.setQuantity(orderItemOption.getQuantity());
                    orderItemOptionDTO.setOrderItemId(orderItem.getId());
                    orderItemOptionDTO.setPrice(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice());
                    orderItemOptionDTO.setTypeId(orderItemOption.getFoodOption().getType().getId());
                    orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                    orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());
                    orderItemOptionDTOList.add(orderItemOptionDTO);
                }
                orderItemDTO.setOrderItemOptions(orderItemOptionDTOList);
                orderItemDTOList.add(orderItemDTO);
            }
            orderDTO.setTotal(order.getTotal());
            orderDTO.setOrderItem(orderItemDTOList);
            orderDTOs.add(orderDTO);
        }
        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
    }

    @Override
    public void update(Long id, Long shipId, Long payId) {
        Order order=orderRepository.findById(id).get();
        order.setDeliveryMethod(deliveryMethodRepository.findById(shipId).get());
        order.setPaymentMethod(paymentRepository.findById(payId).get());
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllReturnPending( Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByStatus( OrderStatus.RETURN_PENDING, pageable);
        return toDTO(orders,pageable);
    }

    @Override
    public Page<OrderDTO> findAllReturnRejected(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByStatus( OrderStatus.RETURN_REJECTED, pageable);
        return orders.map(this::toDTO);
    }

    @Override
    public Page<OrderDTO> findAllReturned(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByStatus( OrderStatus.RETURNED, pageable);
        return orders.map(this::toDTO);
    }

    @Override
    public Order save(OrderDTO orderDTO) throws IOException {
        Order order = new Order();
        if(orderDTO.getShopId() == shopRepository.findByOwnerId(orderDTO.getOwnerId()).get().getId()) {
            throw new RuntimeException("You cannot place an order for your own shop!");
        }
        order.setOwner(userRepository.findById(orderDTO.getOwnerId()).get());
        order.setPaymentMethod(paymentRepository.findById(orderDTO.getPaymentMethodId()).get());
        order.setDeliveryMethod(deliveryMethodRepository.findById(orderDTO.getShipMethodId()).get());
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        order.setShippingAddress(orderDTO.getAddress());
        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItemDTO> orderItemDTOList = orderDTO.getOrderItem();
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderItemDTO orderItemDTO : orderItemDTOList) {
            BigDecimal orderItemTotal = BigDecimal.ZERO;
            Product product = productRepository.findById(orderItemDTO.getProductId()).get();

            if(orderItemDTO.getQuantity() > product.getQuantity()) {
                orderRepository.delete(order);
                return null;  // Product quantity is insufficient, so delete order and return null
            } else {
                product.setQuantity(product.getQuantity() - orderItemDTO.getQuantity());
                productRepository.save(product);  // Save updated product quantity
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setId(orderItemDTO.getId());
            orderItem.setQuantity(orderItemDTO.getQuantity());
            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItemRepository.save(orderItem);

            // Initialize orderItemOptionDTOList to avoid NullPointerException
            List<OrderItemOptionDTO> orderItemOptionDTOList = orderItemDTO.getOrderItemOptions();
            if (orderItemOptionDTOList == null) {
                orderItemOptionDTOList = new ArrayList<>();  // Initialize to an empty list if null
            }

            List<OrderItemOption> orderItemOptions = new ArrayList<>();
            List<Discount> discounts = discountRepository.findAllByProduct_Id(orderItem.getProduct().getId());

            for (OrderItemOptionDTO orderItemOptionDTO : orderItemOptionDTOList) {
                OrderItemOption orderItemOption = new OrderItemOption();
                orderItemOption.setId(orderItemOptionDTO.getId());
                orderItemOption.setOrderItem(orderItem);
                orderItemOption.setFoodOption(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get());

                if (orderItemOptionDTO.getTypeId() == 2) {
                    orderItemOption.setQuantity(orderItem.getQuantity());
                    BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice();
                    orderItem.setUnitPrice(unitPrice);
                    orderItemOption.setTotalPrice(BigDecimal.ZERO);

                    if (discounts != null && !discounts.isEmpty()) {
                        for (Discount discount : discounts) {
                            if (discount.getStatus().equals(Status.ACTIVE)) {
                                orderItem.setDiscountValue(discount.getDiscount_percentage());
                            }else orderItem.setDiscountValue(BigDecimal.ZERO);
                        }
                    } else {
                        orderItem.setDiscountValue(BigDecimal.ZERO);
                    }
                    orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(orderItemDTO.getQuantity())).multiply(BigDecimal.ONE.subtract(orderItem.getDiscountValue())));
                    orderItemTotal = orderItemTotal.add(orderItem.getTotalPrice());
                } else {
                    orderItemOption.setQuantity(orderItemOptionDTO.getQuantity());
                    BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice();
                    orderItemOption.setUnitPrice(unitPrice);
                    orderItemOption.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(orderItemOption.getQuantity())));
                    orderItemTotal = orderItemTotal.add(orderItemOption.getTotalPrice());
                }

                orderItemOptionRepository.save(orderItemOption);
                orderItemOptions.add(orderItemOption);
            }

            orderItem.setTotalPrice(orderItemTotal);
            orderItemRepository.save(orderItem);
            orderItemList.add(orderItem);

            order.setShop(shopRepository.findByProduct(orderItem.getProduct().getId()));
            orderTotal = orderTotal.add(orderItemTotal);
        }

        order.setTotal(orderTotal);
        orderRepository.save(order);
        return order;
    }


    @Override
    public OrderDTO viewOrder(Long id) throws IOException {
        Order order = orderRepository.findById(id).get(); // Load tất cả đơn hàng
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setShopPhone(shopRepository.findById(order.getShop().getId()).get().getPhone());
        orderDTO.setId(order.getId());
        orderDTO.setAddress(order.getShippingAddress());
        orderDTO.setTotal(order.getTotal());
        orderDTO.setOwnerId(order.getOwner().getId());
        orderDTO.setPhone(userRepository.findById(orderDTO.getOwnerId()).get().getPhone());
        orderDTO.setOwnerName(userRepository.findById(orderDTO.getOwnerId()).get().getProfile().getName());
        orderDTO.setReason(order.getReason());
        orderDTO.setPaymentProof(order.getPaymentProof());
        orderDTO.setOrderCode(order.getOrderCode());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
        orderDTO.setShipperName(order.getShipper() != null ?userRepository.findById(order.getShipper().getId()).get().getProfile().getName(): null);
        orderDTO.setShipperPhone(order.getShipper() != null ?userRepository.findById(order.getShipper().getId()).get().getPhone(): null);
        orderDTO.setStatus(order.getStatus().toString());
        orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
        orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);
        orderDTO.setTotal(order.getTotal());
        List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setId(orderItem.getId());
            orderItemDTO.setQuantity(orderItem.getQuantity());
            orderItemDTO.setProductId(orderItem.getProduct().getId());
            orderItemDTO.setTotal(orderItem.getTotalPrice());
            orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
            orderItemDTO.setOrderId(order.getId());
            orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
            orderDTO.setShopId(shopRepository.findByProduct(orderItemDTO.getProductId()).getId());
            orderDTO.setShopAddress(shopRepository.findByProduct(orderItemDTO.getProductId()).getAddress());
            orderDTO.setImage(shopRepository.findByProduct(orderItemDTO.getProductId()).getBackgroundImage());
            orderItemDTO.setProductName(productRepository.findById(orderItemDTO.getProductId()).get().getName());
            orderItemDTO.setImage(productRepository.findById(orderItemDTO.getProductId()).get().getImage());
            List<OrderItemOptionDTO> orderItemOptionDTOList = new ArrayList<>();
            for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                OrderItemOptionDTO orderItemOptionDTO = new OrderItemOptionDTO();
                orderItemOptionDTO.setId(orderItemOption.getId());
                orderItemOptionDTO.setOptionId(orderItemOption.getFoodOption().getId());
                orderItemOptionDTO.setQuantity(orderItemOption.getQuantity());
                orderItemOptionDTO.setOrderItemId(orderItem.getId());
                orderItemOptionDTO.setTypeId(orderItemOption.getFoodOption().getType().getId());
                orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());
            }
            orderItemDTO.setOrderItemOptions(orderItemOptionDTOList);
            orderItemDTOList.add(orderItemDTO);
        }
        orderDTO.setOrderItem(orderItemDTOList);
        return orderDTO;
    }



    @Override
    public Page<OrderDTO> findAllByOwnerAndStatus(Long id, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findALlByOwner_IdAndStatusOrderByCreatedAt(id, status, pageable);
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
    public List<ShipPaymentDTO> findAllShipPaymentByShopId(Long shopId) {
        List<OrderStatus> validStatuses = Arrays.asList(
                OrderStatus.DELIVERED,
                OrderStatus.RETURNED,
                OrderStatus.RETURN_REJECTED
        );

        List<Order> orders = orderRepository.findAllByShopIdAndStatusIn(shopId, validStatuses);

        Map<Long, BigDecimal> shipPaymentMap = new HashMap<>();
        Map<Long, String> shipperNameMap = new HashMap<>();

        for (Order order : orders) {
            if (order.getShipper() != null
                    && order.getDeliveryMethod() != null
                    && order.getDeliveryMethod().getFee() != null) {

                Long shipperId = order.getShipper().getId();
                BigDecimal fee = order.getDeliveryMethod().getFee();

                shipPaymentMap.put(shipperId,
                        shipPaymentMap.getOrDefault(shipperId, BigDecimal.ZERO).add(fee));

                shipperNameMap.putIfAbsent(shipperId, order.getShipper().getProfile().getName());
            }
        }

        List<ShipPaymentDTO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : shipPaymentMap.entrySet()) {
            ShipPaymentDTO dto = new ShipPaymentDTO();
            dto.setShipperId(entry.getKey());
            dto.setAmount(entry.getValue());
            dto.setShipperName(shipperNameMap.get(entry.getKey()));
            result.add(dto);
        }

        return result;
    }


    @Override
    public Page<OrderDTO> findAllByShopAndPending(Long id, Pageable pageable) {
        Page<Order> orderList=orderRepository.findOrdersByShopIdAndStatus(id, OrderStatus.PENDING,pageable);
        return toDTO(orderList,pageable);
    }

    @Override
    public double orderChangeRate() {
        LocalDateTime startOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double a=orderRepository.countOrderByMonth(startOfLastMonth, endOfLastMonth);
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double b=orderRepository.countOrderByMonth(startOfThisMonth, endOfThisMonth);
        return (b-a)/a*100;
    }

    @Override
    public long countPendingOrder() {
        return orderRepository.countReturnPendingOrders();
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
        if(status.equals(OrderStatus.DELIVERED)){
            User user=userRepository.findById(userId).get();
            if (user.getRole().getId() == 4L) {
                user.setDeliveryStatus(DeliveryStatus.AVAILABLE);
                userRepository.save(user);
            }
        }
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void acceptShipping(Long id,Long userId) {
        Order order=orderRepository.findById(id).get();
        order.setShipper(userRepository.findById(userId).get());
        User user=userRepository.findById(userId).get();
        user.setDeliveryStatus(DeliveryStatus.PICKED_UP);
        order.setStatus(OrderStatus.SHIPPING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> findAllByShipper(Long id, Pageable pageable,OrderStatus status) {
        Page<Order> orders = orderRepository.findAllByShipper_IdAndStatusOrderByCreatedAt(id,status,pageable);
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
            if (order.getShipper() == null) {
                int startIndex = shipperIndex;
                boolean assigned = false;

                do {
                    User shipper = availableShippers.get(shipperIndex);

                    // Tránh gán shipper là chính chủ đơn hàng
                    if (!shipper.getId().equals(order.getOwner().getId())) {
                        shipper.setDeliveryStatus(DeliveryStatus.ASSIGNED);
                        userRepository.save(shipper);

                        order.setShipper(shipper);
                        orderRepository.save(order);

                        assigned = true;
                    }

                    shipperIndex = (shipperIndex + 1) % totalShippers;
                } while (!assigned && shipperIndex != startIndex);
            }
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
            image.setType(typesRepository.findById(4L).get());
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
        List<Image> imageList=imageRepository.findAllByRelatedIdAndType_Id(orderDTO.getId(),4L);
        List<ImageDTO> imageDTOList=new ArrayList<>();
        for (Image image : imageList) {
            ImageDTO imageDTO=new ImageDTO();
            imageDTO.setUrl(image.getUrl());
            imageDTO.setRelatedId(orderDTO.getId());
            imageDTO.setId(image.getId());
            imageDTO.setOwnerId(image.getOwnerId());
            imageDTO.setTypeId(4L);
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
    public void updatePrice(Long id, BigDecimal price) {
        Order order=orderRepository.findById(id).get();
        order.setTotal(price);
        orderRepository.save(order);
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
        List<OrderItemDTO> orderItems = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItems.add(orderMapper.toItemDTO(orderItem));
        }
        orderDTO.setOrderItem(orderItems);
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
    public CountDTO countOrderByStatus(Long id  ) {
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

    @Override
    public Long countAllByStatus(OrderStatus status) {
        Long count = orderRepository.countAllByStatus((status));
        return count;
    }

    public List<CountByYearDTO> getOrderCountByStatusAndYear(OrderStatus status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // Chuyển startDate thành LocalDateTime
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // Chuyển endDate thành LocalDateTime

        // Thực hiện truy vấn để lấy kết quả
        List<Object[]> results = orderRepository.countOrdersByStatusAndYear(status, startDateTime, endDateTime);

        // Danh sách chứa các đối tượng CountByYearDTO
        List<CountByYearDTO> countByYearDTOS = new ArrayList<>();

        // Duyệt qua các kết quả trả về từ truy vấn
        for (Object[] result : results) {
            CountByYearDTO countByYearDTO = new CountByYearDTO();

            // Lấy năm từ kết quả truy vấn (result[0] chứa năm)
            int year = (Integer) result[0];
            countByYearDTO.setYear(year);

            // Lấy số lượng đơn hàng từ kết quả truy vấn (result[1] chứa số lượng đơn hàng)
            Long count = (Long) result[1];
            countByYearDTO.setCount(count);

            // Thêm đối tượng vào danh sách kết quả
            countByYearDTOS.add(countByYearDTO);
        }

        // Trả về danh sách kết quả
        return countByYearDTOS;
    }
    public List<CountByMonthDTO> getOrderCountByStatusAndMonth(OrderStatus status, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 2023-12-31T23:59:59
        List<Object[]> results = orderRepository.countOrdersByStatusAndMonth(status, startDateTime, endDateTime);
        List<CountByMonthDTO> countByMonthDTOS = new ArrayList<>();

        // Chuyển đổi kết quả thành danh sách DTO
        for (Object[] result : results) {
            CountByMonthDTO countByMonthDTO = new CountByMonthDTO();

            // Get the month and year from the query result
            int month = (Integer) result[0]; // Month
            int year = (Integer) result[1]; // Year

            // Format the month as yyyy/MM
            String formattedMonth = String.format("%d/%02d", month, year); // Example: 2025/03
            countByMonthDTO.setMonth(formattedMonth);

            // Get the order count (it could be either Long or Integer)
            if (result[2] instanceof Long) {
                countByMonthDTO.setCount((Long) result[2]);
            } else {
                countByMonthDTO.setCount(((Integer) result[2]).longValue());
            }

            // Add the DTO to the result list
            countByMonthDTOS.add(countByMonthDTO);
        }

        return countByMonthDTOS;
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

    public List<Object[]> countShopOrdersByStatusAndDay(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId) {
        // Convert startDate and endDate to LocalDateTime for query parameters
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 2023-01-01T23:59:59
        List<Object[]> results = orderRepository.countShopOrdersByStatusAndDay(status, startDateTime, endDateTime, shopId);

        // Convert the results to return LocalDate
        return results.stream()
                .map(result -> {
                    LocalDate date = ((java.sql.Date) result[0]).toLocalDate(); // Convert java.sql.Date to LocalDate
                    return new Object[]{date, result[1]};
                })
                .collect(Collectors.toList());
    }
    public List<Object[]> countShopOrdersByStatusAndMonth(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return orderRepository.countShopOrdersByStatusAndMonth(status, startDateTime, endDateTime, shopId);
    }

    // Đếm số lượng đơn hàng theo năm cho cửa hàng cụ thể
    public List<Object[]> countShopOrdersByStatusAndYear(OrderStatus status, LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return orderRepository.countShopOrdersByStatusAndYear(status, startDateTime, endDateTime, shopId);
    }
    public Map<Date, BigDecimal> calculateShopRevenueByDay(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByDay(startDateTime, endDateTime, shopId);

        Map<Date, BigDecimal> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            Date date=(java.sql.Date) result[0];
            Long shopIdResult = (Long) result[1];
            BigDecimal revenue = (BigDecimal) result[2];
            shopRevenue.put(date, revenue);
        }
        return shopRevenue;
    }



    // Tính tổng doanh thu theo tháng
    public Map<String, BigDecimal> calculateShopRevenueByMonth(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByMonth(startDateTime, endDateTime, shopId);

        Map<String, BigDecimal> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            String monthYear = result[0] + "-" + String.format("%02d", result[1]);  // Format: YYYY-MM
            BigDecimal revenue = (BigDecimal) result[2];
            shopRevenue.put(monthYear, revenue);
        }
        return shopRevenue;
    }

    // Tính tổng doanh thu theo năm
    public Map<Integer, BigDecimal> calculateShopRevenueByYear(LocalDate startDate, LocalDate endDate, Long shopId) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = orderRepository.calculateShopRevenueByYear(startDateTime, endDateTime, shopId);

        Map<Integer, BigDecimal> shopRevenue = new HashMap<>();
        for (Object[] result : results) {
            Integer year = (Integer) result[0];
            BigDecimal revenue = (BigDecimal) result[1];
            shopRevenue.put(year, revenue);
        }
        return shopRevenue;
    }
}



