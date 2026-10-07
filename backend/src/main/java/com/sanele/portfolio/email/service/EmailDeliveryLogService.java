package com.sanele.portfolio.email.service;

import com.sanele.portfolio.email.dto.EmailDeliveryLogResponse;
import com.sanele.portfolio.email.dto.EmailDeliveryStatus;
import com.sanele.portfolio.email.entity.EmailDeliveryLog;
import com.sanele.portfolio.email.repository.EmailDeliveryLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailDeliveryLogService {
    private final EmailDeliveryLogRepository repository;

    public EmailDeliveryLogService(EmailDeliveryLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long contactMessageId, String templateName, String recipient,
            EmailDeliveryStatus status, String failureReason) {
        repository.save(new EmailDeliveryLog(contactMessageId, templateName,
                recipient, status, failureReason));
    }

    @Transactional(readOnly = true)
    public Page<EmailDeliveryLogResponse> getLogs(int page, int size) {
        return repository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(page, size))
                .map(log -> new EmailDeliveryLogResponse(log.getId(), log.getContactMessageId(),
                        log.getTemplateName(), log.getStatus().name(), log.getFailureReason(),
                        log.getCreatedAt()));
    }
}
