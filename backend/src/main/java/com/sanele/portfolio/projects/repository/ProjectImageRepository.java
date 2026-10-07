package com.sanele.portfolio.projects.repository;

import com.sanele.portfolio.projects.entity.ProjectImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
    List<ProjectImage> findByProjectIdOrderBySortOrderAsc(Long projectId);
    void deleteByProjectId(Long projectId);
}