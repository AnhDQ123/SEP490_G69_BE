package org.ffb_be.repository;

import org.ffb_be.entity.Config;
import org.ffb_be.utils.enums.ConfigCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByKey(String key);
    Page<Config> findByCategory(ConfigCategory category, Pageable pageable);
}
