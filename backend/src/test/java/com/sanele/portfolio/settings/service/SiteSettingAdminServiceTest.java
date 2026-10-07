package com.sanele.portfolio.settings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.settings.dto.SiteSettingWriteRequest;
import com.sanele.portfolio.settings.entity.SiteSetting;
import com.sanele.portfolio.settings.repository.SiteSettingRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteSettingAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private SiteSettingRepository siteSettingRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private SiteSettingAdminService service;

    @BeforeEach
    void setUp() {
        service = new SiteSettingAdminService(siteSettingRepository, userRepository,
                auditLogRepository, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void createSettingPersistsAndAuditsPublicProfileValue() {
        when(siteSettingRepository.findBySettingKey("profile.location")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(siteSettingRepository.save(any(SiteSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateSetting("profile.location", new SiteSettingWriteRequest(" Cape Town "), ACTOR);

        assertEquals("profile.location", response.settingKey());
        assertEquals("Cape Town", response.settingValue());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("SITE_SETTING", auditCaptor.getValue().getEntityType());
        assertEquals("Cape Town", auditCaptor.getValue().getDetails().get("after").get("settingValue").asText());
    }

    @Test
    void updateSettingCapturesThePreviousValue() {
        SiteSetting existing = mock(SiteSetting.class);
        User actor = mock(User.class);
        when(existing.getId()).thenReturn(12L);
        when(existing.getSettingKey()).thenReturn("profile.location");
        when(existing.getSettingValue()).thenReturn("Durban");
        when(siteSettingRepository.findBySettingKey("profile.location")).thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(actor);

        service.updateSetting("profile.location", new SiteSettingWriteRequest("Cape Town"), ACTOR);

        verify(existing).updateDetails("Cape Town", actor);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("UPDATE", auditCaptor.getValue().getAction());
        assertEquals("Durban", auditCaptor.getValue().getDetails().get("before").get("settingValue").asText());
    }

    @Test
    void unsupportedSettingKeysAreRejected() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateSetting("security.api-key", new SiteSettingWriteRequest("secret"), ACTOR));
        verifyNoInteractions(siteSettingRepository, auditLogRepository);
    }

    @Test
    void deleteSettingAuditsTheOldValue() {
        SiteSetting existing = mock(SiteSetting.class);
        when(existing.getSettingKey()).thenReturn("profile.focus");
        when(existing.getSettingValue()).thenReturn("Design,Engineering");
        when(siteSettingRepository.findBySettingKey("profile.focus")).thenReturn(Optional.of(existing));

        service.deleteSetting("profile.focus", ACTOR);

        verify(siteSettingRepository).delete(existing);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("DELETE", auditCaptor.getValue().getAction());
        assertEquals("Design,Engineering",
                auditCaptor.getValue().getDetails().get("before").get("settingValue").asText());
    }
}
