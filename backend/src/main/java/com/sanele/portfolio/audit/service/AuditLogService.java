package com.sanele.portfolio.audit.service;

import com.sanele.portfolio.audit.dto.AuditLogResponse;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Page<AuditLogResponse> getAuditLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(log -> new AuditLogResponse(log.getId(),
                        log.getUser() == null ? "System" : log.getUser().getDisplayName(),
                        log.getAction(), log.getEntityType(), log.getEntityId(),
                        log.getDetails(), log.getCreatedAt()));
    }
}
