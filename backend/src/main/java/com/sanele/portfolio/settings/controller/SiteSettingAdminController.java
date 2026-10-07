package com.sanele.portfolio.settings.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.settings.dto.SiteSettingResponse;
import com.sanele.portfolio.settings.dto.SiteSettingWriteRequest;
import com.sanele.portfolio.settings.service.SiteSettingAdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class SiteSettingAdminController {
    private final SiteSettingAdminService siteSettingAdminService;

    public SiteSettingAdminController(SiteSettingAdminService siteSettingAdminService) {
        this.siteSettingAdminService = siteSettingAdminService;
    }

    @GetMapping
    public List<SiteSettingResponse> getSettings() {
        return siteSettingAdminService.getSettings();
    }

    @PutMapping("/{key}")
    public SiteSettingResponse updateSetting(@PathVariable String key,
            @Valid @RequestBody SiteSettingWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return siteSettingAdminService.updateSetting(key, request, actor);
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSetting(@PathVariable String key, @AuthenticationPrincipal PortfolioUserDetails actor) {
        siteSettingAdminService.deleteSetting(key, actor);
    }
}
