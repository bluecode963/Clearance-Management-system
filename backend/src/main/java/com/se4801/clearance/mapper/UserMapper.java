package com.se4801.clearance.mapper;

import com.se4801.clearance.dto.response.UserResponse;
import com.se4801.clearance.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
