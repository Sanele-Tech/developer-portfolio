package com.sanele.portfolio.skills.repository;

import com.sanele.portfolio.skills.entity.Skill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByProfileIdOrderByCategoryAscSortOrderAsc(Long profileId);
    List<Skill> findByProfileIdOrderByCategoryAscSortOrderAscIdAsc(Long profileId);
    Optional<Skill> findByIdAndProfileId(Long id, Long profileId);
}