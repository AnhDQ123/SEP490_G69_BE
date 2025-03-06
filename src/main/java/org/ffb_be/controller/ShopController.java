package org.ffb_be.controller;

import lombok.AllArgsConstructor;
import org.ffb_be.service.shop.ShopService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin("*")
@AllArgsConstructor
public class ShopController {
    private final ShopService shopService;

}
