package org.ffb_be.service.cart;

import org.ffb_be.dto.cart.CartDTO;
import org.ffb_be.dto.cart.CartItemDTO;
import org.ffb_be.dto.cart.CartItemOptionDTO;
import org.ffb_be.entity.*;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.CartStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @Transactional
    @Override
    public void save(CartDTO cartDTO) throws IOException {
        Map<Long, List<CartItemDTO>> shopCartItems = new HashMap<>();

        // Nhóm các sản phẩm theo Shop
        for (CartItemDTO cartItemDTO : cartDTO.getCartItemDTOList()) {
            Long shopId = shopRepository.findByProduct(cartItemDTO.getProductId()).getId();
            shopCartItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(cartItemDTO);
        }

        for (Map.Entry<Long, List<CartItemDTO>> entry : shopCartItems.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemDTO> cartItemDTOList = entry.getValue();

            // Lấy hoặc tạo Cart mới cho User và Shop
            Cart cart = getOrCreateCart(cartDTO.getUserId(), shopId);
            BigDecimal totalCartPrice = cart.getTotal() != null ? cart.getTotal() : BigDecimal.ZERO;

            for (CartItemDTO cartItemDTO : cartItemDTOList) {
                // Tìm CartItem đã có trong giỏ hàng
                List<CartItem> existingItems = cartItemRepository
                        .findAllByCartIdAndProductId(cart.getId(), cartItemDTO.getProductId());

                // Danh sách optionId của sản phẩm đang thêm
                List<Long> incomingOptionIds = cartItemDTO.getCartItemOptionDTOList()
                        .stream()
                        .map(CartItemOptionDTO::getOptionId)
                        .sorted()
                        .collect(Collectors.toList());

                CartItem matchedItem = null;

                // Kiểm tra nếu có CartItem đã tồn tại với các option trùng khớp
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

                // Nếu CartItem đã có, chỉ cần cập nhật
                CartItem item;
                if (matchedItem != null) {
                    item = matchedItem;
                    item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
                    item.setUpdatedAt(LocalDateTime.now());

                    // Xóa các option cũ để thêm lại option mới
                    cartItemOptionRepository.deleteAllByCartItemId(item.getId());
                } else {
                    // Nếu không có CartItem, tạo mới
                    item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(productRepository.findById(cartItemDTO.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found")));
                    item.setQuantity(cartItemDTO.getQuantity());
                    item.setCreatedAt(LocalDateTime.now());
                }

                item.setUnitPrice(BigDecimal.ZERO);  // Set tạm thời, sẽ tính lại sau
                item.setTotalPrice(BigDecimal.ZERO); // Set tạm thời, sẽ tính lại sau
                cartItemRepository.save(item);

                BigDecimal itemTotal = BigDecimal.ZERO;

                // Thêm CartItemOption nếu chưa có
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

                    // Nếu là option chính (typeId == 2), cập nhật giá sản phẩm
                    if (optionDTO.getTypeId() == 2) {
                        item.setUnitPrice(unitPrice);
                        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                    }

                    itemTotal = itemTotal.add(totalOptionPrice);
                    cartItemOptionRepository.save(option);
                }

                totalCartPrice = totalCartPrice.add(itemTotal);
                cartItemRepository.save(item); // Cập nhật lại CartItem
            }

            // Cập nhật tổng tiền của Cart
            cart.setTotal(totalCartPrice);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }
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
                    cartDTO.setShopName(shopRepository.findById(cartDTO.getShopId()).get().getName());
                    cartItemDTO.setProductName(productRepository.findById(cartItemDTO.getProductId()).get().getName());
                    cartItemDTO.setImage(productRepository.findById(cartItemDTO.getProductId()).get().getImage());
                    cartItemDTO.setQuantity(cartItem.getQuantity());
                    cartItemDTO.setPrice(cartItem.getUnitPrice());
                    cartItemDTO.setTotalPrice(cartItem.getTotalPrice());
                    cartItemDTO.setId(cartItem.getId());
                    cartItemDTO.setCartId(cart.getId());
                    List<CartItemOptionDTO> cartItemOptionDTOList=new ArrayList<>();
                    List<CartItemOption> cartItemOptions=cartItem.getCartItemOptions();
                    for(CartItemOption cartItemOption:cartItemOptions){
                        CartItemOptionDTO cartItemOptionDTO=new CartItemOptionDTO();
                        cartItemOptionDTO.setOptionId(cartItemOption.getId());
                        cartItemOptionDTO.setOptionName(cartItemOption.getFoodOption().getName());
                        cartItemOptionDTO.setImage(cartItemOption.getFoodOption().getImage());
                        cartItemOptionDTO.setQuantity(cartItemOption.getQuantity());
                        cartItemOptionDTO.setCartItemId(cartItemDTO.getId());
                        cartItemOptionDTO.setPrice(cartItemOption.getUnitPrice());
                        cartItemOptionDTO.setTotalPrice(cartItemOption.getTotalPrice());
                        cartItemOptionDTO.setTypeId(cartItemOption.getFoodOption().getType().getId());
                        cartItemOptionDTOList.add(cartItemOptionDTO);
                    }
                    cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                    cartItemDTOList.add(cartItemDTO);
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
                cartItemDTO.setPrice(cartItem.getUnitPrice());
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
                    cartItemOptionDTO.setPrice(cartItemOption.getUnitPrice());
                    cartItemOptionDTO.setTotalPrice(cartItemOption.getTotalPrice());
                    cartItemOptionDTO.setTypeId(cartItemOption.getFoodOption().getType().getId());
                    cartItemOptionDTOList.add(cartItemOptionDTO);
                }
                cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                cartItemDTOList.add(cartItemDTO);
            }
            cartDTO.setCartItemDTOList(cartItemDTOList);
        return cartDTO;
    }

    @Override
    public void deleteItemFromCart(Long cartId, Long id) {
        Product product=productRepository.findById(cartId).get();
        CartItem cartItem=cartItemRepository.findByProduct(product);
        cartItemRepository.delete(cartItem);

    }

    @Override
    public void deleteOptionFromCart(Long cartId, Long optionId) {
        CartItemOption cartItemOption=cartItemOptionRepository.findByCartItem_IdAndFoodOption_Id(cartId,optionId);
        cartItemOptionRepository.delete(cartItemOption);
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

