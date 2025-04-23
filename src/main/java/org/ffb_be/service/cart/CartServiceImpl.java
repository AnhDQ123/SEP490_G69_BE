package org.ffb_be.service.cart;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.cart.CartDTO;
import org.ffb_be.dto.cart.CartItemDTO;
import org.ffb_be.dto.cart.CartItemOptionDTO;
import org.ffb_be.dto.discount.DiscountDTO2;
import org.ffb_be.dto.product.FoodOptionDTO;
import org.ffb_be.entity.*;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.CartStatus;
import org.ffb_be.utils.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final ShopRepository shopRepository;
    private final DiscountRepository discountRepository;


    @Transactional
    @Override
    public void save(CartDTO cartDTO) throws IOException {
        // Nhóm các sản phẩm theo Shop
        Map<Long, List<CartItemDTO>> shopCartItems = new HashMap<>();
        for (CartItemDTO cartItemDTO : cartDTO.getCartItemDTOList()) {
            Long shopId;
            if(cartItemDTO.getProductId()!=null){
                shopId= shopRepository.findByProduct(cartItemDTO.getProductId()).getId();
            }else shopId=null;
            shopCartItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(cartItemDTO);
        }

        for (Map.Entry<Long, List<CartItemDTO>> entry : shopCartItems.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemDTO> cartItemDTOList = entry.getValue();

            // Lấy hoặc tạo mới Cart cho user với shop tương ứng
            Cart cart = getOrCreateCart(cartDTO.getUserId(), shopId);
            BigDecimal totalCartPrice = cart.getTotal() != null ? cart.getTotal() : BigDecimal.ZERO;

            for (CartItemDTO cartItemDTO : cartItemDTOList) {
                Product product = productRepository.findById(cartItemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // Kiểm tra xem đã có CartItem nào của sản phẩm này với foodOptionId tương ứng chưa
                List<CartItem> existingItems = cartItemRepository.findAllByCartIdAndProductId(cart.getId(), product.getId());
                List<Long> incomingFoodOptionIds = cartItemDTO.getCartItemOptionDTOList().stream()
                        .map(CartItemOptionDTO::getOptionId) // Dùng foodOptionId thay vì optionId
                        .sorted()
                        .collect(Collectors.toList());

                CartItem matchedItem = null;
                for (CartItem existingItem : existingItems) {
                    // Lấy danh sách foodOptionIds từ các CartItemOption đã lưu
                    List<Long> existingFoodOptionIds = cartItemOptionRepository.findAllByCartItemId(existingItem.getId())
                            .stream()
                            .map(o -> o.getFoodOption().getId()) // Dùng foodOptionId
                            .sorted()
                            .collect(Collectors.toList());
                    if (existingFoodOptionIds.equals(incomingFoodOptionIds)) {
                        matchedItem = existingItem;
                        break;
                    }
                }

                CartItem item;
                if (matchedItem != null) {
                    // Nếu đã tồn tại, cập nhật số lượng và thời gian
                    item = matchedItem;
                    item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
                    item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                    item.setUpdatedAt(LocalDateTime.now());

                    // Lấy các CartItemOption cũ của CartItem này
                    List<CartItemOption> existingOptions = cartItemOptionRepository.findAllByCartItemId(item.getId());

                    // Cộng dồn số lượng của các tùy chọn cũ vào số lượng mới
                    for (CartItemOption existingOption : existingOptions) {
                        // Cộng số lượng của các tùy chọn cũ vào tùy chọn mới
                        for (CartItemOptionDTO newOptionDTO : cartItemDTO.getCartItemOptionDTOList()) {
                            if (existingOption.getFoodOption().getId().equals(newOptionDTO.getOptionId())) {
                                existingOption.setQuantity(existingOption.getQuantity() + newOptionDTO.getQuantity());
                                existingOption.setTotalPrice(existingOption.getUnitPrice().multiply(new BigDecimal(existingOption.getQuantity())));
                                cartItemOptionRepository.save(existingOption);
                                break;
                            }
                        }
                    }
                } else {
                    // Tạo mới CartItem và lưu trước để có id persist cho CartItemOption
                    item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(cartItemDTO.getQuantity());
                    item.setCreatedAt(LocalDateTime.now());
                    item = cartItemRepository.save(item);  // Lưu ngay để đảm bảo item không còn là transient
                    BigDecimal basePrice = BigDecimal.ZERO; // Lấy giá cơ bản của sản phẩm
                    BigDecimal extrasTotal = BigDecimal.ZERO;  // Tổng giá của các option phụ
                    BigDecimal discountTotal = BigDecimal.ZERO; // Tổng discount cho các option có typeId == 2
                    BigDecimal totalItemPrice = BigDecimal.ZERO;

                    // Tính toán giá của các option phụ
                    for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
                        FoodOption foodOption = foodOptionRepository.findById(optionDTO.getOptionId())
                                .orElseThrow(() -> new RuntimeException("Food Option not found"));
                        BigDecimal optionUnitPrice = foodOption.getPrice();

                        CartItemOption option = new CartItemOption();
                        option.setCartItem(item);  // Lưu ý: item đã được persist
                        option.setQuantity(optionDTO.getQuantity());
                        option.setCreatedAt(LocalDateTime.now());
                        option.setUpdatedAt(LocalDateTime.now());
                        option.setUnitPrice(optionUnitPrice);
                        option.setTotalPrice(optionUnitPrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
                        option.setFoodOption(foodOption);
                        cartItemOptionRepository.save(option);

                        // Tính discount cho các option chính (typeId == 2)
                        List<Discount> discountList = discountRepository.findAllByProduct_Id(product.getId());
                        if (optionDTO.getTypeId() == 2) {
                            BigDecimal bestDiscount = BigDecimal.ZERO;
                            for (Discount discount : discountList) {
                                if (discount.getStatus().equals(Status.ACTIVE)) {
                                    bestDiscount = discount.getDiscount_percentage();
                                }
                            }
                            basePrice = optionUnitPrice;
                            discountTotal = discountTotal.add(basePrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
                            discountTotal = discountTotal.multiply(BigDecimal.ONE.subtract(bestDiscount));
                        } else {
                            // Cộng thêm giá của các option phụ
                            extrasTotal = extrasTotal.add(optionUnitPrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
                        }
                    }


                    totalItemPrice = discountTotal.add(extrasTotal);

                    // Cập nhật lại giá cho CartItem
                    item.setUnitPrice(basePrice);
                    item.setTotalPrice(totalItemPrice);
                    cartItemRepository.save(item);

                    totalCartPrice = totalCartPrice.add(totalItemPrice);
                }

                // TÍNH TOÁN GIÁ

            }

            // Cập nhật tổng giá cho Cart
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
    public List<CartDTO> findByUserId(Long userId) {
        // Lấy danh sách giỏ hàng của user có trạng thái PENDING
        List<Cart> cartList = cartRepository.findAllByOwner_IdAndStatus(userId, CartStatus.PENDING);
        List<CartDTO> cartDTOList = new ArrayList<>();

        for (Cart cart : cartList) {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setId(cart.getId());
            cartDTO.setUserId(cart.getOwner().getId());

            // Giả sử tất cả sản phẩm trong giỏ hàng đều thuộc cùng 1 shop,
            // Lấy thông tin shop từ sản phẩm đầu tiên trong giỏ hàng
            if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                CartItem firstItem = cart.getCartItems().get(0);
                Shop shop = shopRepository.findByProduct(firstItem.getProduct().getId());
                if (shop != null) {
                    cartDTO.setShopId(shop.getId());
                    cartDTO.setShopName(shop.getName());
                }
            }

            BigDecimal cartTotal = BigDecimal.ZERO;
            List<CartItemDTO> cartItemDTOList = new ArrayList<>();

            // Kiểm tra xem có CartItem không trước khi tiếp tục
            if (cart.getCartItems() != null) {
                for (CartItem cartItem : cart.getCartItems()) {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    cartItemDTO.setId(cartItem.getId());
                    cartItemDTO.setCartId(cart.getId());
                    Long productId = cartItem.getProduct().getId();
                    cartItemDTO.setProductId(productId);


                    // Lấy thông tin sản phẩm
                    productRepository.findById(productId).ifPresent(product -> {
                        cartItemDTO.setProductName(product.getName());
                        cartItemDTO.setImage(product.getImage());
                    });

                    // Lấy giá của CartItem và đảm bảo giá trị không phải null
                    BigDecimal unitPrice = cartItem.getUnitPrice();


                    BigDecimal cartItemTotal = BigDecimal.ZERO;
                    List<CartItemOptionDTO> cartItemOptionDTOList = new ArrayList<>();
                    if (cartItem.getCartItemOptions() != null) {
                        for (CartItemOption option : cartItem.getCartItemOptions()) {
                            CartItemOptionDTO optionDTO = new CartItemOptionDTO();
                            optionDTO.setId(option.getId());
                            Long optionId = option.getFoodOption().getId();
                            optionDTO.setOptionId(optionId);
                            optionDTO.setOptionName(option.getFoodOption().getName());
                            optionDTO.setImage(option.getFoodOption().getImage());
                            optionDTO.setCartItemId(cartItem.getId());
                            optionDTO.setQuantity(option.getQuantity());

                            // Lấy đơn giá của option từ FoodOption
                            Optional<FoodOption> foodOptionOpt = foodOptionRepository.findById(optionId);
                            if (!foodOptionOpt.isPresent()) {
                                continue;
                            }
                            FoodOption foodOption = foodOptionOpt.get();
                            if (foodOption.getType().getId() == 2) {
                                cartItemDTO.setQuantity(option.getQuantity());
                                optionDTO.setQuantity(option.getQuantity());
                            } else {
                                // Với các option khác, tính giá theo số lượng option
                                optionDTO.setQuantity(option.getQuantity());
                            }

                            optionDTO.setPrice(option.getUnitPrice());
                            optionDTO.setTotalPrice(option.getTotalPrice());
                            optionDTO.setTypeId(foodOption.getType().getId());
                            cartItemOptionDTOList.add(optionDTO);
                        }
                    }
                    cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);
                    cartItemDTOList.add(cartItemDTO);
                    cartTotal = cartTotal.add(cartItemTotal);
                }
            }

            cartDTO.setDiscountPrice(cartTotal);
            cartDTO.setCartItemDTOList(cartItemDTOList);
            cartDTOList.add(cartDTO);
        }
        return cartDTOList;
    }


    @Override
    public CartDTO findById(Long id) {
        Cart cart = cartRepository.findById(id).get();
        CartDTO cartDTO = new CartDTO();
        cartDTO.setUserId(cart.getOwner().getId());
        cartDTO.setId(cart.getId());
        List<CartItemDTO> cartItemDTOList = new ArrayList<>();
        List<CartItem> cartItems = cart.getCartItems();
        for (CartItem cartItem : cartItems) {
            CartItemDTO cartItemDTO = new CartItemDTO();
            cartItemDTO.setProductId(cartItem.getProduct().getId());
            cartDTO.setShopId(shopRepository.findByProduct(cartItem.getProduct().getId()).getId());
            cartDTO.setShopName(shopRepository.findById(cartDTO.getShopId()).get().getName());
            cartItemDTO.setQuantity(cartItem.getQuantity());
            cartItemDTO.setPrice(cartItem.getUnitPrice());
            cartItemDTO.setTotalPrice(cartItem.getTotalPrice());
            cartItemDTO.setId(cartItem.getId());
            cartItemDTO.setCartId(cart.getId());
            List<CartItemOptionDTO> cartItemOptionDTOList = new ArrayList<>();
            List<CartItemOption> cartItemOptions = cartItem.getCartItemOptions();
            for (CartItemOption cartItemOption : cartItemOptions) {
                CartItemOptionDTO cartItemOptionDTO = new CartItemOptionDTO();
                cartItemOptionDTO.setOptionId(cartItemOption.getFoodOption().getId());
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
    public void increaseOptionQuantity(Long id) {
        CartItemOption cartItemOption = cartItemOptionRepository.findById(id).get();
        cartItemOption.setQuantity(cartItemOption.getQuantity() + 1);
        cartItemOptionRepository.save(cartItemOption);
    }

    @Override
    public void decreaseOptionQuantity(Long id) {
        CartItemOption cartItemOption = cartItemOptionRepository.findById(id).get();
        cartItemOption.setQuantity(cartItemOption.getQuantity() - 1);
        cartItemOptionRepository.save(cartItemOption);
        if (cartItemOption.getQuantity() == 0) {
            cartItemOptionRepository.delete(cartItemOption);
        }
    }

    @Override
    public void changeSize(Long oldId, Long newId) {
        CartItemOption cartItemOption = cartItemOptionRepository.findById(oldId).get();
        FoodOption foodOption = foodOptionRepository.findById(newId).get();
        cartItemOption.setFoodOption(foodOption);
        cartItemOption.setUnitPrice(foodOption.getPrice());
        cartItemOptionRepository.save(cartItemOption);
    }

    @Override
    public void deleteItemFromCart(Long cartId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found")); // Ném ngoại lệ khi không tìm thấy sản phẩm

        CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cartId, id);
        if (cartItem != null) {
            List<CartItemOption> cartItemOption = cartItemOptionRepository.findAllByCartItemId(cartItem.getId());
            cartItemOptionRepository.deleteAll(cartItemOption);
            cartItemRepository.delete(cartItem);
        } else {
            throw new RuntimeException("CartItem not found for the product.");
        }
        Cart cart = cartRepository.findById(cartId).get();
        if (cart.getCartItems().isEmpty()) {
            cartRepository.delete(cart);
        }

    }

    @Override
    public void deleteOptionFromCart(Long cartId, Long optionId) {
        CartItemOption cartItemOption = cartItemOptionRepository.findByCartItem_IdAndFoodOption_Id(cartId, optionId);
        cartItemOptionRepository.delete(cartItemOption);
    }

    @Override
    public void addOptionToItem(Long cartItemId, Long optionId) {
        FoodOption foodOption = foodOptionRepository.findById(optionId).get();
        CartItem cartItem = cartItemRepository.findById(cartItemId).get();
        int a = 0;
        for (CartItemOption cartItemOption1 : cartItem.getCartItemOptions()) {
            if (cartItemOption1.getFoodOption().getId().equals(optionId)) {
                cartItemOption1.setQuantity(cartItemOption1.getQuantity() + 1);
                cartItemOption1.setTotalPrice(foodOption.getPrice().multiply(BigDecimal.valueOf(cartItemOption1.getQuantity())));
                cartItemOptionRepository.save(cartItemOption1);
                a = 1;
            }
        }
        if (a != 0) {
            CartItemOption cartItemOption = new CartItemOption();
            cartItemOption.setFoodOption(foodOption);
            cartItemOption.setQuantity(1);
            cartItemOption.setUnitPrice(foodOption.getPrice());
            cartItemOption.setTotalPrice(foodOption.getPrice());
            cartItemOption.setCartItem(cartItem);
            cartItemOptionRepository.save(cartItemOption);
        }
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

