package com.sanele.portfolio.messages.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.messages.dto.ContactMessageResponse;
import com.sanele.portfolio.messages.dto.ContactMessageReplyRecord;
import com.sanele.portfolio.messages.dto.ContactMessageReplyRequest;
import com.sanele.portfolio.messages.dto.ContactMessageReplyResponse;
import com.sanele.portfolio.messages.dto.ContactMessageStatusRequest;
import com.sanele.portfolio.messages.service.ContactMessageAdminService;
import com.sanele.portfolio.messages.service.ContactMessageReplyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/contact-messages")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ContactMessageAdminController {
    private final ContactMessageAdminService messageAdminService;
    private final ContactMessageReplyService replyService;

    public ContactMessageAdminController(ContactMessageAdminService messageAdminService,
            ContactMessageReplyService replyService) {
        this.messageAdminService = messageAdminService;
        this.replyService = replyService;
    }

    @GetMapping
    public Page<ContactMessageResponse> getMessages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return messageAdminService.getMessages(page, size);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ContactMessageResponse updateStatus(@PathVariable Long id,
            @Valid @RequestBody ContactMessageStatusRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return messageAdminService.updateStatus(id, request, actor);
    }

    @GetMapping("/{id}/replies")
    public java.util.List<ContactMessageReplyRecord> getReplies(@PathVariable Long id) {
        return replyService.getReplies(id);
    }

    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.OK)
    public ContactMessageReplyResponse reply(@PathVariable Long id,
            @Valid @RequestBody ContactMessageReplyRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return replyService.reply(id, request, actor);
    }
}
