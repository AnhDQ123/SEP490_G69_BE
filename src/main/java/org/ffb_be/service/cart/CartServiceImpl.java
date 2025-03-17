package org.ffb_be.service.cart;

import org.ffb_be.dto.cart.CartDTO;
import org.ffb_be.dto.cart.CartItemDTO;
import org.ffb_be.dto.cart.CartItemOptionDTO;
import org.ffb_be.entity.Cart;
import org.ffb_be.entity.CartItem;
import org.ffb_be.entity.CartItemOption;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.CartStatus;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FoodOptionRepository foodOptionRepository;
    public CartServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository, CartItemOptionRepository cartItemOptionRepository, UserRepository userRepository, CartRepository cartRepository, FoodOptionRepository foodOptionRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.foodOptionRepository = foodOptionRepository;
    }


    @Override
    public void save(CartDTO cartDTO) throws IOException {
        Cart cart = new Cart();
        CartStatus pending = CartStatus.PENDING;
        cart.setOwner(userRepository.findById(cartDTO.getUserId()).get());
        cart.setStatus(pending);
        BigDecimal total=BigDecimal.ZERO;
        cart.setTotal(BigDecimal.ZERO);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItem =new ArrayList<>();
        List<CartItemDTO> cartItemDTO =cartDTO.getCartItemDTOList();
        for(CartItemDTO cartItemDTO1:cartItemDTO){
            CartItem item = new CartItem();
            cartItemRepository.save(item);
            item.setCart(cart);
            item.setQuantity(cartItemDTO1.getQuantity());
            item.setProduct(productRepository.findById(cartItemDTO1.getProductId()).get());
            item.setCreatedAt(LocalDateTime.now());
            List<CartItemOption> cartItemOptions = new ArrayList<>();
            List<CartItemOptionDTO> cartItemOptionDTO =cartItemDTO1.getCartItemOptionDTOList();
            for(CartItemOptionDTO cartItemOptionDTO1:cartItemOptionDTO){
                CartItemOption cartItemOption = new CartItemOption();
                if(cartItemOptionDTO1.getTypeId()== 2){
                    item.setUnitPrice(foodOptionRepository.findById(cartItemOptionDTO1.getOptionId()).get().getPrice());
                    item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                }
                cartItemOption.setCartItem(item);
                cartItemOption.setQuantity(cartItemOptionDTO1.getQuantity());
                cartItemOption.setUnitPrice(foodOptionRepository.findById(cartItemOptionDTO1.getOptionId()).get().getPrice());
                cartItemOption.setTotalPrice( cartItemOption.getUnitPrice().multiply(BigDecimal.valueOf(cartItemOption.getQuantity())).multiply(new BigDecimal(item.getQuantity())));
                cartItemOption.setFoodOption(foodOptionRepository.findById(cartItemOptionDTO1.getOptionId()).get());
                total=total.add(cartItemOption.getTotalPrice());
                cartItemOptions.add(cartItemOption);
            }
            total=total.add(item.getTotalPrice());
            cartItemOptionRepository.saveAll(cartItemOptions);
            item.setCartItemOptions(cartItemOptions);
            cartItem.add(item);
        }
        cart.setTotal(total);
        cartItemRepository.saveAll(cartItem);
        cartRepository.save(cart);
    }

}
//@Override
//public void save(CartDTO cartDTO) throws IOException {
//    Cart cart = new Cart();
//    List<CartItem> cartItem =cartItemRepository.findAllByCart_Id(cartDTO.getId());
//    List<CartItemDTO> cartItemDTOS = new ArrayList<>();
//    CartStatus pending = CartStatus.PENDING;
//    cart.setOwner(userRepository.findById(cartDTO.getUserId()).get());
//    cart.setStatus(pending);
//    cart.setTotal(BigDecimal.ZERO);
//    cart.setCreatedAt(LocalDateTime.now());
//    cartRepository.save(cart);
//
//    for (CartItem cart1 : cartItem) {
//        CartItemDTO cartItemDTO = new CartItemDTO();
//        cartItemDTO.setId(cart1.getId());
//        cartItemDTO.setProductId(cart1.getProduct().getId());
//        cartItemDTO.setPrice(cart1.getUnitPrice());
//        cartItemDTO.setTotalPrice(cart1.getTotalPrice());
//        cartItemDTO.setQuantity(cart1.getQuantity());
//        List<CartItemOption> cartItemOptions = cartItemOptionRepository.findAllByCartItem_Id(cart1.getId());
//        cartItemOptionRepository.saveAll(cartItemOptions);
//        List<CartItemOptionDTO> cartItemOptionDTOS = new ArrayList<>();
//        for (CartItemOption cartItemOption : cartItemOptions) {
//            CartItemOptionDTO cartItemOptionDTO = new CartItemOptionDTO();
//            cartItemOptionDTO.setId(cartItemOption.getId());
//            cartItemOptionDTO.setCartItemId(cartItemOption.getCartItem().getId());
//            cartItemOptionDTO.setOptionId(cartItemOption.getFoodOption().getId());
//            cartItemOptionDTO.setPrice(cartItemOption.getUnitPrice());
//            cartItemOptionDTO.setQuantity(cartItemOption.getQuantity());
//            cartItemOptionDTO.setTotalPrice(cartItemOption.getTotalPrice());
//            cartItemOptionDTOS.add(cartItemOptionDTO);
//        }
//        cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOS);
//        cartItemDTOS.add(cartItemDTO);
//        cartItemRepository.saveAll(cartItem);
//    }
//
//}

