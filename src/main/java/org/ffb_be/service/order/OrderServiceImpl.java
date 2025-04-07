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
import org.ffb_be.utils.enums.OrderStatus;
import org.ffb_be.utils.enums.Status;
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
                List<Discount> discount=discountRepository.findAllByProduct_Id((orderItem.getId()));
                if(discount!=null) {
                    List<DiscountDTO2> discountDTOs=new ArrayList<>();
                    for (Discount discount1:discount) {
                        DiscountDTO2 discountDTO=new DiscountDTO2();;
                        discountDTO.setAmount(discount1.getDiscount_percentage());
                        discountDTO.setId(discount1.getId());
                        discountDTO.setStartDate(discount1.getStartDate());
                        discountDTO.setEndDate(discount1.getEndDate());
                        discountDTO.setStatus(discount1.getStatus().toString());
                        discountDTOs.add(discountDTO);
                    }
                    orderItemDTO.setDiscount(discountDTOs);
                }else {
                    orderItemDTO.setDiscount(null);
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
                for(Discount discount1:discount){
                    if(discount1.getStatus().equals(Status.ACTIVE)){
                        orderItemDTO.setTotal(orderItemTotal.multiply(BigDecimal.ONE.subtract(discount1.getDiscount_percentage())));
                    }
                }
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
    public Order save(OrderDTO orderDTO) throws IOException {
         Order order = new Order();
         order.setOwner(userRepository.findById(orderDTO.getOwnerId()).get());
         order.setPaymentMethod(paymentRepository.findById(orderDTO.getPaymentMethodId()).get());
         order.setDeliveryMethod(deliveryMethodRepository.findById(orderDTO.getShipMethodId()).get());
         orderRepository.save(order);
         BigDecimal orderTotal = BigDecimal.ZERO;
         List<OrderItemDTO> orderItemDTOList = orderDTO.getOrderItem();
         List<OrderItem> orderItemList = new ArrayList<>();
         for (OrderItemDTO orderItemDTO : orderItemDTOList) {
             BigDecimal orderItemTotal = BigDecimal.ZERO;
             Product product = productRepository.findById(orderItemDTO.getProductId()).get();
             if(orderItemDTO.getQuantity()>product.getQuantity()){
                 orderRepository.delete(order);
                 return null;
             }else {
                 product.setQuantity(product.getQuantity() - orderItemDTO.getQuantity());
                 productRepository.save(product);
             }
             OrderItem orderItem = new OrderItem();
             orderItem.setId(orderItemDTO.getId());
             orderItem.setQuantity(orderItemDTO.getQuantity());
             orderItem.setProduct(productRepository.findById(orderItemDTO.getProductId()).get());
             orderItem.setOrder(order);
             orderItem.setCreatedAt(LocalDateTime.now());
             orderItemRepository.save(orderItem);
             List<Discount> discount=discountRepository.findAllByProduct_Id((orderItem.getId()));
             List<OrderItemOptionDTO> orderItemOptionDTOList = orderItemDTO.getOrderItemOptions();
             List<OrderItemOption> orderItemOptions = new ArrayList<>();
             for(OrderItemOptionDTO orderItemOptionDTO:orderItemOptionDTOList){
                 OrderItemOption orderItemOption = new OrderItemOption();
                 orderItemOption.setId(orderItemOptionDTO.getId());
                 orderItemOption.setOrderItem(orderItem);
                 orderItemOption.setFoodOption(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get());
                 if (orderItemOptionDTO.getTypeId() == 2) {
                     orderItemOption.setQuantity(orderItem.getQuantity());
                     BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice();
                     orderItem.setUnitPrice(unitPrice);
                     orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(orderItemDTO.getQuantity())));
                     orderItemOption.setTotalPrice(BigDecimal.ZERO);
                     orderItemTotal=orderItemTotal.add(orderItem.getTotalPrice());
                 }else{
                     orderItemOption.setQuantity(orderItemOptionDTO.getQuantity());
                     BigDecimal unitPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice();
                     orderItemOption.setUnitPrice(unitPrice);
                     orderItemOption.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(orderItemOption.getQuantity())));
                     orderItemTotal=orderItemTotal.add(orderItemOption.getTotalPrice());
                 }
                 orderItemOptionRepository.save(orderItemOption);
                 orderItemOptions.add(orderItemOption);
             }
             if(discount!=null && !discount.isEmpty()){
                 for(Discount discount1:discount){
                     if(discount1.getStatus().equals(Status.ACTIVE)){
                         orderItem.setDiscountValue(discount1.getDiscount_percentage());
                     }
                 }
             }else orderItem.setDiscountValue(BigDecimal.ZERO);
             orderItemTotal=orderItemTotal.multiply(BigDecimal.ONE.subtract(orderItem.getDiscountValue()));
             orderItem.setTotalPrice(orderItemTotal);
             orderItemRepository.save(orderItem);
             orderItemList.add(orderItem);
             order.setShop(shopRepository.findByProduct(orderItem.getProduct().getId()));
             orderTotal=orderTotal.add(orderItemTotal);
         }
         order.setTotal(orderTotal);
         orderRepository.save(order);
         return order;
    }

    @Override
    public OrderDTO viewOrder(Long id) throws IOException {
        Order order = orderRepository.findById(id).get(); // Load tất cả đơn hàng
        OrderDTO orderDTO = new OrderDTO();

        orderDTO.setId(order.getId());
        orderDTO.setAddress(order.getShippingAddress());
        orderDTO.setTotal(order.getTotal());
        orderDTO.setOwnerId(order.getOwner().getId());
        orderDTO.setPhone(userRepository.findById(orderDTO.getOwnerId()).get().getPhone());
        orderDTO.setOwnerName(userRepository.findById(orderDTO.getOwnerId()).get().getProfile().getName());
        orderDTO.setReason(order.getReason());
        orderDTO.setPaymentProof(order.getPaymentProof());

        if(order.getVoucher() != null) {
            orderDTO.setVoucherId(order.getVoucher().getId());
            orderDTO.setVoucherAmount(order.getVoucher().getDiscountValue());
        } else {
            orderDTO.setVoucherAmount(BigDecimal.ZERO);
        }

        orderDTO.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
        orderDTO.setStatus(order.getStatus().toString());
        orderDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
        orderDTO.setShipMethodId(order.getDeliveryMethod() != null ? order.getDeliveryMethod().getId() : null);

        List<OrderItemDTO> orderItemDTOList = new ArrayList<>();

        // Duyệt qua từng OrderItem
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setId(orderItem.getId());
            orderItemDTO.setQuantity(orderItem.getQuantity());
            orderItemDTO.setProductId(orderItem.getProduct().getId());
            orderItemDTO.setTotal(orderItem.getTotalPrice());

            // Lấy danh sách discount của sản phẩm (lưu ý: có thể cần dùng orderItem.getProduct().getId())

            orderItemDTO.setCreatedAt(orderItem.getCreatedAt());
            orderItemDTO.setOrderId(order.getId());

            // Lấy thông tin shop dựa vào product
            orderDTO.setShopName(shopRepository.findByProduct(orderItemDTO.getProductId()).getName());
            orderDTO.setShopId(shopRepository.findByProduct(orderItemDTO.getProductId()).getId());
            orderDTO.setShopAddress(shopRepository.findByProduct(orderItemDTO.getProductId()).getAddress());
            orderDTO.setImage(shopRepository.findByProduct(orderItemDTO.getProductId()).getBackgroundImage());

            orderItemDTO.setProductName(productRepository.findById(orderItemDTO.getProductId()).get().getName());
            orderItemDTO.setImage(productRepository.findById(orderItemDTO.getProductId()).get().getImage());

            List<OrderItemOptionDTO> orderItemOptionDTOList = new ArrayList<>();
            // Khai báo biến tổng riêng cho từng loại
            BigDecimal totalType1 = BigDecimal.ZERO; // Chỉ cộng vào
            BigDecimal totalType2 = BigDecimal.ZERO; // Sẽ áp dụng discount sau

            // Duyệt qua các OrderItemOption
            for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                OrderItemOptionDTO orderItemOptionDTO = new OrderItemOptionDTO();
                orderItemOptionDTO.setId(orderItemOption.getId());
                orderItemOptionDTO.setOptionId(orderItemOption.getFoodOption().getId());
                orderItemOptionDTO.setQuantity(orderItemOption.getQuantity());
                orderItemOptionDTO.setOrderItemId(orderItem.getId());

                // Lấy giá của FoodOption
                BigDecimal optionPrice = foodOptionRepository.findById(orderItemOptionDTO.getOptionId())
                        .orElseThrow(() -> new RuntimeException("Food Option not found"))
                        .getPrice();
                orderItemOptionDTO.setPrice(optionPrice);
                orderItemOptionDTO.setTypeId(orderItemOption.getFoodOption().getType().getId());
                orderItemOptionDTO.setOptionName(orderItemOption.getFoodOption().getName());
                orderItemOptionDTO.setImage(orderItemOption.getFoodOption().getImage());

                // Tính tổng cho tùy chọn hiện tại
                BigDecimal optionTotal = optionPrice.multiply(BigDecimal.valueOf(orderItemOptionDTO.getQuantity()));
                orderItemOptionDTO.setTotal(optionTotal);
                orderItemOptionDTOList.add(orderItemOptionDTO);

                // Phân loại tùy chọn theo typeId
                if (orderItemOptionDTO.getTypeId() == 2) {
                    totalType2 = totalType2.add(optionTotal);
                } else { // Giả sử typeId = 1
                    totalType1 = totalType1.add(optionTotal);
                }
            }
            BigDecimal discountedTotalType2=totalType2;
            // Tính hệ số discount chỉ áp dụng cho type2
            BigDecimal discountFactor = BigDecimal.ONE;
            List<Discount> discountList = discountRepository.findAllByProduct_Id(orderItem.getProduct().getId());
            if (discountList != null) {
                for (Discount discount : discountList) {
                    if (discount.getStatus().equals(Status.ACTIVE)) {
                        discountFactor = discountFactor.multiply(BigDecimal.ONE.subtract(discount.getDiscount_percentage()));
                        discountedTotalType2= discountedTotalType2.multiply(discountFactor);
                    }
                }
            }

            // Tổng của OrderItem = tổng không discount (type1) + tổng discount (type2 đã nhân discount)
            BigDecimal orderItemTotal = totalType1.add(discountedTotalType2);
            orderItemDTO.setTotal(orderItemTotal);
            orderItemDTO.setOrderItemOptions(orderItemOptionDTOList);
            orderItemDTOList.add(orderItemDTO);
        }

        // Tính tổng đơn hàng từ tất cả OrderItem
        BigDecimal finalOrderTotal = orderItemDTOList.stream()
                .map(OrderItemDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Áp dụng voucher (nếu có) cho toàn đơn hàng
        orderDTO.setTotal(finalOrderTotal.multiply(BigDecimal.ONE.subtract(orderDTO.getVoucherAmount())));
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
    public List<ShipPaymentDTO> findAllShipPaymentByShopId(Long shopId) {
        // Lấy tất cả các đơn hàng của shop có trạng thái DELIVERED
        List<Order> orders = orderRepository.findAllByShop_IdAndStatus(shopId, OrderStatus.DELIVERED);

        // Map để gom nhóm shipperId với tổng phí giao hàng
        Map<Long, BigDecimal> shipPaymentMap = new HashMap<>();
        // Map để lưu shipperName tương ứng với shipperId
        Map<Long, String> shipperNameMap = new HashMap<>();

        for (Order order : orders) {
            // Kiểm tra shipper và deliveryMethod có hợp lệ không
            if (order.getShipper() != null
                    && order.getDeliveryMethod() != null
                    && order.getDeliveryMethod().getFee() != null) {
                Long shipperId = order.getShipper().getId();
                BigDecimal fee = order.getDeliveryMethod().getFee();

                // Cộng dồn phí giao hàng cho shipper tương ứng
                shipPaymentMap.put(shipperId, shipPaymentMap.getOrDefault(shipperId, BigDecimal.ZERO).add(fee));
                // Lưu tên shipper nếu chưa có
                if (!shipperNameMap.containsKey(shipperId)) {
                    shipperNameMap.put(shipperId, order.getShipper().getProfile().getName());
                }
            }
        }

        // Tạo danh sách DTO từ các Map đã có
        List<ShipPaymentDTO> shipPaymentDTOList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : shipPaymentMap.entrySet()) {
            ShipPaymentDTO dto = new ShipPaymentDTO();
            dto.setShipperId(entry.getKey());
            dto.setAmount(entry.getValue());
            dto.setShipperName(shipperNameMap.get(entry.getKey()));
            shipPaymentDTOList.add(dto);
        }

        return shipPaymentDTOList;
    }

    @Override
    public Page<OrderDTO> findAllByShopAndPending(Long id, Pageable pageable) {
        Page<Order> orderList=orderRepository.findOrdersByShopIdAndStatus(id, OrderStatus.PENDING,pageable);
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



