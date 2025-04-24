package org.ffb_be.service.report;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.CountDTOBy.CountByDateDTO;
import org.ffb_be.dto.CountDTOBy.CountByMonthDTO;
import org.ffb_be.dto.CountDTOBy.CountByYearDTO;
import org.ffb_be.dto.image.ImageDTO;
import org.ffb_be.dto.report.ReportCreateDTO;
import org.ffb_be.dto.report.ReportViewDTO;
import org.ffb_be.entity.*;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.ReportStatus;

import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        reportViewDTO.setReporterId(report.getReporter().getId());
        if(report.getType().getId()==4L){
            reportViewDTO.setReportName(blogRepository.findById(report.getRelatedId()).get().getWriter().getUsername());
            reportViewDTO.setReportedUserId(blogRepository.findById(report.getRelatedId()).get().getWriter().getId());
            reportViewDTO.setReportItemId(report.getRelatedId());
            reportViewDTO.setReportType("BLOG");
        }
        if(report.getType().getId()==5L){
            reportViewDTO.setReportName(shopRepository.findById(report.getRelatedId()).get().getName());
            reportViewDTO.setReportedUserId(shopRepository.findById(report.getRelatedId()).get().getOwner().getId());
            reportViewDTO.setReportItemId(report.getRelatedId());
            reportViewDTO.setReportType("SHOP");
        }if(report.getType().getId()==6L){
            reportViewDTO.setReportName(productRepository.findById(report.getRelatedId()).get().getName());
            reportViewDTO.setReportedUserId(shopRepository.findByProduct(report.getRelatedId()).getOwner().getId());
            reportViewDTO.setReportItemId(report.getRelatedId());
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

    @Override
    public void addToReport(Long id, List<MultipartFile> option) throws IOException {
        Report report=reportRepository.findById(id).orElse(null);
        if (option != null && !option.isEmpty()) {
            System.out.println("Uploading " + option.size() + " option images");
            for (int i = 0; i < option.size(); i++) {
                Image image=new Image();
                System.out.println("Uploading Avatar: " + option.get(i).getOriginalFilename());
                String url = cloudinaryUpload.uploadFile(option.get(i));
                image.setUrl(url);
                image.setRelatedId(id);
                image.setOwnerId(report.getReporter().getId());
                image.setType(typesRepository.findById(7l).get());
                imageRepository.save(image);
            }
        }
    }
    private ReportViewDTO convertToDTO(Report report) {
        ReportViewDTO reportViewDTO = new ReportViewDTO();
        reportViewDTO.setId(report.getId());
        reportViewDTO.setReportName(report.getType().getDescription());  // Giả sử bạn muốn lấy mô tả từ Type
        reportViewDTO.setReportType(report.getType().getCategory().toString());  // Hoặc loại enum của type
        reportViewDTO.setReporterId(report.getReporter().getId());
        reportViewDTO.setReportedUserId(report.getRelatedId());
        reportViewDTO.setReportItemId(report.getRelatedId());
        reportViewDTO.setReason(report.getReason());
        reportViewDTO.setCreatedAt(report.getCreatedAt());
        reportViewDTO.setStatus(report.getStatus().name());
        List<Image> imageList=imageRepository.findAllByRelatedIdAndType_Id(report.getId(),7l);
        List<ImageDTO> imageDTOList=new ArrayList<>();
        for (Image image : imageList) {
            ImageDTO imageDTO=new ImageDTO();
            imageDTO.setUrl(image.getUrl());
            imageDTO.setRelatedId(report.getId());
            imageDTO.setId(image.getId());
            imageDTO.setOwnerId(image.getOwnerId());
            imageDTO.setTypeId(7l);
            imageDTOList.add(imageDTO);
        }
        reportViewDTO.setImage(imageDTOList);
        return reportViewDTO;
    }
    public Page<ReportViewDTO> findAllByShop(Long shopId, int page, int size) {
        // Tạo đối tượng Pageable để phân trang
        Pageable pageable = PageRequest.of(page, size);

        // Gọi phương thức repository để lấy kết quả phân trang
        Page<Report> reports = reportRepository.findReportsByShopId(shopId, pageable);

        // Chuyển đổi từ Page<Report> sang Page<ReportViewDTO>
        return reports.map(this::convertToDTO);  // Sử dụng phương thức convertToDTO mà bạn đã tạo trước đó
    }

    @Override
    public Page<ReportViewDTO> findAllByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByStatus(status,pageable);
        return reports.map(this::convertToDTO);
    }

    @Override
    public Page<ReportViewDTO> findAllByType(Long id, Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByType_Id(id,pageable);
        return reports.map(this::convertToDTO);
    }

    @Override
    public Page<ReportViewDTO> findAll(Pageable pageable) {
        Page<Report> reports = reportRepository.findAll(pageable);
        return reports.map(this::convertToDTO);
    }

    public List<CountByDateDTO> getReportCountByDay(ReportStatus status, LocalDate startDate, LocalDate endDate, Long type) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = reportRepository.countShopReportsByStatusAndDay(status, startDateTime, endDateTime,type);

        List<CountByDateDTO> countByDateDTOS = new ArrayList<>();
        for (Object[] result : results) {
            CountByDateDTO countByDateDTO = new CountByDateDTO();
            countByDateDTO.setDate((LocalDateTime) result[0]);
            countByDateDTO.setCount((Long) result[1]);
            countByDateDTOS.add(countByDateDTO);
        }
        return countByDateDTOS;
    }
    public List<CountByMonthDTO> getReportCountByMonth(ReportStatus status, LocalDate startDate, LocalDate endDate, Long type) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = reportRepository.countReportsByStatusAndTypeIdAndMonth(status, startDateTime, endDateTime,type);

        List<CountByMonthDTO> countByMonthDTOS = new ArrayList<>();

        // Chuyển đổi kết quả thành danh sách DTO
        for (Object[] result : results) {
            CountByMonthDTO countByMonthDTO = new CountByMonthDTO();

            // Get the month and year from the query result
            int month = (Integer) result[1]; // Month
            int year = (Integer) result[0]; // Year

            // Format the month as yyyy/MM
            String formattedMonth = String.format("%d/%02d", year, month); // Example: 2025/03
            countByMonthDTO.setMonth(formattedMonth);

            // Get the order count (it could be either Long or Integer)
            if (result[2] instanceof Long) {
                countByMonthDTO.setCount((Long) result[2]);
            } else {
                countByMonthDTO.setCount(((Integer) result[2]).longValue());
            }

            // Add the DTO to the result list
            countByMonthDTOS.add(countByMonthDTO);
        }
        return countByMonthDTOS;
    }
    public List<CountByYearDTO> getReportCountByYear(ReportStatus status, LocalDate startDate, LocalDate endDate,Long type) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2023-01-01T00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Object[]> results = reportRepository.countReportsByStatusAndTypeIdAndYear(status, startDateTime, endDateTime,type);

        List<CountByYearDTO> countByYearDTOS = new ArrayList<>();

        // Duyệt qua các kết quả trả về từ truy vấn
        for (Object[] result : results) {
            CountByYearDTO countByYearDTO = new CountByYearDTO();

            // Lấy năm từ kết quả truy vấn (result[0] chứa năm)
            int year = (Integer) result[0];
            countByYearDTO.setYear(year);

            // Lấy số lượng đơn hàng từ kết quả truy vấn (result[1] chứa số lượng đơn hàng)
            Long count = (Long) result[1];
            countByYearDTO.setCount(count);

            // Thêm đối tượng vào danh sách kết quả
            countByYearDTOS.add(countByYearDTO);
        }

        // Trả về danh sách kết quả
        return countByYearDTOS;
    }
    public Long countAllReports() {
        return reportRepository.countAllReports();
    }

    @Override
    public void updateReportStatus(Long id) {
        Report report=reportRepository.findById(id).get();
        if(report.getType().getId()==4L){
            Blog blog=blogRepository.findById(report.getRelatedId()).get();
            blogRepository.save(blog);
        }
        if(report.getType().getId()==5L){
            Shop shop=shopRepository.findById(report.getRelatedId()).get();
            shopRepository.save(shop);
        }
        if(report.getType().getId()==6L){
            Product product=productRepository.findById(report.getRelatedId()).get();
            productRepository.save(product);
        }
        report.setStatus(ReportStatus.COMPLETED);
        reportRepository.save(report);
    }

    @Override
    public Long countAllByShop(Long shopId) {
        return reportRepository.countAllReportsByShop(shopId);
    }

    @Override
    public double reportChangeRate() {
        LocalDateTime startOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double a=reportRepository.countPendingReportByMonth(startOfLastMonth, endOfLastMonth);
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        double b=reportRepository.countPendingReportByMonth(startOfThisMonth, endOfThisMonth);
        return (b-a)/a*100;
    }
}
