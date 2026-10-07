package com.sanele.portfolio.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ConflictException;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.content.dto.PageWriteRequest;
import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.entity.PageVersion;
import com.sanele.portfolio.content.repository.PageRepository;
import com.sanele.portfolio.content.repository.PageVersionRepository;
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
class PageAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private PageRepository pageRepository;
    @Mock private PageVersionRepository pageVersionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private PageAdminService service;

    @BeforeEach
    void setUp() {
        service = new PageAdminService(pageRepository, pageVersionRepository,
                userRepository, auditLogRepository, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void createPageCreatesFirstContentVersionAndAuditEntry() {
        when(pageRepository.findBySlug("about")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(pageRepository.saveAndFlush(any(Page.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createPage(request("about", "About", "Initial content", Page.Status.DRAFT), ACTOR);

        assertEquals("about", response.slug());
        assertEquals(Page.Status.DRAFT, response.status());
        ArgumentCaptor<PageVersion> versionCaptor = ArgumentCaptor.forClass(PageVersion.class);
        verify(pageVersionRepository).save(versionCaptor.capture());
        assertEquals(1, versionCaptor.getValue().getVersionNumber());
        assertEquals("Initial content", versionCaptor.getValue().getContent());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("PAGE", auditCaptor.getValue().getEntityType());
    }

    @Test
    void createPageRejectsDuplicateSlug() {
        when(pageRepository.findBySlug("about")).thenReturn(Optional.of(mock(Page.class)));

        assertThrows(ConflictException.class,
                () -> service.createPage(request("about", "About", "Content", Page.Status.DRAFT), ACTOR));
        verifyNoInteractions(pageVersionRepository, auditLogRepository);
    }

    @Test
    void updatePageAddsVersionOnlyWhenContentChanges() {
        Page page = spy(new Page("about", "About", null, "Version one", Page.Status.DRAFT, mock(User.class)));
        when(page.getId()).thenReturn(7L);
        when(pageRepository.findById(7L)).thenReturn(Optional.of(page));
        when(pageRepository.findBySlug("about")).thenReturn(Optional.of(page));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(pageRepository.saveAndFlush(any(Page.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PageVersion priorVersion = mock(PageVersion.class);
        when(priorVersion.getVersionNumber()).thenReturn(1);
        when(pageVersionRepository.findFirstByPageIdOrderByVersionNumberDesc(7L))
                .thenReturn(Optional.of(priorVersion));

        var response = service.updatePage(7L, request("about", "About me", "Version two", Page.Status.PUBLISHED), ACTOR);

        assertEquals("Version two", response.content());
        assertEquals(Page.Status.PUBLISHED, response.status());
        ArgumentCaptor<PageVersion> versionCaptor = ArgumentCaptor.forClass(PageVersion.class);
        verify(pageVersionRepository).save(versionCaptor.capture());
        assertEquals(2, versionCaptor.getValue().getVersionNumber());
        assertEquals("Version two", versionCaptor.getValue().getContent());
    }

    @Test
    void updatePageDoesNotAddVersionForMetadataOnlyChange() {
        Page page = spy(new Page("about", "About", null, "Same content", Page.Status.DRAFT, mock(User.class)));
        when(page.getId()).thenReturn(7L);
        when(pageRepository.findById(7L)).thenReturn(Optional.of(page));
        when(pageRepository.findBySlug("about")).thenReturn(Optional.of(page));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(pageRepository.saveAndFlush(any(Page.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updatePage(7L, request("about", "About me", "Same content", Page.Status.PUBLISHED), ACTOR);

        verifyNoInteractions(pageVersionRepository);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getPageReturnsNotFoundForMissingId() {
        when(pageRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPage(7L));
    }

    @Test
    void writeRequestValidatesSlugAndRequiredFields() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            assertTrue(validator.validate(request("about-me", "About", "Content", Page.Status.DRAFT)).isEmpty());
            assertFalse(validator.validate(request("About Me", " ", " ", null)).isEmpty());
        }
    }

    private PageWriteRequest request(String slug, String title, String content, Page.Status status) {
        return new PageWriteRequest(slug, title, "Summary", content, status);
    }
}
