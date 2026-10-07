package com.sanele.portfolio.messages.repository;

import com.sanele.portfolio.messages.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    Page<ContactMessage> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
    long countByStatus(ContactMessage.Status status);
}