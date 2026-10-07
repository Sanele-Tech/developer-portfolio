package com.sanele.portfolio.messages.repository;

import com.sanele.portfolio.messages.entity.Message;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Message> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    long countByStatus(String status);
}