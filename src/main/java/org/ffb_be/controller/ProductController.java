package org.ffb_be.controller;



import org.ffb_be.dto.product.ProductCreateDTO;

import org.ffb_be.service.product.ProductService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

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
    @PutMapping()
    public ResponseEntity<?> update(@Validated @ModelAttribute() ProductCreateDTO productCreateDTO,
                                        BindingResult bindingResult,
                                        @RequestParam("avatar") MultipartFile avatar,
                                        @RequestParam("option")  List<MultipartFile> option) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        productService.save(productCreateDTO, avatar, option);
        return ResponseEntity.ok().body(productCreateDTO);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@Validated @ModelAttribute() ProductCreateDTO productCreateDTO,
                                         BindingResult bindingResult,
                                         @RequestParam("avatar") MultipartFile avatar,
                                         @RequestParam("option")  List<MultipartFile> option) throws IOException {
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
    @GetMapping("/getFresh")
    public ResponseEntity<?> getProductByFreshType() {
        return ResponseEntity.ok(productService.findFreshProducts());
    }
    @GetMapping("/getCooked")
    public ResponseEntity<?> getProductByCookedType() {
        return ResponseEntity.ok(productService.findCookedProducts());
    }
    @GetMapping("/getPopular")
    public ResponseEntity<?> getPopular() {
        return ResponseEntity.ok(productService.findPopularProducts());
    }
    @GetMapping("/similar")
    public ResponseEntity<?> getSimilarProducts(@RequestParam String search) {
        return ResponseEntity.ok(productService.findSimimlarProduct(search));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( productService.findAll(pageable));
    }
}
