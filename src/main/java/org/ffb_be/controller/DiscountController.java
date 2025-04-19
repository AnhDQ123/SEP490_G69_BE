package org.ffb_be.controller;


import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.discount.DiscountDTO;
import org.ffb_be.service.discount.DiscountService;
import org.ffb_be.utils.enums.Status;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discount")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping("/add")
    public void addDiscount(@Validated @RequestBody DiscountDTO discountDTO, @RequestParam Long productId) {
        discountService.save(discountDTO,productId);
    }
    @GetMapping
    public DiscountDTO findById(@RequestParam Long id) {
        return discountService.findById(id);
    }

    @GetMapping("/shop")
    public List<DiscountDTO> findByShopId(@RequestParam Long shopId, @RequestParam Status status) {
        return discountService.findAllByShopIdAndStatus(shopId, status);
    }
    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody DiscountDTO discountDTO) {
        discountService.update(discountDTO,id);
    }
    @PutMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        discountService.delete(id);
    }
}
