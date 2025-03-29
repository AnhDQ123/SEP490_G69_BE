package org.ffb_be.repository;

import org.ffb_be.entity.Config;
import org.ffb_be.utils.enums.ConfigCategory;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByKey(String key);

    Page<Config> findByCategoryAndStatus(ConfigCategory category, Status status, Pageable pageable);
}
