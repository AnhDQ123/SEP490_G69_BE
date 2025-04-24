package org.ffb_be.service.report;

import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.dto.report.ReportViewDTO;
import org.ffb_be.utils.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface ReportService {
    void createReport(ReportCreateDTO report, List<MultipartFile> option) throws IOException;
    ReportViewDTO viewReport(Long id);
    void addToReport(Long id,List<MultipartFile> option) throws IOException;
    Page<ReportViewDTO> findAllByShop(Long id,int page,int size);
    Page<ReportViewDTO> findAllByStatus(ReportStatus status, Pageable pageable);
    Page<ReportViewDTO> findAllByType(Long id, Pageable pageable);
    Page<ReportViewDTO> findAll(Pageable pageable);
    List<CountByYearDTO > getReportCountByYear(ReportStatus status, LocalDate startDate, LocalDate endDate, Long type);
    List<CountByMonthDTO> getReportCountByMonth(ReportStatus status, LocalDate startDate, LocalDate endDate, Long type);
    List<CountByDateDTO> getReportCountByDay(ReportStatus status, LocalDate startDate, LocalDate endDate, Long type);
    Long countAllReports();
    void updateReportStatus(Long id);
    Long countAllByShop(Long shopId);
    double reportChangeRate();
}
