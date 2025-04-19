package org.ffb_be.repository;

import org.ffb_be.entity.DeliveryMethod;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryMethodRepository extends JpaRepository<DeliveryMethod, Long> {
    Page<DeliveryMethod> findByStatus(Status status, Pageable pageable);

    List<DeliveryMethod> findAllByStatus(Status status);
}
