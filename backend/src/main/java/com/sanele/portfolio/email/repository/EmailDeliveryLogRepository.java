package com.sanele.portfolio.email.repository;

import com.sanele.portfolio.email.entity.EmailDeliveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, Long> {
    Page<EmailDeliveryLog> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
}
