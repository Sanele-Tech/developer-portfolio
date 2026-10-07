package com.sanele.portfolio.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.repository.PageRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PageServiceTest {
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final PageService service = new PageService(pageRepository);

    @Test
    void returnsPublishedPageBySlug() {
        Page page = mock(Page.class);
        when(pageRepository.findBySlugAndStatus("about", Page.Status.PUBLISHED)).thenReturn(Optional.of(page));
        when(page.getId()).thenReturn(4L);
        when(page.getSlug()).thenReturn("about");
        when(page.getTitle()).thenReturn("About");
        when(page.getExcerpt()).thenReturn("A short introduction");
        when(page.getContent()).thenReturn("Published content");
        when(page.getStatus()).thenReturn(Page.Status.PUBLISHED);

        var response = service.getPublishedPage("about");

        assertEquals(4L, response.id());
        assertEquals("about", response.slug());
        assertEquals("Published content", response.content());
        assertEquals(Page.Status.PUBLISHED, response.status());
    }

    @Test
    void doesNotReturnMissingDraftOrArchivedPages() {
        when(pageRepository.findBySlugAndStatus("private", Page.Status.PUBLISHED)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPublishedPage("private"));
    }

    @Test
    void publishedPageIndexContainsOnlySummariesOfPublishedPages() {
        Page page = mock(Page.class);
        when(pageRepository.findByStatusOrderByUpdatedAtDesc(Page.Status.PUBLISHED)).thenReturn(List.of(page));
        when(page.getSlug()).thenReturn("about");
        when(page.getTitle()).thenReturn("About");
        when(page.getExcerpt()).thenReturn("A short introduction");

        var summaries = service.getPublishedPages();

        assertEquals(1, summaries.size());
        assertEquals("about", summaries.getFirst().slug());
        assertEquals("About", summaries.getFirst().title());
    }
}
