package org.ffb_be.service.report;

import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.dto.report.ReportViewDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ReportService {
    void createReport(ReportCreateDTO report, List<MultipartFile> option) throws IOException;
    ReportViewDTO viewReport(Long id);
    void addToReport(Long id,List<MultipartFile> option) throws IOException;
    Page<ReportViewDTO> findAllByShop(Long id,int page,int size);

}
