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
import java.util.*;
import java.util.stream.Collectors;

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
        Map<Long, List<CartItemDTO>> shopCartItems = new HashMap<>();

        for (CartItemDTO cartItemDTO : cartDTO.getCartItemDTOList()) {
            Long shopId = shopRepository.findByProduct(cartItemDTO.getProductId()).getId();
            shopCartItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(cartItemDTO);
        }

        for (Map.Entry<Long, List<CartItemDTO>> entry : shopCartItems.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemDTO> cartItemDTOList = entry.getValue();

            Cart cart = getOrCreateCart(cartDTO.getUserId(), shopId);
            BigDecimal totalCartPrice = cart.getTotal() != null ? cart.getTotal() : BigDecimal.ZERO;

            for (CartItemDTO cartItemDTO : cartItemDTOList) {
                List<CartItem> existingItems = cartItemRepository
                        .findAllByCartIdAndProductId(cart.getId(), cartItemDTO.getProductId());

                List<Long> incomingOptionIds = cartItemDTO.getCartItemOptionDTOList()
                        .stream()
                        .map(CartItemOptionDTO::getOptionId)
                        .sorted()
                        .collect(Collectors.toList());

                CartItem matchedItem = null;

                for (CartItem existingItem : existingItems) {
                    List<Long> existingOptionIds = cartItemOptionRepository
                            .findAllByCartItemId(existingItem.getId())
                            .stream()
                            .map(o -> o.getFoodOption().getId())
                            .sorted()
                            .collect(Collectors.toList());

                    if (existingOptionIds.equals(incomingOptionIds)) {
                        matchedItem = existingItem;
                        break;
                    }
                }

                CartItem item;
                if (matchedItem != null) {
                    item = matchedItem;
                    item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
                    item.setUpdatedAt(LocalDateTime.now());

                    // Xoá option cũ để insert lại (đơn giản hơn so sánh từng option)
                    cartItemOptionRepository.deleteAllByCartItemId(item.getId());
                } else {
                    item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(productRepository.findById(cartItemDTO.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found")));
                    item.setQuantity(cartItemDTO.getQuantity());
                    item.setCreatedAt(LocalDateTime.now());
                }

                item.setUnitPrice(BigDecimal.ZERO);
                item.setTotalPrice(BigDecimal.ZERO);
                cartItemRepository.save(item);

                BigDecimal itemTotal = BigDecimal.ZERO;

                for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
                    CartItemOption option = new CartItemOption();
                    option.setCartItem(item);
                    option.setQuantity(optionDTO.getQuantity());
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());

                    BigDecimal unitPrice = foodOptionRepository.findById(optionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found"))
                            .getPrice();

                    option.setUnitPrice(unitPrice);

                    BigDecimal totalOptionPrice = unitPrice
                            .multiply(BigDecimal.valueOf(option.getQuantity()))
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    option.setTotalPrice(totalOptionPrice);
                    option.setFoodOption(foodOptionRepository.findById(optionDTO.getOptionId())
                            .orElseThrow(() -> new RuntimeException("Food Option not found")));

                    if (optionDTO.getTypeId() == 2) {
                        item.setUnitPrice(unitPrice);
                        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                    }

                    itemTotal = itemTotal.add(totalOptionPrice);
                    cartItemOptionRepository.save(option);
                }

                totalCartPrice = totalCartPrice.add(itemTotal);
                cartItemRepository.save(item);
            }

            cart.setTotal(totalCartPrice);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }



    // Hỗ trợ: lấy hoặc tạo mới cart
    private Cart getOrCreateCart(Long userId, Long shopId) {
        return cartRepository.findByUserIdAndShopIdAndStatus(userId, shopId, CartStatus.PENDING)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setOwner(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found")));
                    newCart.setStatus(CartStatus.PENDING);
                    newCart.setTotal(BigDecimal.ZERO);
                    newCart.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });
    }



    @Override
    public List<CartDTO> findByUserId(Long id) {
            CartStatus status = CartStatus.PENDING;
            List<Cart> cartList=cartRepository.findAllByOwner_IdAndStatus(id,status);
            List<CartDTO> cartDTOList=new ArrayList<>();

            for(Cart cart:cartList){
                BigDecimal cartTotalPrice=BigDecimal.ZERO;
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
                        cartItemOptionDTO.setOptionId(cartItemOption.getFoodOption().getId());
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
                        ItemOptionTotalPrice=ItemOptionTotalPrice.add(cartItemOptionDTO.getTotalPrice());
                    }
                    itemTotalPrice=itemTotalPrice.add(ItemOptionTotalPrice);
                    cartItemDTO.setTotalPrice(itemTotalPrice);
                    cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                    cartItemDTOList.add(cartItemDTO);
                    cartTotalPrice=cartTotalPrice.add(itemTotalPrice);
                    cartDTO.setPrice(cartTotalPrice);
                }
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


