package com.se4801.clearance.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
}
