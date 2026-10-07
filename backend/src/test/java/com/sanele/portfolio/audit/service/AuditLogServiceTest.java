package com.sanele.portfolio.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.users.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AuditLogServiceTest {
    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final AuditLogService service = new AuditLogService(repository);

    @Test
    void returnsPagedAuditEntriesWithActorAndChangeDetails() {
        User user = mock(User.class);
        when(user.getDisplayName()).thenReturn("Portfolio Admin");
        AuditLog log = mock(AuditLog.class);
        when(log.getId()).thenReturn(18L);
        when(log.getUser()).thenReturn(user);
        when(log.getAction()).thenReturn("UPDATE");
        when(log.getEntityType()).thenReturn("PAGE");
        when(log.getEntityId()).thenReturn(7L);
        when(log.getDetails()).thenReturn(JsonNodeFactory.instance.objectNode().put("changed", true));
        when(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(log)));

        var result = service.getAuditLogs(0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("Portfolio Admin", result.getContent().getFirst().actor());
        assertEquals("UPDATE", result.getContent().getFirst().action());
        assertEquals(true, result.getContent().getFirst().details().get("changed").asBoolean());
    }

    @Test
    void labelsEntriesWithoutAnActorAsSystem() {
        AuditLog log = mock(AuditLog.class);
        when(log.getUser()).thenReturn(null);
        when(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(log)));

        var result = service.getAuditLogs(0, 20);

        assertEquals("System", result.getContent().getFirst().actor());
    }
}
