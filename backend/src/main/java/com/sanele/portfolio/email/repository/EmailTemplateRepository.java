package com.sanele.portfolio.email.repository;

import com.sanele.portfolio.email.entity.EmailTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByName(String name);
}