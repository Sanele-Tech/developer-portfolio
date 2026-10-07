package com.sanele.portfolio.achievements.repository;

import com.sanele.portfolio.achievements.entity.Achievement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByProfileIdOrderBySortOrderAsc(Long profileId);
    List<Achievement> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
    Optional<Achievement> findByIdAndProfileId(Long id, Long profileId);
}