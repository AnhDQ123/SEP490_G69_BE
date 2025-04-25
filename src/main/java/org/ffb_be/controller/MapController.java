package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.service.map.MapService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;

    @GetMapping("/geocode/{address}")
    public String getGeocode(@PathVariable String address) {
        return mapService.getGeocode(address);
    }

    @GetMapping("/reverse-geocode/{lat}/{lng}")
    public String getReverseGeocode(@PathVariable double lat, @PathVariable double lng) {
        return mapService.getReverseGeocode(lat, lng);
    }
    @GetMapping("/getRoute")
    public String getRoute(@RequestParam String origin, @RequestParam String destination) {
        return mapService.getRoute(origin, destination);
    }
}
