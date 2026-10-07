package com.sanele.portfolio.projects.repository;

import com.sanele.portfolio.projects.entity.ProjectLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLinkRepository extends JpaRepository<ProjectLink, Long> {
    List<ProjectLink> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}