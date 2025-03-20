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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final ShopRepository shopRepository;
    public CartServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository, CartItemOptionRepository cartItemOptionRepository, UserRepository userRepository, CartRepository cartRepository, FoodOptionRepository foodOptionRepository, ShopRepository shopRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.foodOptionRepository = foodOptionRepository;
        this.shopRepository = shopRepository;
    }


    @Override
    public void save(CartDTO cartDTO) throws IOException {
        // Nhóm sản phẩm theo Shop
        Map<Long, List<CartItemDTO>> shopCartItems = new HashMap<>();

        for (CartItemDTO cartItemDTO : cartDTO.getCartItemDTOList()) {
            Long shopId = shopRepository.findByProduct(cartItemDTO.getProductId()).getId();
            shopCartItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(cartItemDTO);
        }

        // Duyệt từng shop để tạo Cart riêng
        for (Map.Entry<Long, List<CartItemDTO>> entry : shopCartItems.entrySet()) {

            List<CartItemDTO> cartItemDTOList = entry.getValue();

            // Tạo giỏ hàng mới cho từng shop
            Cart cart = new Cart();
            cart.setOwner(userRepository.findById(cartDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
            cart.setStatus(CartStatus.PENDING);
            cart.setTotal(BigDecimal.ZERO);
            cart.setCreatedAt(LocalDateTime.now());
            cartRepository.save(cart); // Lưu Cart trước để có ID

            List<CartItem> cartItems = new ArrayList<>();
            BigDecimal totalCartPrice = BigDecimal.ZERO;

            // Duyệt qua từng CartItem của shop này
            for (CartItemDTO cartItemDTO : cartItemDTOList) {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setQuantity(cartItemDTO.getQuantity());
                item.setProduct(productRepository.findById(cartItemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found")));
                item.setCreatedAt(LocalDateTime.now());

                // **Gán đầy đủ thông tin trước khi lưu**
                cartItems.add(item);
            }

            // **Lưu tất cả CartItems vào DB trước**
            cartItemRepository.saveAll(cartItems);

            List<CartItemOption> cartItemOptions = new ArrayList<>();
            for (CartItem item : cartItems) {
                for (CartItemOptionDTO cartItemOptionDTO : cartItemDTOList
                        .stream().filter(dto -> dto.getProductId().equals(item.getProduct().getId()))
                        .findFirst().orElseThrow(() -> new RuntimeException("CartItemDTO not found"))
                        .getCartItemOptionDTOList()) {

                    CartItemOption cartItemOption = new CartItemOption();

                    // Nếu typeId == 2 thì gán giá từ `FoodOption`
                    if (cartItemOptionDTO.getTypeId() == 2) {
                        BigDecimal unitPrice = foodOptionRepository.findById(cartItemOptionDTO.getOptionId())
                                .orElseThrow(() -> new RuntimeException("Food Option not found"))
                                .getPrice();
                        item.setUnitPrice(unitPrice);
                        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                    }

                    cartItemOption.setCartItem(item);
                    cartItemOption.setQuantity(cartItemOptionDTO.getQuantity());
                    cartItemOption.setCreatedAt(LocalDateTime.now());
                    cartItemOption.setUnitPrice(foodOptionRepository.findById(cartItemOptionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found")).getPrice());
                    cartItemOption.setTotalPrice(cartItemOption.getUnitPrice()
                            .multiply(BigDecimal.valueOf(cartItemOption.getQuantity()))
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
                    cartItemOption.setFoodOption(foodOptionRepository.findById(cartItemOptionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found")));
                    totalCartPrice = totalCartPrice.add(cartItemOption.getTotalPrice());
                    cartItemOptions.add(cartItemOption);
                }
            }

            // **Lưu tất cả CartItemOptions vào DB**
            cartItemOptionRepository.saveAll(cartItemOptions);

            // Cập nhật total của Cart
            cart.setTotal(totalCartPrice);
            cartRepository.save(cart);
        }
    }

    @Override
    public List<CartDTO> findByUserId(Long id) {
            CartStatus status = CartStatus.PENDING;
            List<Cart> cartList=cartRepository.findAllByOwner_IdAndStatus(id,status);
            List<CartDTO> cartDTOList=new ArrayList<>();
            BigDecimal cartTotalPrice=BigDecimal.ZERO;
            for(Cart cart:cartList){
                CartDTO cartDTO=new CartDTO();
                cartDTO.setUserId(cart.getOwner().getId());
                cartDTO.setId(cart.getId());
                cartDTO.setPrice(cart.getTotal());
                List<CartItemDTO> cartItemDTOList=new ArrayList<>();
                List<CartItem> cartItems=cart.getCartItems();
                BigDecimal itemTotalPrice=BigDecimal.ZERO;
                for(CartItem cartItem:cartItems){
                    CartItemDTO cartItemDTO=new CartItemDTO();
                    cartItemDTO.setProductId(cartItem.getProduct().getId());
                    cartDTO.setShopId(shopRepository.findByProduct(cartItem.getProduct().getId()).getId());
                    cartDTO.setShopName(shopRepository.findById(cartDTO.getShopId()).get().getName());
                    cartItemDTO.setProductName(productRepository.findById(cartItemDTO.getProductId()).get().getName());
                    cartItemDTO.setImage(productRepository.findById(cartItemDTO.getProductId()).get().getImage());
                    cartItemDTO.setQuantity(cartItem.getQuantity());
                    cartItemDTO.setId(cartItem.getId());
                    cartItemDTO.setCartId(cart.getId());
                    List<CartItemOptionDTO> cartItemOptionDTOList=new ArrayList<>();
                    List<CartItemOption> cartItemOptions=cartItem.getCartItemOptions();
                    BigDecimal ItemOptionTotalPrice=BigDecimal.ZERO;
                    for(CartItemOption cartItemOption:cartItemOptions){
                        CartItemOptionDTO cartItemOptionDTO=new CartItemOptionDTO();
                        cartItemOptionDTO.setOptionId(cartItemOption.getId());
                        cartItemOptionDTO.setOptionName(cartItemOption.getFoodOption().getName());
                        cartItemOptionDTO.setImage(cartItemOption.getFoodOption().getImage());
                        cartItemOptionDTO.setQuantity(cartItemOption.getQuantity());
                        cartItemOptionDTO.setCartItemId(cartItemDTO.getId());
                        if(cartItemOption.getFoodOption().getType().getId()==2){
                            cartItemDTO.setPrice(cartItemOption.getFoodOption().getPrice());
                            cartItemOptionDTO.setQuantity(cartItemDTO.getQuantity());
                        }
                        cartItemOptionDTO.setPrice(cartItemOption.getFoodOption().getPrice());
                        cartItemOptionDTO.setTotalPrice(cartItemOptionDTO.getPrice().multiply(BigDecimal.valueOf(cartItemOption.getQuantity())));
                        cartItemOptionDTO.setTypeId(cartItemOption.getFoodOption().getType().getId());
                        cartItemOptionDTOList.add(cartItemOptionDTO);
                        if(cartItemOption.getFoodOption().getType().getId()!=2){
                            ItemOptionTotalPrice=ItemOptionTotalPrice.add(cartItemOptionDTO.getTotalPrice());
                        }
                    }
                    itemTotalPrice=itemTotalPrice.add(ItemOptionTotalPrice);
                    cartItemDTO.setTotalPrice(itemTotalPrice);
                    cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                    cartItemDTOList.add(cartItemDTO);
                    cartTotalPrice=cartTotalPrice.add(itemTotalPrice);
                }
                cartDTO.setPrice(cartTotalPrice);
                cartDTO.setCartItemDTOList(cartItemDTOList);
                cartDTOList.add(cartDTO);
            }
        return cartDTOList;
    }

    @Override
    public CartDTO findById(Long id) {
        Cart cart=cartRepository.findById(id).get();
        CartDTO cartDTO=new CartDTO();
            cartDTO.setUserId(cart.getOwner().getId());
            cartDTO.setId(cart.getId());
            cartDTO.setPrice(cart.getTotal());
            List<CartItemDTO> cartItemDTOList=new ArrayList<>();
            List<CartItem> cartItems=cart.getCartItems();
            for(CartItem cartItem:cartItems){
                CartItemDTO cartItemDTO=new CartItemDTO();
                cartItemDTO.setProductId(cartItem.getProduct().getId());
                cartDTO.setShopId(shopRepository.findByProduct(cartItem.getProduct().getId()).getId());
                cartItemDTO.setQuantity(cartItem.getQuantity());
                cartItemDTO.setTotalPrice(cartItem.getTotalPrice());
                cartItemDTO.setId(cartItem.getId());
                cartItemDTO.setCartId(cart.getId());
                List<CartItemOptionDTO> cartItemOptionDTOList=new ArrayList<>();
                List<CartItemOption> cartItemOptions=cartItem.getCartItemOptions();
                for(CartItemOption cartItemOption:cartItemOptions){
                    CartItemOptionDTO cartItemOptionDTO=new CartItemOptionDTO();
                    cartItemOptionDTO.setOptionId(cartItemOption.getId());
                    cartItemOptionDTO.setQuantity(cartItemOption.getQuantity());
                    cartItemOptionDTO.setCartItemId(cartItemDTO.getId());
                    cartItemOptionDTO.setTypeId(cartItemOption.getFoodOption().getType().getId());
                    cartItemOptionDTOList.add(cartItemOptionDTO);
                }
                cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                cartItemDTOList.add(cartItemDTO);
            }
            cartDTO.setCartItemDTOList(cartItemDTOList);
        return cartDTO;
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

