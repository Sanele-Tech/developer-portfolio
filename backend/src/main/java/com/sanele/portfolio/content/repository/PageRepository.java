package com.sanele.portfolio.content.repository;

import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.entity.Page.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findBySlug(String slug);
    Optional<Page> findBySlugAndStatus(String slug, Status status);
    List<Page> findByStatusOrderByUpdatedAtDesc(Status status);
    List<Page> findAllByOrderByUpdatedAtDescIdDesc();
    long countByStatus(Status status);
}