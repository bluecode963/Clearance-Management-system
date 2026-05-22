package com.se4801.clearance.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
