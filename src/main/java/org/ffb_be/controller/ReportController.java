package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.service.report.ReportService;
import org.ffb_be.utils.enums.ReportStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
                                         List<MultipartFile> option) throws IOException {
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

    @GetMapping("/status")
    public ResponseEntity<?> getAllByStatus(@RequestParam("status") String status,
                                          @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                          @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        ReportStatus reportStatus = ReportStatus.valueOf(status);
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( reportService.findAllByStatus(reportStatus,pageable));
    }
    @GetMapping("/type")
    public ResponseEntity<?> getAllByType(@RequestParam("type") Long id,
                                            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( reportService.findAllByType(id,pageable));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok( reportService.findAll(pageable));
    }
    @GetMapping("/count/day")
    public List<CountByDateDTO> getReportCountByDay(
            @RequestParam("status") String status,
            @RequestParam("type") Long type) {
        ReportStatus reportStatus = ReportStatus.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return reportService.getReportCountByDay(reportStatus, startDate, endDate,type);
    }
    @GetMapping("/count/month")
    public List<CountByMonthDTO> getReportCountByMonth(
            @RequestParam("status") String status,
            @RequestParam("type") Long type) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(7);
        ReportStatus reportStatus = ReportStatus.valueOf(status);
        return reportService.getReportCountByMonth(reportStatus, startDate, endDate,type);
    }
    @GetMapping("/count/year")
    public List<CountByYearDTO> getReportCountByYear(
            @RequestParam("status") String status,
            @RequestParam("type") Long type) {
        ReportStatus reportStatus = ReportStatus.valueOf(status);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(3);

        return reportService.getReportCountByYear(reportStatus, startDate, endDate,type);
    }
    @GetMapping("count/pending")
    public Long countAllPendingReports() {
        return reportService.countAllReports();
    }

    @PutMapping("/update")
    public void updateStatus(
                              @RequestParam("id") Long id){
        reportService.updateReportStatus(id);
    }

    @GetMapping("/shop/count")
    public Long countReportsByShop(@RequestParam("id") Long id) {
        return reportService.countAllByShop(id);
    }
    @GetMapping("/change/rate")
    public double reportChangeRate(){
        return reportService.reportChangeRate();
    }
}
