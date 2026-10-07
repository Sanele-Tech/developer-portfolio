package com.sanele.portfolio.education.controller;

import com.sanele.portfolio.education.dto.EducationResponse;
import com.sanele.portfolio.education.service.EducationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/education")
public class EducationController {
    private final EducationService educationService;
    public EducationController(EducationService educationService) { this.educationService = educationService; }
    @GetMapping
    public List<EducationResponse> getEducation() { return educationService.getEducation(); }
}