package com.sanele.portfolio.messages.dto;

import com.sanele.portfolio.messages.entity.ContactMessage.Status;
import jakarta.validation.constraints.NotNull;

public record ContactMessageStatusRequest(@NotNull Status status) {
}
