package org.ffb_be.repository;

import org.ffb_be.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findAllByProduct_Id(Long productId, Pageable pageable);
    long countAllByProduct_Id(Long productId);

    List<Feedback> findAllByProduct_Id(Long productId);
}
