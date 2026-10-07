package com.sanele.portfolio.achievements.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.achievements.dto.AchievementWriteRequest;
import com.sanele.portfolio.achievements.entity.Achievement;
import com.sanele.portfolio.achievements.repository.AchievementRepository;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import jakarta.validation.Validation;
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
class AchievementAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private AchievementRepository achievementRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private AchievementAdminService service;
    private Profile profile;

    @BeforeEach
    void setUp() {
        service = new AchievementAdminService(achievementRepository, profileRepository,
                userRepository, auditLogRepository, new ObjectMapper().findAndRegisterModules());
        profile = mock(Profile.class);
    }

    @Test
    void createAchievementPersistsNormalizedDetailsAndAuditEntry() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.createAchievement(request(" Builder Award ", " Open Source Foundation "),
                ACTOR);

        assertEquals("Builder Award", response.title());
        assertEquals("CERTIFICATION", response.cardType());
        assertEquals("Open Source Foundation", response.issuer());
        assertEquals("https://example.test/award", response.url());
        assertEquals("https://example.test/certificate.png", response.imageUrl());
        assertEquals(List.of("https://example.test/certificate.png"), response.imageUrls());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("ACHIEVEMENT", auditCaptor.getValue().getEntityType());
        assertEquals("Builder Award", auditCaptor.getValue().getDetails().get("after").get("title").asText());
    }

    @Test
    void updateAchievementRecordsBeforeAndAfterSnapshots() {
        Achievement achievement = new Achievement(profile, "Old award", "CERTIFICATION", "Old issuer",
                "Old description", LocalDate.of(2022, 1, 1), "https://example.test/old",
                "https://example.test/old.png", List.of("https://example.test/old.png"), 1);
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(achievementRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.of(achievement));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.updateAchievement(9L, request("New award", "New issuer"), ACTOR);

        assertEquals("New award", response.title());
        assertEquals("https://example.test/certificate.png", response.imageUrl());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("Old award", auditCaptor.getValue().getDetails().get("before").get("title").asText());
        assertEquals("New award", auditCaptor.getValue().getDetails().get("after").get("title").asText());
    }

    @Test
    void deleteAchievementAuditsTheRemovedItem() {
        Achievement achievement = new Achievement(profile, "Community award", "COMMUNITY", null, null,
                null, null, null, List.of(), 0);
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(achievementRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.of(achievement));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        service.deleteAchievement(9L, ACTOR);

        verify(achievementRepository).delete(achievement);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("DELETE", auditCaptor.getValue().getAction());
        assertEquals("Community award", auditCaptor.getValue().getDetails().get("before").get("title").asText());
    }

    @Test
    void getAchievementCannotReadAnotherProfileAchievement() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(achievementRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getAchievement(9L));
    }

    @Test
    void writeRequestValidatesRequiredTitleAndSortOrder() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            assertTrue(validator.validate(request("Award", "Issuer")).isEmpty());
            assertFalse(validator.validate(new AchievementWriteRequest(" ", null, null, null, null,
                    "x".repeat(501), null, -1, List.of())).isEmpty());
        }
    }

    private AchievementWriteRequest request(String title, String issuer) {
        return new AchievementWriteRequest(title, "CERTIFICATION", issuer, "  ",
                LocalDate.of(2024, 4, 15), " https://example.test/award ",
                " https://example.test/certificate.png ", 2, List.of(" https://example.test/certificate.png "));
    }
}
