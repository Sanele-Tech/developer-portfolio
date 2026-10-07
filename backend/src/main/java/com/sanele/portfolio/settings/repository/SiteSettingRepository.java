package com.sanele.portfolio.settings.repository;

import com.sanele.portfolio.settings.entity.SiteSetting;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {
    Optional<SiteSetting> findBySettingKey(String settingKey);
    List<SiteSetting> findAllBySettingKeyInOrderBySettingKeyAsc(Collection<String> settingKeys);
}