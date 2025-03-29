package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.service.report.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.viewReport(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> addReport(@Validated @ModelAttribute() ReportCreateDTO reportCreateDTO,
                                        BindingResult bindingResult,
                                        @RequestParam("option") List<MultipartFile> option) throws IOException {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());}
        reportService.createReport(reportCreateDTO, option);
        return ResponseEntity.ok().body(reportCreateDTO);
    }
    @PostMapping("/add")
    public void addToReport(@RequestParam Long id,
                            @RequestParam("option") List<MultipartFile> option) throws IOException {
        reportService.addToReport(id, option);
    }
    @GetMapping("/shop/{id}")
    public ResponseEntity<?> getAllByShop(@PathVariable Long id,
                                          @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                          @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        return ResponseEntity.ok( reportService.findAllByShop(id,page,size));
    }

}
