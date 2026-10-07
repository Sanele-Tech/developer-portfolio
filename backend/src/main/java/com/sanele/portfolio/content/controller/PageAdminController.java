package com.sanele.portfolio.content.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.content.dto.PageResponse;
import com.sanele.portfolio.content.dto.PageWriteRequest;
import com.sanele.portfolio.content.service.PageAdminService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pages")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class PageAdminController {
    private final PageAdminService pageAdminService;

    public PageAdminController(PageAdminService pageAdminService) {
        this.pageAdminService = pageAdminService;
    }

    @GetMapping
    public List<PageResponse> getPages() {
        return pageAdminService.getPages();
    }

    @GetMapping("/{id}")
    public PageResponse getPage(@PathVariable Long id) {
        return pageAdminService.getPage(id);
    }

    @PostMapping
    public ResponseEntity<PageResponse> createPage(@Valid @RequestBody PageWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        PageResponse page = pageAdminService.createPage(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/pages/" + page.id())).body(page);
    }

    @PutMapping("/{id}")
    public PageResponse updatePage(@PathVariable Long id, @Valid @RequestBody PageWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return pageAdminService.updatePage(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePage(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        pageAdminService.deletePage(id, actor);
    }
}
