package org.ffb_be.service.cart;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.cart.CartDTO;
import org.ffb_be.dto.cart.CartItemDTO;
import org.ffb_be.dto.cart.CartItemOptionDTO;
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
import java.util.function.Function;
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
            Long shopId = null;
            if (cartItemDTO.getProductId() != null) {
                shopId = shopRepository.findByProduct(cartItemDTO.getProductId()).getId();
            }
            shopCartItems.computeIfAbsent(shopId, k -> new ArrayList<>()).add(cartItemDTO);
        }

        // Lấy tất cả các sản phẩm và food options liên quan
        List<Long> productIds = cartDTO.getCartItemDTOList().stream()
                .map(CartItemDTO::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productRepository.findByIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> foodOptionIds = cartDTO.getCartItemDTOList().stream()
                .flatMap(item -> item.getCartItemOptionDTOList().stream())
                .map(CartItemOptionDTO::getOptionId)
                .collect(Collectors.toList());

        Map<Long, FoodOption> foodOptionMap = foodOptionRepository.findByIds(foodOptionIds)
                .stream()
                .collect(Collectors.toMap(FoodOption::getId, Function.identity()));

        for (Map.Entry<Long, List<CartItemDTO>> entry : shopCartItems.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemDTO> cartItemDTOList = entry.getValue();

            // Lấy hoặc tạo mới Cart cho user với shop tương ứng
            Cart cart = getOrCreateCart(cartDTO.getUserId(), shopId);
            BigDecimal totalCartPrice = cart.getTotal() != null ? cart.getTotal() : BigDecimal.ZERO;

            for (CartItemDTO cartItemDTO : cartItemDTOList) {
                Product product = productMap.get(cartItemDTO.getProductId());
                if (product == null) {
                    throw new RuntimeException("Product not found");
                }

                // Kiểm tra số lượng sản phẩm tồn kho
                if (cartItemDTO.getQuantity() > product.getQuantity()) {
                    throw new RuntimeException("Not enough stock for product " + product.getName());
                }

                // Kiểm tra xem đã có CartItem nào của sản phẩm này với foodOptionId tương ứng chưa
                List<CartItem> existingItems = cartItemRepository.findAllByCartIdAndProductId(cart.getId(), product.getId());
                List<Long> incomingFoodOptionIds = cartItemDTO.getCartItemOptionDTOList().stream()
                        .map(CartItemOptionDTO::getOptionId)
                        .sorted()
                        .collect(Collectors.toList());

                // Tìm kiếm item đã tồn tại
                CartItem matchedItem = findMatchedCartItem(existingItems, incomingFoodOptionIds);

                CartItem item;
                if (matchedItem != null) {
                    // Nếu đã tồn tại, cập nhật số lượng và thời gian
                    item = updateExistingItem(matchedItem, cartItemDTO, product, foodOptionMap);
                } else {
                    // Tạo mới CartItem và lưu trước để có id persist cho CartItemOption
                    item = createNewCartItem(cart, product, cartItemDTO, foodOptionMap);
                }
                totalCartPrice = totalCartPrice.add(item.getTotalPrice());
                // Cập nhật tổng giá cho Cart
                cart.setTotal(totalCartPrice);
                cart.setUpdatedAt(LocalDateTime.now());
                cartRepository.save(cart);
            }
        }
    }

    private CartItem findMatchedCartItem(List<CartItem> existingItems, List<Long> incomingFoodOptionIds) {
        for (CartItem existingItem : existingItems) {
            List<Long> existingFoodOptionIds = cartItemOptionRepository.findAllByCartItemId(existingItem.getId())
                    .stream()
                    .map(o -> o.getFoodOption().getId())
                    .sorted()
                    .toList();
            if (existingFoodOptionIds.equals(incomingFoodOptionIds)) {
                return existingItem;
            }
        }
        return null;
    }

    private CartItem updateExistingItem(CartItem matchedItem, CartItemDTO cartItemDTO, Product product, Map<Long, FoodOption> foodOptionMap) {
        BigDecimal itemWithExtra;
        // Cập nhật số lượng của CartItem
        CartItem item = new CartItem();
        matchedItem.setQuantity(matchedItem.getQuantity() + cartItemDTO.getQuantity());

        // Tính toán lại tổng giá trị của CartItem (bao gồm cả giá trị giảm giá)
        BigDecimal totalItemPrice = calculateTotalItemPrice(cartItemDTO, product, foodOptionMap);

        // Cập nhật lại giá trị tổng (totalPrice) của CartItem
        matchedItem.setTotalPrice(matchedItem.getTotalPrice().add(totalItemPrice));

        // Cập nhật thời gian sửa đổi
        matchedItem.setUpdatedAt(LocalDateTime.now());

        // Cập nhật các CartItemOption
        List<CartItemOption> existingOptions = cartItemOptionRepository.findAllByCartItemId(matchedItem.getId());
        for (CartItemOption existingOption : existingOptions) {
            for (CartItemOptionDTO newOptionDTO : cartItemDTO.getCartItemOptionDTOList()) {
                if (existingOption.getFoodOption().getId().equals(newOptionDTO.getOptionId())) {
                    // Cập nhật số lượng của CartItemOption
                    existingOption.setQuantity(existingOption.getQuantity() + newOptionDTO.getQuantity());

                    // Tính toán lại giá trị tổng của CartItemOption
                    existingOption.setTotalPrice(existingOption.getUnitPrice().multiply(new BigDecimal(existingOption.getQuantity())));
                    // Lưu lại CartItemOption sau khi cập nhật
                    cartItemOptionRepository.save(existingOption);
                    break;
                }
            }
        }
        itemWithExtra = calculateTotalItemExtraPrice(cartItemDTO, foodOptionMap);
        item.setTotalPrice(totalItemPrice.add(itemWithExtra));
        return item;
    }

    private CartItem createNewCartItem(Cart cart, Product product, CartItemDTO cartItemDTO, Map<Long, FoodOption> foodOptionMap) {
        CartItem item = new CartItem();
        CartItem temp = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(cartItemDTO.getQuantity());
        item.setCreatedAt(LocalDateTime.now());

        BigDecimal basePrice = BigDecimal.ZERO;
        BigDecimal itemWithExtra;
        BigDecimal totalItemPrice = calculateTotalItemPrice(cartItemDTO, product, foodOptionMap);

        item.setUnitPrice(basePrice);
        item.setTotalPrice(totalItemPrice);
        cartItemRepository.save(item);

        // Xử lý tùy chọn (options) cho sản phẩm
        for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
            FoodOption foodOption = foodOptionMap.get(optionDTO.getOptionId());
            BigDecimal optionUnitPrice = foodOption.getPrice();
            if(optionDTO.getTypeId() == 2){
                item.setUnitPrice(optionUnitPrice);
            }
            CartItemOption option = new CartItemOption();
            option.setCartItem(item);
            option.setQuantity(optionDTO.getQuantity());
            option.setCreatedAt(LocalDateTime.now());
            option.setUpdatedAt(LocalDateTime.now());
            option.setUnitPrice(optionUnitPrice);
            option.setTotalPrice(optionUnitPrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
            option.setFoodOption(foodOption);
            cartItemOptionRepository.save(option);
        }
        itemWithExtra = calculateTotalItemExtraPrice(cartItemDTO, foodOptionMap);
        temp.setTotalPrice(item.getTotalPrice().add(itemWithExtra));
        return temp;
    }

    private BigDecimal calculateTotalItemPrice(CartItemDTO cartItemDTO, Product product, Map<Long, FoodOption> foodOptionMap) {
        BigDecimal discountTotal = BigDecimal.ZERO;

        for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
            FoodOption foodOption = foodOptionMap.get(optionDTO.getOptionId());
            BigDecimal optionUnitPrice = foodOption.getPrice();

            if (optionDTO.getTypeId() == 2) {
                discountTotal = applyDiscount(optionUnitPrice, optionDTO.getQuantity(), product);
            }
        }
        return discountTotal;
    }

    private BigDecimal calculateTotalItemExtraPrice(CartItemDTO cartItemDTO, Map<Long, FoodOption> foodOptionMap) {
        BigDecimal extrasTotal = BigDecimal.ZERO;

        for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
            FoodOption foodOption = foodOptionMap.get(optionDTO.getOptionId());
            BigDecimal optionUnitPrice = foodOption.getPrice();

            if (optionDTO.getTypeId() == 1) {
                extrasTotal = extrasTotal.add(optionUnitPrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
            }
        }
        return extrasTotal;
    }

    private BigDecimal applyDiscount(BigDecimal optionUnitPrice, int quantity, Product product) {
        BigDecimal bestDiscount = BigDecimal.ZERO;
        List<Discount> discountList = discountRepository.findAllByProduct_Id(product.getId());

        for (Discount discount : discountList) {
            if (discount.getStatus().equals(Status.ACTIVE)) {
                bestDiscount = discount.getDiscount_percentage();
            }
        }

        return optionUnitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(BigDecimal.ONE.subtract(bestDiscount));
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
            cartDTO.setStatus(cart.getStatus().toString());
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
                    cartItemDTO.setPrice(cartItem.getUnitPrice());
                    cartItemDTO.setTotalPrice(cartItem.getTotalPrice());
                    Long productId = cartItem.getProduct().getId();
                    cartItemDTO.setProductId(productId);
                    // Lấy thông tin sản phẩm
                    productRepository.findById(productId).ifPresent(product -> {
                        cartItemDTO.setProductName(product.getName());
                        cartItemDTO.setImage(product.getImage());
                    });

                    // Lấy giá của CartItem và đảm bảo giá trị không phải null

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
        // Lấy CartItemOption hiện tại
        CartItemOption cartItemOption = cartItemOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItemOption not found"));

        // Tăng số lượng
        cartItemOption.setQuantity(cartItemOption.getQuantity() + 1);

        // Cập nhật giá trị tổng của CartItemOption
        cartItemOption.setTotalPrice(cartItemOption.getUnitPrice().multiply(new BigDecimal(cartItemOption.getQuantity())));

        // Lưu lại CartItemOption
        cartItemOptionRepository.save(cartItemOption);

        CartItem cartItem = cartItemRepository.findById(cartItemOption.getCartItem().getId())
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Cart cart = cartItem.getCart();
        cart.setTotal(cart.getTotal().add(cartItemOption.getUnitPrice()));  // Cập nhật tổng giá giỏ hàng
        cartRepository.save(cart);  // Lưu lại Cart
    }


    @Override
    public void decreaseOptionQuantity(Long id) {
        // Lấy CartItemOption hiện tại
        CartItemOption cartItemOption = cartItemOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItemOption not found"));

        // Giảm số lượng
        cartItemOption.setQuantity(cartItemOption.getQuantity() - 1);

        // Cập nhật giá trị tổng của CartItemOption nếu số lượng lớn hơn 0
        if (cartItemOption.getQuantity() > 0) {
            cartItemOption.setTotalPrice(cartItemOption.getUnitPrice().multiply(new BigDecimal(cartItemOption.getQuantity())));
            cartItemOptionRepository.save(cartItemOption);
        } else {
            // Nếu số lượng bằng 0, xóa CartItemOption
            cartItemOptionRepository.delete(cartItemOption);
            return;
        }

        // Cập nhật giá trị tổng của CartItem (cập nhật lại tổng giá giỏ hàng)
        CartItem cartItem = cartItemRepository.findById(cartItemOption.getCartItem().getId())
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Cart cart = cartItem.getCart();
        cart.setTotal(cart.getTotal().subtract(cartItemOption.getUnitPrice()));  // Cập nhật tổng giá giỏ hàng
        cartRepository.save(cart);  // Lưu lại Cart
    }

    @Override
    public void changeSize(Long oldId, Long newId) {
        // Lấy CartItemOption cũ
        CartItemOption cartItemOption = cartItemOptionRepository.findById(oldId)
                .orElseThrow(() -> new RuntimeException("CartItemOption not found"));

        // Lấy FoodOption mới
        FoodOption foodOption = foodOptionRepository.findById(newId)
                .orElseThrow(() -> new RuntimeException("FoodOption not found"));

        // Cập nhật FoodOption và giá trị của CartItemOption
        cartItemOption.setFoodOption(foodOption);
        cartItemOption.setUnitPrice(foodOption.getPrice());

        // Tính toán lại giá trị tổng của CartItemOption (bao gồm cả giảm giá nếu có)
        BigDecimal totalOptionPrice = cartItemOption.getUnitPrice().multiply(new BigDecimal(cartItemOption.getQuantity()));
        // Cập nhật tổng giá trị của CartItemOption
        cartItemOption.setTotalPrice(totalOptionPrice);

        // Lưu lại CartItemOption sau khi cập nhật
        cartItemOptionRepository.save(cartItemOption);

        // Nếu FoodOption có giảm giá (typeId == 2), áp dụng giảm giá
        if (foodOption.getType().getId() == 2) {
            totalOptionPrice = applyDiscount(foodOption.getPrice(), cartItemOption.getQuantity(), cartItemOption.getFoodOption().getFood());
        }

        // Cập nhật giá trị tổng của CartItem
        CartItem cartItem = cartItemRepository.findById(cartItemOption.getCartItem().getId())
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        BigDecimal oldSizePrice = cartItem.getTotalPrice();
        cartItem.setUnitPrice(foodOption.getPrice());
        cartItem.setTotalPrice(totalOptionPrice);  // Cộng giá trị tổng của CartItemOption mới

        // Tính lại tổng giá của Cart (bao gồm tất cả các CartItem)
        Cart cart = cartItem.getCart();
        cart.setTotal(cart.getTotal().add(totalOptionPrice).subtract(oldSizePrice));  // Cập nhật tổng giá giỏ hàng
        cartRepository.save(cart);  // Lưu lại Cart
    }

    @Override
    public void deleteItemFromCart(Long cartId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found")); // Ném ngoại lệ khi không tìm thấy sản phẩm

        // Tìm CartItem theo cartId và productId
        CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cartId, id);
        if (cartItem != null) {
            BigDecimal itemTotalPrice = cartItem.getTotalPrice();
            BigDecimal sizeOptionTotalPrice = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            // Lấy tất cả CartItemOption liên quan đến CartItem này
            List<CartItemOption> cartItemOption = cartItemOptionRepository.findAllByCartItemId(cartItem.getId());
            BigDecimal optionTotalPrice = cartItemOption.stream().map(CartItemOption::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            // Xóa tất cả CartItemOption trước khi xóa CartItem
            cartItemOptionRepository.deleteAll(cartItemOption);

            // Lưu lại CartItemOption trước khi xóa CartItem
            cartItemRepository.delete(cartItem);


            Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
            cart.setTotal(cart.getTotal().subtract(optionTotalPrice.subtract(sizeOptionTotalPrice)).subtract(itemTotalPrice)); // Cập nhật lại tổng giá giỏ hàng
            cartRepository.save(cart); // Lưu lại Cart
        } else {
            throw new RuntimeException("CartItem not found for the product.");
        }
    }

    @Override
    public void deleteOptionFromCart(Long cartId, Long optionId) {
        // Tìm CartItemOption theo cartId và optionId
        CartItemOption cartItemOption = cartItemOptionRepository.findByCartItem_IdAndFoodOption_Id(cartId, optionId);

        if (cartItemOption != null) {
            // Lấy CartItem liên quan đến CartItemOption này
            CartItem cartItem = cartItemRepository.findById(cartItemOption.getCartItem().getId())
                    .orElseThrow(() -> new RuntimeException("CartItem not found"));
            BigDecimal optionTotalPrice = cartItemOption.getTotalPrice();
            // Xóa CartItemOption
            cartItemOptionRepository.delete(cartItemOption);

            Cart cart = cartItem.getCart();

            // Cập nhật lại tổng giá giỏ hàng
            cart.setTotal(cart.getTotal().subtract(optionTotalPrice));
            cartRepository.save(cart);
        } else {
            throw new RuntimeException("CartItemOption not found.");
        }
    }

    @Override
    public void addOptionToItem(Long cartItemId, Long optionId) {
        FoodOption foodOption = foodOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("FoodOption not found"));
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        boolean isOptionUpdated = false; // Biến để theo dõi xem có CartItemOption nào đã được cập nhật không

        // Cập nhật hoặc thêm CartItemOption
        for (CartItemOption cartItemOption : cartItem.getCartItemOptions()) {
            if (cartItemOption.getFoodOption().getId().equals(optionId)) {
                // Nếu tùy chọn đã tồn tại, chỉ cần tăng số lượng
                cartItemOption.setQuantity(cartItemOption.getQuantity() + 1);
                // Cập nhật lại giá trị tổng cho CartItemOption
                cartItemOption.setTotalPrice(foodOption.getPrice().multiply(BigDecimal.valueOf(cartItemOption.getQuantity())));
                cartItemOption.setUpdatedAt(LocalDateTime.now());
                cartItemOptionRepository.save(cartItemOption); // Lưu CartItemOption sau khi cập nhật
                isOptionUpdated = true;
            }
        }

        // Nếu CartItemOption không tồn tại, tạo mới CartItemOption
        if (!isOptionUpdated) {
            CartItemOption cartItemOption = new CartItemOption();
            cartItemOption.setFoodOption(foodOption);
            cartItemOption.setQuantity(1);
            cartItemOption.setUnitPrice(foodOption.getPrice());
            cartItemOption.setTotalPrice(foodOption.getPrice()); // Tổng giá của CartItemOption ban đầu bằng đơn giá
            cartItemOption.setCartItem(cartItem);
            cartItemOption.setCreatedAt(LocalDateTime.now());
            cartItemOptionRepository.save(cartItemOption);
        }

        Cart cart = cartItem.getCart();
        cart.setTotal(cart.getTotal().add(foodOption.getPrice())); // Cập nhật lại tổng giá của Cart
        cartRepository.save(cart); // Lưu lại Cart sau khi cập nhật tổng giá
    }

}