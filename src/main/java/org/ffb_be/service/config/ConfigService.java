package org.ffb_be.service.config;

import org.ffb_be.entity.Config;
import org.ffb_be.utils.enums.ConfigCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConfigService {
    Page<Config> getConfigsByCategory(ConfigCategory category, Pageable pageable);

    Config getConfigById(Long id);

    Config createConfig(ConfigCategory category, String key, String value);

    Config updateConfig(Long id, String value);

    void deleteConfig(Long id);
}
