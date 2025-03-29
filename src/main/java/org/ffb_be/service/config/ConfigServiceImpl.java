package org.ffb_be.service.config;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.Config;
import org.ffb_be.repository.ConfigRepository;
import org.ffb_be.utils.enums.ConfigCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigRepository configRepository;

    @Override
    public Page<Config> getConfigsByCategory(ConfigCategory category, Pageable pageable) {
        return configRepository.findByCategory(category, pageable);
    }

    @Override
    public Config getConfigById(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config không tồn tại"));
    }

    @Override
    public Config createConfig(ConfigCategory category, String key, String value) {
        if (configRepository.findByKey(key).isPresent()) {
            throw new IllegalArgumentException("Config đã tồn tại");
        }
        return configRepository.save(new Config(null, category, key, value));
    }

    @Override
    public Config updateConfig(Long id, String value) {
        Config config = getConfigById(id);
        config.setValue(value);
        return configRepository.save(config);
    }

    @Override
    public void deleteConfig(Long id) {
        Config config = getConfigById(id);
        configRepository.delete(config);
    }
}
