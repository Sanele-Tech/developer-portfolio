package com.sanele.portfolio.settings.repository;

import com.sanele.portfolio.settings.entity.FeatureFlag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByFlagKey(String flagKey);
}