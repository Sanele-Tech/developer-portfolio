package com.sanele.portfolio.messages.repository;

import com.sanele.portfolio.messages.entity.MessageReply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReplyRepository extends JpaRepository<MessageReply, Long> {
    List<MessageReply> findByMessageIdOrderByCreatedAtAsc(Long messageId);
}