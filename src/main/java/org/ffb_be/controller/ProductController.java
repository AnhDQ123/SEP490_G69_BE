package org.ffb_be.controller;



import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.product.ProductCreateDTO;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.ffb_be.service.product.ProductService;

import org.ffb_be.service.product.RecommendationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {
    private final ProductService productService;
    private final RecommendationService recommendationService;


    @GetMapping("/shop/{id}")
    public ResponseEntity<?> getAllByShop(@PathVariable Long id,
                                    @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( productService.findAllByShop(id,pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@Validated @ModelAttribute() ProductCreateDTO productCreateDTO,
                                        BindingResult bindingResult,
                                        @RequestParam("avatar") MultipartFile avatar,
                                        @RequestParam("option")  List<MultipartFile> option) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        productService.update(id,productCreateDTO, avatar, option);
        return ResponseEntity.ok().body(productCreateDTO);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@Validated @ModelAttribute() ProductCreateDTO productCreateDTO,
                                         BindingResult bindingResult,
                                        MultipartFile avatar,
                                        List<MultipartFile> option) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        productService.save(productCreateDTO, avatar, option);
        return ResponseEntity.ok().body(productCreateDTO);
    }
    @GetMapping("/filter") //filter
    public ResponseEntity<?> getByCategory(@RequestParam String cat) {
        return ResponseEntity.ok(productService.findByCategory(cat));
    }

    @GetMapping("/getFresh/{userId}")
    public ResponseEntity<?> getProductByFreshType(
            @PathVariable Long userId,
            @RequestParam(value = "top", required = false) int top) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, "FRESH", top));
    }

    @GetMapping("/getCooked/{userId}")
    public ResponseEntity<?> getProductByCookedType(
            @PathVariable Long userId,
            @RequestParam(value = "top", required = false) int top) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, "COOKED", top));
    }

    @GetMapping("/getPopular")
    public ResponseEntity<?> getPopular() {
        return ResponseEntity.ok(productService.findPopularProducts());
    }

    @GetMapping("/similar")
    public ResponseEntity<?> getSimilarProducts(@RequestParam String search) {
        return ResponseEntity.ok(productService.findSimimlarProduct(search));
    }

    @GetMapping("/{userId}/{productType}/{top}")
    public ResponseEntity<?> getRecommendations(
            @PathVariable Long userId,
            @PathVariable String productType,
            @PathVariable int top) {
        List<ProductResponseDTO> recommendations = recommendationService.getRecommendations(userId, productType, top);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> response = recommendationService.getAllData();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( productService.findAll(pageable));
    }
    @GetMapping("/top-selling/today")
    public List<Object[]> findTopSellingProductsToday(@RequestParam("shopId") Long shopId) {
        return productService.findTopSellingProductsToday(shopId);
    }

    // API để lấy sản phẩm bán chạy nhất trong tháng này cho cửa hàng cụ thể
    @GetMapping("/top-selling/month")
    public List<Object[]> findTopSellingProductsThisMonth(@RequestParam("shopId") Long shopId) {
        return productService.findTopSellingProductsThisMonth(shopId);
    }

    // API để lấy sản phẩm bán chạy nhất trong năm này cho cửa hàng cụ thể
    @GetMapping("/top-selling/year")
    public List<Object[]> findTopSellingProductsThisYear(@RequestParam("shopId") Long shopId) {
        return productService.findTopSellingProductsThisYear(shopId);
    }

    @GetMapping("/shop/drink")
    public ResponseEntity<?> getDrinkByShop(@RequestParam("shopId") Long shopId) {
        return ResponseEntity.ok( productService.findByCategoryByShop(shopId));
    }
//    @GetMapping("/search")
//    public List<ProductResponseDTO> searchProducts(@RequestParam String query,
//                                                   @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
//                                                   @RequestParam(value = "size", defaultValue = "20", required = false) Integer size
//    ) {
//        return productService.searchHighlySimilarProducts(query,page,size); // Tìm kiếm sản phẩm theo từ khóa
//    }
//    @PostMapping("/document")
//    public String syncProducts() {
//        return productService.syncAllProducts();
//    }
}
