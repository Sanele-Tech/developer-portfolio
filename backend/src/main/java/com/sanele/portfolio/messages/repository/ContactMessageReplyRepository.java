package com.sanele.portfolio.messages.repository;

import com.sanele.portfolio.messages.entity.ContactMessageReply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageReplyRepository extends JpaRepository<ContactMessageReply, Long> {
    List<ContactMessageReply> findAllByContactMessageIdOrderByCreatedAtAsc(Long contactMessageId);
}
