package org.ffb_be.service.report;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.dto.report.ReportViewDTO;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Report;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.ReportStatus;

import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final BlogRepository blogRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TypesRepository typesRepository;
    private final CloudinaryUpload cloudinaryUpload;
    private final ImageRepository imageRepository;
    @Override
    public void createReport(ReportCreateDTO reportCreateDTO, List<MultipartFile> option) throws IOException {
        Report report = new Report();
        report.setReporter(userRepository.findById(reportCreateDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found")));
        report.setStatus(ReportStatus.PENDING);
        report.setType(typesRepository.findById(reportCreateDTO.getTypeId()).get());
        report.setReason(reportCreateDTO.getReason());
        report.setRelatedId(reportCreateDTO.getRelatedId());
        report.setCreatedAt(LocalDateTime.now());
        reportRepository.save(report);
        if (option != null && !option.isEmpty()) {
            System.out.println("Uploading " + option.size() + " option images");
            for (int i = 0; i < option.size(); i++) {
                Image image=new Image();
                    System.out.println("Uploading Avatar: " + option.get(i).getOriginalFilename());
                    String url = cloudinaryUpload.uploadFile(option.get(i));
                    image.setUrl(url);
                    image.setRelatedId(report.getId());
                    image.setOwnerId(report.getReporter().getId());
                    image.setType(typesRepository.findById(7l).get());
                    imageRepository.save(image);
            }
        }
    }

    @Override
    public ReportViewDTO viewReport(Long id) {
        Report report=reportRepository.findById(id).orElse(null);
        ReportViewDTO reportViewDTO=new ReportViewDTO();
        reportViewDTO.setId(id);
        if(report.getType().getId()==4l){
            reportViewDTO.setReportName(blogRepository.findById(report.getRelatedId()).get().getWriter().getUsername());
            reportViewDTO.setReportType("BLOG");
        }
        if(report.getType().getId()==5l){
            reportViewDTO.setReportName(shopRepository.findById(report.getRelatedId()).get().getName());
            reportViewDTO.setReportType("SHOP");
        }if(report.getType().getId()==6l){
            reportViewDTO.setReportName(productRepository.findById(report.getRelatedId()).get().getName());
            reportViewDTO.setReportType("PRODUCT");
        }
        List<Image> imageList=imageRepository.findAllByRelatedIdAndType_Id(id,7l);
        List<ImageDTO> imageDTOList=new ArrayList<>();
        for (Image image : imageList) {
            ImageDTO imageDTO=new ImageDTO();
            imageDTO.setUrl(image.getUrl());
            imageDTO.setRelatedId(id);
            imageDTO.setId(image.getId());
            imageDTO.setOwnerId(image.getOwnerId());
            imageDTO.setTypeId(7l);
            imageDTOList.add(imageDTO);
        }
        reportViewDTO.setImage(imageDTOList);
        reportViewDTO.setReason(report.getReason());
        reportViewDTO.setStatus(report.getStatus().toString());
        reportViewDTO.setCreatedAt(report.getCreatedAt());
        return reportViewDTO;
    }
}
