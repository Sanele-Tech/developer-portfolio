package com.sanele.portfolio.skills.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.skills.dto.SkillWriteRequest;
import com.sanele.portfolio.skills.entity.Skill;
import com.sanele.portfolio.skills.repository.SkillRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SkillAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private SkillRepository skillRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private SkillAdminService service;
    private Profile profile;

    @BeforeEach
    void setUp() {
        service = new SkillAdminService(skillRepository, profileRepository,
                userRepository, auditLogRepository, new ObjectMapper().findAndRegisterModules());
        profile = mock(Profile.class);
    }

    @Test
    void createSkillPersistsNormalizedDetailsAndAuditEntry() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.createSkill(new SkillWriteRequest(" React ", " Frontend ", 85, 2), ACTOR);

        assertEquals("React", response.name());
        assertEquals("Frontend", response.category());
        assertEquals(Short.valueOf((short) 85), response.proficiency());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("SKILL", auditCaptor.getValue().getEntityType());
        assertEquals("React", auditCaptor.getValue().getDetails().get("after").get("name").asText());
    }

    @Test
    void updateSkillRecordsBeforeAndAfterSnapshots() {
        Skill skill = new Skill(profile, "Java", "Backend", (short) 70, 1);
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(skillRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.updateSkill(9L, new SkillWriteRequest("Spring", " Backend ", 90, 4), ACTOR);

        assertEquals("Spring", response.name());
        assertEquals("Backend", response.category());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("Java", auditCaptor.getValue().getDetails().get("before").get("name").asText());
        assertEquals("Spring", auditCaptor.getValue().getDetails().get("after").get("name").asText());
    }

    @Test
    void deleteSkillAuditsTheRemovedItem() {
        Skill skill = new Skill(profile, "TypeScript", null, null, 0);
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(skillRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.of(skill));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        service.deleteSkill(9L, ACTOR);

        verify(skillRepository).delete(skill);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("DELETE", auditCaptor.getValue().getAction());
        assertEquals("TypeScript", auditCaptor.getValue().getDetails().get("before").get("name").asText());
    }

    @Test
    void getSkillCannotReadAnotherProfileSkill() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profile.getId()).thenReturn(3L);
        when(skillRepository.findByIdAndProfileId(9L, 3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSkill(9L));
    }

    @Test
    void writeRequestValidatesRequiredNameAndProficiencyRange() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            assertTrue(validator.validate(new SkillWriteRequest("React", null, 100, 0)).isEmpty());
            assertFalse(validator.validate(new SkillWriteRequest(" ", null, 101, -1)).isEmpty());
        }
    }
}
