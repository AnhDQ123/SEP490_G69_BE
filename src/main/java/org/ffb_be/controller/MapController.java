package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.service.map.MapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;

    @GetMapping("/geocode")
    public double[] getCoordinates(@RequestParam String address) {
        return mapService.getCoordinates(address);
    }
}
