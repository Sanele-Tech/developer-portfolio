package com.sanele.portfolio.projects.repository;

import com.sanele.portfolio.projects.entity.Technology;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    Optional<Technology> findByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    List<Technology> findAllByOrderByNameAsc();
}