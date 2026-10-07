package com.sanele.portfolio.content.repository;

import com.sanele.portfolio.content.entity.SeoMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeoMetadataRepository extends JpaRepository<SeoMetadata, Long> {
    Optional<SeoMetadata> findByPageId(Long pageId);
}