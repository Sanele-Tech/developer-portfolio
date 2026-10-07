package com.sanele.portfolio.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ConflictException;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.content.dto.PageResponse;
import com.sanele.portfolio.content.dto.PageWriteRequest;
import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.entity.PageVersion;
import com.sanele.portfolio.content.repository.PageRepository;
import com.sanele.portfolio.content.repository.PageVersionRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PageAdminService {
    private final PageRepository pageRepository;
    private final PageVersionRepository pageVersionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public PageAdminService(PageRepository pageRepository, PageVersionRepository pageVersionRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.pageRepository = pageRepository;
        this.pageVersionRepository = pageVersionRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<PageResponse> getPages() {
        return pageRepository.findAllByOrderByUpdatedAtDescIdDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse getPage(Long id) {
        return toResponse(findPage(id));
    }

    @Transactional
    public PageResponse createPage(PageWriteRequest request, PortfolioUserDetails actor) {
        if (pageRepository.findBySlug(request.slug()).isPresent()) {
            throw new ConflictException("A page with this slug already exists.");
        }
        User author = userRepository.getReferenceById(actor.getId());
        Page page = new Page(request.slug(), request.title().trim(), normalizeOptional(request.excerpt()),
                request.content(), request.status(), author);
        pageRepository.saveAndFlush(page);
        pageVersionRepository.save(new PageVersion(page, 1, page.getContent(), author));
        writeAudit(actor, "CREATE", page, null, snapshot(page));
        return toResponse(page);
    }

    @Transactional
    public PageResponse updatePage(Long id, PageWriteRequest request, PortfolioUserDetails actor) {
        Page page = findPage(id);
        pageRepository.findBySlug(request.slug()).filter(existing -> !id.equals(existing.getId()))
                .ifPresent(existing -> { throw new ConflictException("A page with this slug already exists."); });
        Map<String, Object> before = snapshot(page);
        boolean contentChanged = !page.getContent().equals(request.content());
        User author = userRepository.getReferenceById(actor.getId());
        page.updateDetails(request.slug(), request.title().trim(), normalizeOptional(request.excerpt()),
                request.content(), request.status(), author);
        pageRepository.saveAndFlush(page);
        if (contentChanged) {
            int nextVersion = pageVersionRepository.findFirstByPageIdOrderByVersionNumberDesc(id)
                    .map(PageVersion::getVersionNumber).orElse(0) + 1;
            pageVersionRepository.save(new PageVersion(page, nextVersion, page.getContent(), author));
        }
        writeAudit(actor, "UPDATE", page, before, snapshot(page));
        return toResponse(page);
    }

    @Transactional
    public void deletePage(Long id, PortfolioUserDetails actor) {
        Page page = findPage(id);
        writeAudit(actor, "DELETE", page, snapshot(page), null);
        pageRepository.delete(page);
    }

    private Page findPage(Long id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", id.toString()));
    }

    private PageResponse toResponse(Page page) {
        return new PageResponse(page.getId(), page.getSlug(), page.getTitle(), page.getExcerpt(),
                page.getContent(), page.getStatus(), page.getCreatedAt(), page.getUpdatedAt());
    }

    private Map<String, Object> snapshot(Page page) {
        return Map.of(
                "slug", page.getSlug(),
                "title", page.getTitle(),
                "excerpt", page.getExcerpt() == null ? "" : page.getExcerpt(),
                "content", page.getContent(),
                "status", page.getStatus().name());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Page page,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "PAGE", page.getId(), objectMapper.valueToTree(details)));
    }
}
