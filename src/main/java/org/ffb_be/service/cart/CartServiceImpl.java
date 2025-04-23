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
        // Cập nhật số lượng của CartItem
        CartItem item = new CartItem();
        matchedItem.setQuantity(matchedItem.getQuantity() + cartItemDTO.getQuantity());

        // Tính toán lại tổng giá trị của CartItem (bao gồm cả giá trị giảm giá)
        BigDecimal totalItemPrice = calculateTotalItemPrice(cartItemDTO, product, matchedItem.getUnitPrice(), foodOptionMap);

        // Cập nhật lại giá trị tổng (totalPrice) của CartItem
        item.setTotalPrice(totalItemPrice);
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
        return item;
    }

    private CartItem createNewCartItem(Cart cart, Product product, CartItemDTO cartItemDTO, Map<Long, FoodOption> foodOptionMap) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(cartItemDTO.getQuantity());
        item.setCreatedAt(LocalDateTime.now());

        BigDecimal basePrice = BigDecimal.ZERO;
        BigDecimal totalItemPrice = calculateTotalItemPrice(cartItemDTO, product, basePrice, foodOptionMap);

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

        return item;
    }

    private BigDecimal calculateTotalItemPrice(CartItemDTO cartItemDTO, Product product, BigDecimal basePrice, Map<Long, FoodOption> foodOptionMap) {
        BigDecimal extrasTotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;

        for (CartItemOptionDTO optionDTO : cartItemDTO.getCartItemOptionDTOList()) {
            FoodOption foodOption = foodOptionMap.get(optionDTO.getOptionId());
            BigDecimal optionUnitPrice = foodOption.getPrice();

            if (optionDTO.getTypeId() == 2) {
                discountTotal = applyDiscount(optionUnitPrice, optionDTO.getQuantity(), product);
            } else {
                extrasTotal = extrasTotal.add(optionUnitPrice.multiply(BigDecimal.valueOf(optionDTO.getQuantity())));
            }
        }

        return discountTotal.add(extrasTotal);
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
                    cartItemDTO.setQuantity(cartItem.getQuantity());

                    // Lấy thông tin sản phẩm
                    productRepository.findById(productId).ifPresent(product -> {
                        cartItemDTO.setProductName(product.getName());
                        cartItemDTO.setImage(product.getImage());
                    });

                    // Lấy giá của CartItem và đảm bảo giá trị không phải null
                    BigDecimal unitPrice = cartItem.getUnitPrice();
                    if (unitPrice == null) {
                        unitPrice = BigDecimal.ZERO;  // Nếu unitPrice là null, sử dụng giá mặc định (hoặc giá trị hợp lý)
                    }

                    // Lấy danh sách discount cho sản phẩm
                    List<Discount> discountList = discountRepository.findAllByProduct_Id(productId);
                    boolean discountApplied = false;
                    BigDecimal discountMultiplier = BigDecimal.ONE;

                    // Kiểm tra nếu có discount đang active
                    for (Discount disc : discountList) {
                        if (disc.getStatus() == Status.ACTIVE) {
                            discountMultiplier = BigDecimal.ONE.subtract(disc.getDiscount_percentage());
                            discountApplied = true;
                            break; // Chỉ áp dụng discount đầu tiên active
                        }
                    }

                    BigDecimal cartItemTotal = BigDecimal.ZERO;
                    List<CartItemOptionDTO> cartItemOptionDTOList = new ArrayList<>();
                    if (cartItem.getCartItemOptions() != null) {
                        for (CartItemOption option : cartItem.getCartItemOptions()) {
                            CartItemOptionDTO optionDTO = new CartItemOptionDTO();
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
                            BigDecimal unitPriceOption = foodOption.getPrice();

                            if (foodOption.getType().getId() == 2 && discountApplied) {
                                // Với option loại 2, áp dụng discount cho toàn bộ sản phẩm
                                optionDTO.setQuantity(cartItem.getQuantity());
                                BigDecimal basePrice = unitPriceOption.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                                BigDecimal finalPrice = basePrice.multiply(discountMultiplier);
                                optionDTO.setTotalPrice(finalPrice);
                                // Thiết lập giá cho CartItemDTO
                                cartItemDTO.setPrice(unitPriceOption);
                                cartItemDTO.setTotalPrice(finalPrice);
                                cartItemTotal = finalPrice;
                            } else {
                                // Với các option khác, tính giá theo số lượng option
                                optionDTO.setQuantity(option.getQuantity());
                                BigDecimal optionTotal = unitPriceOption.multiply(BigDecimal.valueOf(option.getQuantity()));
                                optionDTO.setTotalPrice(optionTotal);
                                cartItemTotal = cartItemTotal.add(optionTotal);
                            }

                            optionDTO.setPrice(option.getUnitPrice());
                            optionDTO.setTotalPrice(option.getTotalPrice());
                            optionDTO.setTypeId(foodOption.getType().getId());
                            cartItemOptionDTOList.add(optionDTO);
                        }
                    }


                    cartItemDTO.setCartItemOptionDTOList(cartItemOptionDTOList);

                    // Nếu không có discount (không có option loại 2 active), sử dụng giá mặc định của CartItem
                    if (!discountApplied) {
                        cartItemDTO.setPrice(unitPrice);
                        if (cartItemTotal.compareTo(BigDecimal.ZERO) == 0) {
                            cartItemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                        }
                        cartItemDTO.setTotalPrice(cartItemTotal);
                    }
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

        CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cartId,id);
        if (cartItem != null) {
            List<CartItemOption> cartItemOption=cartItemOptionRepository.findAllByCartItemId(cartItem.getId());
            cartItemOptionRepository.deleteAll(cartItemOption);
            cartItemRepository.delete(cartItem);
        } else {
            throw new RuntimeException("CartItem not found for the product.");
        }

    }

    @Override
    public void deleteOptionFromCart(Long cartId, Long optionId) {
        CartItemOption cartItemOption = cartItemOptionRepository.findByCartItem_IdAndFoodOption_Id(cartId, optionId);
        cartItemOptionRepository.delete(cartItemOption);
    }


}