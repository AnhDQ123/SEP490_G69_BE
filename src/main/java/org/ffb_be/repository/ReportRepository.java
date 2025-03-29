package org.ffb_be.repository;

import org.ffb_be.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("SELECT r FROM Report r WHERE (r.type.id = 5 AND r.relatedId = :shopId) " +
            "OR (r.type.id = 6 AND r.relatedId IN (SELECT p.id FROM Product p WHERE p.shop.id = :shopId))")
    Page<Report> findReportsByShopId(Long shopId, Pageable pageable);
}
