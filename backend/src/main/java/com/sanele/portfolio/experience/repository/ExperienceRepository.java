package com.sanele.portfolio.experience.repository;

import com.sanele.portfolio.experience.entity.Experience;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByProfileIdOrderBySortOrderAsc(Long profileId);
    List<Experience> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
    Optional<Experience> findByIdAndProfileId(Long id, Long profileId);
}