package com.sanele.portfolio.admin.controller;

import com.sanele.portfolio.admin.dto.AdminOverviewResponse;
import com.sanele.portfolio.admin.service.AdminOverviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/overview")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class AdminOverviewController {
    private final AdminOverviewService overviewService;

    public AdminOverviewController(AdminOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public AdminOverviewResponse getOverview() {
        return overviewService.getOverview();
    }
}
