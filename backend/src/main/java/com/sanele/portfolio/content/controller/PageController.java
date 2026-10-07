package com.sanele.portfolio.content.controller;

import com.sanele.portfolio.content.dto.PageResponse;
import com.sanele.portfolio.content.dto.PageSummaryResponse;
import com.sanele.portfolio.content.service.PageService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pages")
public class PageController {
    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public List<PageSummaryResponse> getPublishedPages() {
        return pageService.getPublishedPages();
    }

    @GetMapping("/{slug}")
    public PageResponse getPublishedPage(@PathVariable String slug) {
        return pageService.getPublishedPage(slug);
    }
}
