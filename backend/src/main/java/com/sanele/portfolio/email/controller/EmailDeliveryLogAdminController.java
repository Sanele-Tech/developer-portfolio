package com.sanele.portfolio.email.controller;

import com.sanele.portfolio.email.dto.EmailDeliveryLogResponse;
import com.sanele.portfolio.email.service.EmailDeliveryLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/email-deliveries")
@PreAuthorize("hasRole('ADMIN')")
public class EmailDeliveryLogAdminController {
    private final EmailDeliveryLogService deliveryLogService;

    public EmailDeliveryLogAdminController(EmailDeliveryLogService deliveryLogService) {
        this.deliveryLogService = deliveryLogService;
    }

    @GetMapping
    public Page<EmailDeliveryLogResponse> getLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return deliveryLogService.getLogs(page, size);
    }
}
