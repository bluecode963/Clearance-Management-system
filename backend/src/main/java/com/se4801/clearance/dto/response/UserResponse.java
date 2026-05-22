package com.se4801.clearance.dto.response;

import com.se4801.clearance.model.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        boolean active
) {
}
