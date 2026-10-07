package com.sanele.portfolio.education.repository;

import com.sanele.portfolio.education.entity.Education;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByProfileIdOrderBySortOrderAsc(Long profileId);
    List<Education> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
    Optional<Education> findByIdAndProfileId(Long id, Long profileId);
}