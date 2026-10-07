package com.sanele.portfolio.auth.dto;

import java.util.List;

public record UserResponse(Long id, String username, String email, String displayName, List<String> roles) { }