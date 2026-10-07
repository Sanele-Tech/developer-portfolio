package com.sanele.portfolio.content.repository;

import com.sanele.portfolio.content.entity.PageVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageVersionRepository extends JpaRepository<PageVersion, Long> {
    List<PageVersion> findByPageIdOrderByCreatedAtDesc(Long pageId);
    Optional<PageVersion> findFirstByPageIdOrderByCreatedAtDesc(Long pageId);
    Optional<PageVersion> findFirstByPageIdOrderByVersionNumberDesc(Long pageId);
}