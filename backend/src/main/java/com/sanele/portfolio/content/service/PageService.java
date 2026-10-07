package com.sanele.portfolio.content.service;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.content.dto.PageResponse;
import com.sanele.portfolio.content.dto.PageSummaryResponse;
import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.repository.PageRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PageService {
    private final PageRepository pageRepository;

    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public PageResponse getPublishedPage(String slug) {
        Page page = pageRepository.findBySlugAndStatus(slug, Page.Status.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published page", slug));
        return new PageResponse(page.getId(), page.getSlug(), page.getTitle(), page.getExcerpt(),
                page.getContent(), page.getStatus(), page.getCreatedAt(), page.getUpdatedAt());
    }

    public List<PageSummaryResponse> getPublishedPages() {
        return pageRepository.findByStatusOrderByUpdatedAtDesc(Page.Status.PUBLISHED).stream()
                .map(page -> new PageSummaryResponse(page.getSlug(), page.getTitle(),
                        page.getExcerpt(), page.getUpdatedAt()))
                .toList();
    }
}
