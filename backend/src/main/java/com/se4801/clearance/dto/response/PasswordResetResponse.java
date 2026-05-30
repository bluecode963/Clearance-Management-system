package com.se4801.clearance.dto.response;

public record PasswordResetResponse(
        String message,
        String resetToken
) {
}
