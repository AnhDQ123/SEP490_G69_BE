package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Config;
import org.ffb_be.service.config.ConfigService;
import org.ffb_be.utils.enums.ConfigCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigService configService;

    @GetMapping("/{category}")
    public ResponseEntity<Page<Config>> getConfigsByCategory(
            @PathVariable ConfigCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(configService.getConfigsByCategory(category, pageable));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Config> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(configService.getConfigById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Config> createConfig(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(configService.createConfig(
                ConfigCategory.valueOf(request.get("category")),
                request.get("key"),
                request.get("value")
        ));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Config> updateConfig(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(configService.updateConfig(id, request.get("value")));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }
}
