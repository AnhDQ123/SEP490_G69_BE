package org.ffb_be.repository;

import org.ffb_be.entity.Report;
import org.ffb_be.utils.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("SELECT r FROM Report r WHERE (r.type.id = 5 AND r.relatedId = :shopId) " +
            "OR (r.type.id = 6 AND r.relatedId IN (SELECT p.id FROM Product p WHERE p.shop.id = :shopId))")
    Page<Report> findReportsByShopId(Long shopId, Pageable pageable);

    @Query("SELECT r.createdAt, COUNT(r) FROM Report r WHERE r.status = :status " +
            "AND r.type.id =:type AND r.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY r.createdAt ORDER BY r.createdAt")
    List<Object[]> countShopReportsByStatusAndDay(ReportStatus status,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  Long type);
    @Query("SELECT FUNCTION('MONTH', r.createdAt), FUNCTION('YEAR', r.createdAt), COUNT(r) FROM Report r " +
            "WHERE r.status = :status AND r.type.id = :type " +
            "AND r.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt) " +
            "ORDER BY FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt)")
    List<Object[]> countReportsByStatusAndTypeIdAndMonth( ReportStatus status,
                                                          LocalDateTime startDate,
                                                          LocalDateTime endDate,
                                                            Long type);

    @Query("SELECT FUNCTION('YEAR', r.createdAt), COUNT(r) FROM Report r " +
            "WHERE r.status = :status AND r.type.id = :type " +
            "AND r.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', r.createdAt) " +
            "ORDER BY FUNCTION('YEAR', r.createdAt)")
    List<Object[]> countReportsByStatusAndTypeIdAndYear(ReportStatus status,
                                                        LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        Long type);
    @Query("SELECT COUNT(r) FROM Report r where r.status='PENDING'")
    Long countAllReports();
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.type.id = 5 AND r.relatedId = :shopId")
    Long countAllReportsByShop(Long shopId);
}
