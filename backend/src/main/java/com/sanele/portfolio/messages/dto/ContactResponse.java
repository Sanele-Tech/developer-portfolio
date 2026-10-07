package com.sanele.portfolio.messages.dto;

import com.sanele.portfolio.messages.entity.ContactMessage.Status;
import java.time.Instant;

public record ContactResponse(Long id, Status status, Instant createdAt) { }