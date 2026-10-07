package com.sanele.portfolio.education.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.education.dto.EducationResponse;
import com.sanele.portfolio.education.dto.EducationWriteRequest;
import com.sanele.portfolio.education.service.EducationAdminService;
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
@RequestMapping("/api/admin/education")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class EducationAdminController {
    private final EducationAdminService educationAdminService;

    public EducationAdminController(EducationAdminService educationAdminService) {
        this.educationAdminService = educationAdminService;
    }

    @GetMapping
    public List<EducationResponse> getEducation() {
        return educationAdminService.getEducation();
    }

    @GetMapping("/{id}")
    public EducationResponse getEducation(@PathVariable Long id) {
        return educationAdminService.getEducation(id);
    }

    @PostMapping
    public ResponseEntity<EducationResponse> createEducation(@Valid @RequestBody EducationWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        EducationResponse education = educationAdminService.createEducation(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/education/" + education.id())).body(education);
    }

    @PutMapping("/{id}")
    public EducationResponse updateEducation(@PathVariable Long id,
            @Valid @RequestBody EducationWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return educationAdminService.updateEducation(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEducation(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        educationAdminService.deleteEducation(id, actor);
    }
}
