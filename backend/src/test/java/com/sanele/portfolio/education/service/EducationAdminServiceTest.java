package com.sanele.portfolio.education.service;

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
import com.sanele.portfolio.education.dto.EducationWriteRequest;
import com.sanele.portfolio.education.entity.Education;
import com.sanele.portfolio.education.repository.EducationRepository;
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
class EducationAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private EducationRepository educationRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private EducationAdminService service;
    private Profile profile;

    @BeforeEach
    void setUp() {
        service = new EducationAdminService(educationRepository, profileRepository,
                userRepository, auditLogRepository, new ObjectMapper().findAndRegisterModules());
        profile = mock(Profile.class);
    }

    @Test
    void createEducationPersistsTheDetailsAndAuditEntry() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.createEducation(request(" Example University ", null), ACTOR);

        assertEquals("Example University", response.institution());
        assertEquals("BSc Software Development", response.qualification());
        assertEquals(null, response.description());
        assertEquals(List.of("Software Engineering"), response.coreModules());
        assertEquals("Built a student project.", response.experience());
        assertEquals(List.of("https://example.test/campus.jpg"), response.imageUrls());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("EDUCATION", auditCaptor.getValue().getEntityType());
        assertEquals("Example University",
                auditCaptor.getValue().getDetails().get("after").get("institution").asText());
    }

    @Test
    void updateEducationRecordsBeforeAndAfterSnapshots() {
        Education education = new Education(profile, "Old University", "Old qualification",
                "Old description", LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1),
                List.of("Old module"), "Old experience", List.of("https://example.test/old.jpg"), 1);
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(educationRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.of(education));
        when(educationRepository.save(any(Education.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.updateEducation(9L, request("New University", LocalDate.of(2025, 12, 31)), ACTOR);

        assertEquals("New University", response.institution());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("UPDATE", auditCaptor.getValue().getAction());
        assertEquals("Old University",
                auditCaptor.getValue().getDetails().get("before").get("institution").asText());
        assertEquals("New University",
                auditCaptor.getValue().getDetails().get("after").get("institution").asText());
    }

    @Test
    void requestRejectsEndDateBeforeStartDate() {
        var invalid = new EducationWriteRequest("University", "Qualification", null,
                LocalDate.of(2025, 1, 1), LocalDate.of(2024, 12, 31), 0,
                List.of(), null, List.of());
        assertFalse(invalid.isDateRangeValid());
        assertTrue(request("University", null).isDateRangeValid());
    }

    private EducationWriteRequest request(String institution, LocalDate endDate) {
        return new EducationWriteRequest(institution, "BSc Software Development", "  ",
                LocalDate.of(2022, 1, 1), endDate, 2, List.of(" Software Engineering "),
                " Built a student project. ", List.of(" https://example.test/campus.jpg "));
    }
}
