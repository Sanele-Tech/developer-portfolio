package com.sanele.portfolio.experience.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.experience.dto.ExperienceWriteRequest;
import com.sanele.portfolio.experience.entity.Experience;
import com.sanele.portfolio.experience.repository.ExperienceRepository;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperienceAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private ExperienceRepository experienceRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private ExperienceAdminService service;
    private Profile profile;

    @BeforeEach
    void setUp() {
        service = new ExperienceAdminService(experienceRepository, profileRepository,
                userRepository, auditLogRepository, new ObjectMapper().findAndRegisterModules());
        profile = mock(Profile.class);
    }

    @Test
    void createExperiencePersistsRoleAndAuditEntry() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.createExperience(request(false, LocalDate.of(2025, 12, 31)), ACTOR);

        assertEquals("Developer", response.jobTitle());
        assertEquals("Example Studio", response.company());
        assertFalse(response.currentRole());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("EXPERIENCE", auditCaptor.getValue().getEntityType());
        assertEquals("Developer", auditCaptor.getValue().getDetails().get("after").get("jobTitle").asText());
    }

    @Test
    void currentRoleDoesNotPersistEndDate() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.createExperience(request(true, null), ACTOR);

        assertTrue(response.currentRole());
        assertEquals(null, response.endDate());
    }

    @Test
    void requestDateRangeValidationRejectsInvalidDatesAndCurrentEndDate() {
        var invalidRange = new ExperienceWriteRequest("Developer", "Example Studio", "Built things",
                LocalDate.of(2025, 1, 1), LocalDate.of(2024, 12, 31), false, 0);
        var currentWithEndDate = new ExperienceWriteRequest("Developer", "Example Studio", "Built things",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), true, 0);

        assertFalse(invalidRange.isDateRangeValid());
        assertFalse(currentWithEndDate.isDateRangeValid());
        assertTrue(request(false, null).isDateRangeValid());
    }

    private ExperienceWriteRequest request(boolean currentRole, LocalDate endDate) {
        return new ExperienceWriteRequest(" Developer ", " Example Studio ", " Built things ",
                LocalDate.of(2025, 1, 1), endDate, currentRole, 2);
    }
}
