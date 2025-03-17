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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, FoodOptionRepository foodOptionRepository, OrderItemRepository orderItemRepository, OrderItemOptionRepository orderItemOptionRepository, UserRepository userRepository, DeliveryMethodRepository deliveryMethodRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.foodOptionRepository = foodOptionRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemOptionRepository = orderItemOptionRepository;
        this.userRepository = userRepository;
        this.deliveryMethodRepository = deliveryMethodRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void save(OrderDTO orderDTO) throws IOException {
        Order order = new Order();
        BigDecimal total=BigDecimal.ZERO;
        OrderStatus pending = OrderStatus.PENDING;
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        List<OrderItemDTO> orderItemDTOList=orderDTO.getOrderItem();
        List<OrderItem> orderItem=new ArrayList<>();
        for(OrderItemDTO orderItemDTO:orderItemDTOList){
            OrderItem orderItem1 = new OrderItem();
            orderItem1.setOrder(order);
            orderItem1.setProduct(productRepository.findById(orderItemDTO.getProductId()).get());
            orderItem1.setQuantity(orderItemDTO.getQuantity());
            orderItem1.setCreatedAt(LocalDateTime.now());
            List<OrderItemOptionDTO> orderItemOptionDTOList=orderItemDTO.getOrderItemOptions();
            List<OrderItemOption> orderItemOptionList=new ArrayList<>();
            for(OrderItemOptionDTO orderItemOptionDTO:orderItemOptionDTOList){
                OrderItemOption orderItemOption = new OrderItemOption();
                if(orderItemOptionDTO.getTypeId()== 2){
                    orderItem1.setUnitPrice(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice());
                    if(orderItem1.getProduct().getDiscount().getId()!=null&&orderItem1.getProduct().getDiscount().getEnd_date().isAfter(LocalDate.now())){
                        orderItem1.setTotalPrice(orderItem1.getUnitPrice().multiply(new BigDecimal(orderItem1.getQuantity())).multiply(orderItem1.getProduct().getDiscount().getDiscount_percentage()));
                    }else
                        orderItem1.setTotalPrice(orderItem1.getUnitPrice().multiply(new BigDecimal(orderItem1.getQuantity())));
                }
                orderItemOption.setOrderItem(orderItem1);
                orderItemOption.setQuantity(orderItemOptionDTO.getQuantity());
                orderItemOption.setUnitPrice(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get().getPrice());
                orderItemOption.setTotalPrice( orderItemOption.getUnitPrice().multiply(BigDecimal.valueOf(orderItemOption.getQuantity())).multiply(new BigDecimal(orderItem1.getQuantity())));
                orderItemOption.setFoodOption(foodOptionRepository.findById(orderItemOptionDTO.getOptionId()).get());
                total=total.add(orderItemOption.getTotalPrice());
                orderItemOptionList.add(orderItemOption);
            }
            total=total.add(orderItem1.getTotalPrice());
            orderItemOptionRepository.saveAll(orderItemOptionList);
            orderItem1.setOrderItemOptions(orderItemOptionList);
            orderItem.add(orderItem1);
        }

        orderItemRepository.saveAll(orderItem);
        order.setOwner(userRepository.findById(orderDTO.getOwnerId()).get());
        order.setShipper(userRepository.findById(orderDTO.getShipperId()).get());
        order.setStatus(pending);
        order.setDeliveryMethod(deliveryMethodRepository.findById(orderDTO.getShipMethodId()).get());
        total=total.add(deliveryMethodRepository.findById(orderDTO.getShipMethodId()).get().getFee());
        order.setTotal(total);
        order.setPaymentMethod(paymentRepository.findById(orderDTO.getPaymentMethodId()).get());
//        if(orderDTO.getVoucherId()!=null&&){
//
//        }
//        order.setVoucher();
        orderRepository.save(order);
    }
}
