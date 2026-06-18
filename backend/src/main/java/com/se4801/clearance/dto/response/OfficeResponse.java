package com.se4801.clearance.dto.response;

public record OfficeResponse(
        Long id,
        String officeName,
        String description,
        boolean active
) {
}
