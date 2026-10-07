package com.sanele.portfolio.home.dto;

import java.util.List;

public record QuickProfileResponse(String education, String location, String availability, List<String> focus) { }